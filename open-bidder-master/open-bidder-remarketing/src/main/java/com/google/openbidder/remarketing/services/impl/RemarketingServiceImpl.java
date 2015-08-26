/*
 * Copyright 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.openbidder.remarketing.services.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.openbidder.remarketing.model.Remarketing.Action;
import com.google.openbidder.remarketing.model.Remarketing.TargetedUser;
import com.google.openbidder.remarketing.services.RemarketingBucket;
import com.google.openbidder.remarketing.services.RemarketingService;
import com.google.openbidder.storage.dao.Dao;
import com.google.protobuf.MessageLite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * The remarketing service implementation.
 */
@Singleton
public class RemarketingServiceImpl implements RemarketingService {

  public static final Function<Action, String> GET_ACTION_ID = new Function<Action, String>() {
    @Override public String apply(Action action) {
      assert action != null;
      return action.getActionId();
    }};

  private static final String ACTION_OBJECT = "action";
  private static final String TARGETED_USERS_OBJECT = "targeted-users";

  /**
   * Lock for write operations on the index structures {@link #users}, {@link #userIdLookup}.
   * We use this only when races might cause inconsistencies between these indexes.
   * Must be acquired BEFORE {@link #daoWriteUsersLock}, if both are acquired.
   */
  private final Lock indexWriteLock = new ReentrantLock();
  /**
   * Lock for write operations on the DAO, for {@link #TARGETED_USERS_OBJECT}.
   * Must be acquired AFTER {@link #indexWriteLock}, if both are acquired.
   */
  private final Lock daoWriteUsersLock = new ReentrantLock();
  private final Dao<MessageLite> dao;
  private final ExecutorService executorService;

  private Future<ImmutableMap<String, Action>> futureActions;
  /**
   * Maps pubUserId->TargetedUser.
   */
  private final ConcurrentMap<String, TargetedUser> users = new ConcurrentHashMap<>();
  /**
   * Maps googleGid->pubUserId.
   */
  private final ConcurrentMap<String, String> userIdLookup = new ConcurrentHashMap<>();
  private String bucketName;

  @Inject
  public RemarketingServiceImpl(
      Dao<MessageLite> dao,
      @RemarketingBucket ExecutorService executorService,
      @RemarketingBucket String bucketName) {
    this.dao = checkNotNull(dao);
    this.executorService = checkNotNull(executorService);
    this.bucketName = checkNotNull(bucketName);

    futureActions = startGettingActions();
    startGettingTargetedUsers();
  }



  @Override
  public void addTargetedUser(TargetedUser user) {
    indexWriteLock.lock();
    try {
      TargetedUser existingUser = users.get(user.getPubUserId());
      indexUser(existingUser == null ? user : mergeUsers(existingUser, user));
    } finally {
      indexWriteLock.unlock();
    }
  }

  @Override
  public void deleteTargetedUser(String pubUserId) {
    // Benign-racy delete. Just make sure we remove first the googleGid->pubUserId entries.
    for (Map.Entry<String, String> entry : userIdLookup.entrySet()) {
      if (entry.getKey().equals(pubUserId)) {
        userIdLookup.remove(entry.getKey());
      }
    }
    users.remove(pubUserId);
  }

  @Override
  public void deleteAllTargetedUsers() {
    daoWriteUsersLock.lock();
    try {
      dao.deleteObject(bucketName, TARGETED_USERS_OBJECT);
    } finally {
      daoWriteUsersLock.unlock();
    }

    // Benign-racy delete. Just make sure we remove first the googleGid->pubUserId map.
    userIdLookup.clear();
    users.clear();
  }

  @Override
  public TargetedUser getTargetedUser(String userId) {
    return users.get(userId);
  }

  @Override
  public void storeTargetedUsers() {
    daoWriteUsersLock.lock();
    try {
      dao.createObjectList(ImmutableList.copyOf(users.values()), bucketName, TARGETED_USERS_OBJECT);
    } finally {
      daoWriteUsersLock.unlock();
    }
  }

  @Override
  public void deleteActionForUser(String pubUserId, String actionId) {
    indexWriteLock.lock();
    try {
      TargetedUser user = users.get(pubUserId);
      if (user == null) {
        return;
      }

      List<String> userActions = new ArrayList<>(user.getActionIdList());
      if (userActions.remove(actionId)) {
        if (!userActions.isEmpty()) { // if the user has more actions after removing one
          users.put(pubUserId, TargetedUser.newBuilder()
              .addAllActionId(userActions)
              .addAllGoogleGid(user.getGoogleGidList())
              .setPubUserId(user.getPubUserId())
              .build());
        } else { // delete the user since they have no actions
          deleteTargetedUser(pubUserId);
        }
      }
    } finally {
      indexWriteLock.unlock();
    }
  }

  @Override
  public Collection<Action> getActionsForUser(String googleGid) {
    // Benign-racy reads of the two indexes. As long as we read the googleGid->pubUserId
    // index first, worst case is finding the pubUserId but then not finding the user,
    // which is the same effect as just losing the race to a thread removing both entries.
    List<Action> result = new ArrayList<>();
    String pubUserId = userIdLookup.get(googleGid);

    if (pubUserId != null) {
      TargetedUser user = users.get(pubUserId);

      if (user != null) {
        for (Action action : getActions().values()) {
          if (action.getIsEnabled() && user.getActionIdList().contains(action.getActionId())) {
            result.add(action);
          }
        }
      }
    }

    return result;
  }

  @Override
  public void reloadActions() {
    Future<ImmutableMap<String, Action>> futureActionsOld = futureActions;
    futureActions = startGettingActions();
    futureActionsOld.cancel(true);
  }

  @Override
  public void updateTargetedUsers() {
    Iterable<TargetedUser> storedUsers = dao.getObjectList(
        TargetedUser.class, bucketName, TARGETED_USERS_OBJECT);

    if (storedUsers != null) {
      indexWriteLock.lock();
      try {
        for (TargetedUser storedUser : storedUsers) {
          TargetedUser indexUser = users.get(storedUser.getPubUserId());
          indexUser(indexUser == null ? storedUser : mergeUsers(indexUser, storedUser));
        }
      } finally {
        indexWriteLock.unlock();
      }
      storeTargetedUsers();
    }
  }

  /**
   * Merge two targeted users together and keep all the actions unique and sorted.
   */
  private TargetedUser mergeUsers(TargetedUser existingUser, TargetedUser updatedUser) {
    SortedSet<String> actionIds = new TreeSet<>(existingUser.getActionIdList());
    actionIds.addAll(updatedUser.getActionIdList());
    SortedSet<String> googleGids = new TreeSet<>(existingUser.getGoogleGidList());
    googleGids.addAll(updatedUser.getGoogleGidList());
    return TargetedUser.newBuilder()
        .addAllActionId(actionIds)
        .addAllGoogleGid(googleGids)
        .setPubUserId(updatedUser.getPubUserId())
        .build();
  }

  private void startGettingTargetedUsers() {
    executorService.submit(new Runnable() {
      @Override public void run() {
        indexWriteLock.lock();
        try {
          for (TargetedUser user : dao.getObjectList(
              TargetedUser.class, bucketName, TARGETED_USERS_OBJECT)) {
            indexUser(user);
          }
        } finally {
          indexWriteLock.unlock();
        }
      }
    });
  }

  protected void indexUser(TargetedUser user) {
    users.put(user.getPubUserId(), user);

    for (String googleGid : user.getGoogleGidList()) {
      userIdLookup.put(googleGid, user.getPubUserId());
    }
  }

  private Future<ImmutableMap<String, Action>> startGettingActions() {
    return executorService.submit(new Callable<ImmutableMap<String, Action>>() {
      @Override public ImmutableMap<String, Action> call() throws Exception {
        return Maps.uniqueIndex(dao.findAll(
            Action.class, ACTION_OBJECT + '-', bucketName), GET_ACTION_ID);
      }
    });
  }

  private ImmutableMap<String, Action> getActions() {
    try {
      return futureActions.get();
    } catch (ExecutionException e) {
      throw new IllegalStateException("Unable to load actions from service: ", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(e);
    }
  }
}
