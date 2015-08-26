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

package com.google.openbidder.remarketing.services;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.openbidder.cloudstorage.GoogleCloudStorage;
import com.google.openbidder.cloudstorage.testing.FakeGoogleCloudStorage;
import com.google.openbidder.remarketing.model.Remarketing.Action;
import com.google.openbidder.remarketing.model.Remarketing.TargetedUser;
import com.google.openbidder.remarketing.services.impl.RemarketingServiceImpl;
import com.google.openbidder.storage.dao.CloudStorageDao;
import com.google.openbidder.storage.dao.Dao;
import com.google.openbidder.storage.utils.ProtobufConverter;
import com.google.openbidder.util.testing.FakeClock;
import com.google.protobuf.MessageLite;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Unit tests for {@link RemarketingService}
 */
public class RemarketingServiceTest {
  private static final String BUCKET_NAME = "remarketing-bucket";
  private static final String TARGETED_USERS_NAME = "targeted-users";

  private RemarketingService remarketingService;
  private Dao<MessageLite> dao;
  private ProtobufConverter converter;
  private ExecutorService executorService;

  @Before
  public void setUp() throws Exception {
    executorService = MoreExecutors.newDirectExecutorService();
    GoogleCloudStorage cloudStorage = new FakeGoogleCloudStorage(new FakeClock());
    cloudStorage.putBucket(BUCKET_NAME);
    converter = new ProtobufConverter();
    dao = new CloudStorageDao<>(cloudStorage, converter);
    remarketingService = new RemarketingServiceImpl(dao, executorService, BUCKET_NAME);
  }

  @Test
  public void addTargetedUser_user_userAdded() {
    TargetedUser user = TargetedUser.newBuilder()
        .setPubUserId("pub123")
        .addGoogleGid("goog123")
        .addActionId("action123")
        .build();

    remarketingService.addTargetedUser(user);
    assertEquals(user, remarketingService.getTargetedUser("pub123"));
    assertNull(remarketingService.getTargetedUser("something-else"));
  }

  @Test
  public void addTargetedUser_uniqueUsers_usersAdded() {
    TargetedUser userA = TargetedUser.newBuilder()
        .setPubUserId("pub123")
        .addGoogleGid("goog123")
        .addAllActionId(Arrays.asList("action123", "action000"))
        .build();

    TargetedUser userB = TargetedUser.newBuilder()
        .setPubUserId("pub321")
        .addGoogleGid("goog321")
        .addActionId("action321")
        .build();

    remarketingService.addTargetedUser(userA);
    remarketingService.addTargetedUser(userB);

    assertEquals(userA, remarketingService.getTargetedUser("pub123"));
    assertEquals(userB, remarketingService.getTargetedUser("pub321"));
    assertNull(remarketingService.getTargetedUser("something-else"));
  }

  @Test
  public void addTargetedUser_usersSameId_userMerged() {
    TargetedUser userA = TargetedUser.newBuilder()
        .setPubUserId("pub123")
        .addGoogleGid("goog123")
        .addAllActionId(Arrays.asList("action100", "action101"))
        .build();

    TargetedUser userB = TargetedUser.newBuilder()
        .setPubUserId("pub123")
        .addGoogleGid("goog123")
        .addAllActionId(Arrays.asList("action104", "action103", "action101"))
        .build();

    TargetedUser userAUnionB = TargetedUser
        .newBuilder()
        .setPubUserId("pub123")
        .addGoogleGid("goog123")
        .addAllActionId(Arrays.asList("action100", "action101", "action103", "action104"))
        .build();

    remarketingService.addTargetedUser(userA); // Add two different objects with the same pubUserId
    remarketingService.addTargetedUser(userB);

    assertEquals(
        userAUnionB.getActionIdList(),
        remarketingService.getTargetedUser("pub123").getActionIdList());

    remarketingService.deleteAllTargetedUsers();

    remarketingService.addTargetedUser(userA); // Add two of the same object
    remarketingService.addTargetedUser(userA);
    assertEquals(userA, remarketingService.getTargetedUser("pub123"));
  }

  @Test
  public void deleteTargetedUser_user_userDeleted() {
    TargetedUser user = TargetedUser.newBuilder()
        .setPubUserId("pub123")
        .addGoogleGid("goog123")
        .addActionId("action123")
        .build();

    remarketingService.addTargetedUser(user);
    assertEquals(user, remarketingService.getTargetedUser("pub123"));

    remarketingService.deleteTargetedUser("pub123");
    assertNull(remarketingService.getTargetedUser("pub123"));
  }

  @Test
  public void deletedAllTargetedUsers_users_usersDeleted() {
    TargetedUser user = TargetedUser.newBuilder()
        .setPubUserId("pub123")
        .addGoogleGid("goog123")
        .addActionId("action123")
        .build();

    remarketingService.addTargetedUser(user);
    assertEquals(user, remarketingService.getTargetedUser("pub123")); // its there before delete

    remarketingService.deleteAllTargetedUsers(); // removed locally
    assertNull(remarketingService.getTargetedUser("pub123"));

    assertEquals(emptyList(), dao.getObjectList(
        TargetedUser.class, BUCKET_NAME, TARGETED_USERS_NAME));
  }

  @Test
  public void getTargetedUser_user_userReturned() {
    TargetedUser user = TargetedUser.newBuilder()
        .setPubUserId("pub123")
        .addGoogleGid("goog123")
        .addActionId("action123")
        .build();

    remarketingService.addTargetedUser(user);
    assertEquals(user, remarketingService.getTargetedUser("pub123"));
    assertNull(remarketingService.getTargetedUser("something-else"));

    remarketingService.deleteTargetedUser("pub123");
    assertNull(remarketingService.getTargetedUser("pub123"));
    assertNull(remarketingService.getTargetedUser("something-else"));
  }

  @Test
  public void storeTargetedUsers_users_usersStored() {
    TargetedUser userA = TargetedUser.newBuilder()
        .setPubUserId("pub123")
        .addGoogleGid("goog123")
        .addActionId("action123")
        .build();

    TargetedUser userB = TargetedUser.newBuilder()
        .setPubUserId("pub321")
        .addGoogleGid("goog321")
        .addActionId("action321")
        .build();

    remarketingService.addTargetedUser(userA);
    remarketingService.addTargetedUser(userB);
    remarketingService.storeTargetedUsers();

    assertEquals(userA, remarketingService.getTargetedUser("pub123"));
    assertEquals(userB, remarketingService.getTargetedUser("pub321"));

    List<TargetedUser> users = dao.getObjectList(
        TargetedUser.class, BUCKET_NAME, TARGETED_USERS_NAME);

    assertEquals(2, users.size());
    assertTrue(users.contains(userA));
    assertTrue(users.contains(userB));
  }

  @Test
  public void deleteActionForUser_user_actionDeleted() {
    TargetedUser user = TargetedUser.newBuilder()
        .setPubUserId("pub123")
        .addGoogleGid("goog123")
        .addAllActionId(Arrays.asList("action1", "action2"))
        .build();

    TargetedUser userWithOneAction = TargetedUser.newBuilder()
        .setPubUserId("pub123")
        .addGoogleGid("goog123")
        .addActionId("action1")
        .build();

    remarketingService.addTargetedUser(user);

    remarketingService.deleteActionForUser("another-pub", "action1");
    assertEquals(user, remarketingService.getTargetedUser("pub123"));

    remarketingService.deleteActionForUser("pub123", "another-fake-action");
    assertEquals(user, remarketingService.getTargetedUser("pub123"));

    remarketingService.deleteActionForUser("pub123", "action2");
    assertEquals(userWithOneAction, remarketingService.getTargetedUser("pub123"));

    remarketingService.deleteActionForUser("pub123", "action1");
    assertNull(remarketingService.getTargetedUser("pub123"));
  }

  @Test
  public void getActionsForUser_actions_actionsReturned() {
    Action actionA = Action.newBuilder()
        .setActionId("001")
        .setIsEnabled(true)
        .setDescription("Nexus 7")
        .setClickThroughUrl("http://www.google.com")
        .setMaxCpm(12345)
        .setCreative("<img src='https://www.google.com/images/srpr/logo3w.png'></img>")
        .build();

    Action actionB = Action.newBuilder()
        .setActionId("002")
        .setIsEnabled(true)
        .setDescription("Nexus 4")
        .setClickThroughUrl("http://www.google.com")
        .setMaxCpm(5555)
        .setCreative("<img src='https://www.google.com/images/srpr/logo4w.png'></img>")
        .build();

    Map<String, Action> actions = ImmutableMap.of(
        "action-project1-001", actionA,
        "action-project1-002", actionB);

    dao.createObjects(actions, BUCKET_NAME);

    remarketingService = new RemarketingServiceImpl(dao, executorService, BUCKET_NAME);

    TargetedUser user = TargetedUser.newBuilder()
        .setPubUserId("pub123")
        .addGoogleGid("goog1")
        .addAllActionId(Arrays.asList("001", "002"))
        .build();

    TargetedUser userNewAction = TargetedUser
        .newBuilder()
        .setPubUserId("pub123")
        .addGoogleGid("goog1")
        .addActionId("001")
        .build();

    remarketingService.addTargetedUser(user);
    remarketingService.addTargetedUser(userNewAction);

    assertEquals(ImmutableSet.of(actionA, actionB),
        ImmutableSet.copyOf(remarketingService.getActionsForUser("goog1")));
  }

  @Test
  public void getActionForUser_actions_actionReturned() {
    Action actionA = Action.newBuilder()
        .setActionId("001")
        .setIsEnabled(false)  // this wont be returned since its not enabled
        .setDescription("Nexus 7")
        .setClickThroughUrl("http://www.google.com")
        .setMaxCpm(12345)
        .setCreative("<img src='https://www.google.com/images/srpr/logo3w.png'></img>")
        .build();

    Action actionB = Action.newBuilder()
        .setActionId("002")
        .setIsEnabled(true)
        .setDescription("Nexus 4")
        .setClickThroughUrl("http://www.google.com")
        .setMaxCpm(5555)
        .setCreative("<img src='https://www.google.com/images/srpr/logo4w.png'></img>")
        .build();

    Map<String, Action> actions = ImmutableMap.of(
        "action-project1-001", actionA,
        "action-project1-002", actionB);

    dao.createObjects(actions, BUCKET_NAME);

    remarketingService = new RemarketingServiceImpl(dao, executorService, BUCKET_NAME);

    TargetedUser user = TargetedUser.newBuilder()
        .setPubUserId("pub123")
        .addGoogleGid("goog123")
        .addAllActionId(Arrays.asList("001", "002"))
        .build();

    TargetedUser userNewAction = TargetedUser
        .newBuilder()
        .setPubUserId("pub123")
        .addGoogleGid("goog123")
        .addActionId("action1")
        .build();

    remarketingService.addTargetedUser(user);
    remarketingService.addTargetedUser(userNewAction);

    assertEquals(singleton(actionB),
        ImmutableSet.copyOf(remarketingService.getActionsForUser("goog123")));
  }

  @Test
  public void getActionForUser_actions_noActionsReturned() {
    Action actionA = Action.newBuilder()
        .setActionId("001")
        .setIsEnabled(false)  // this wont be returned since its not enabled
        .setDescription("Nexus 7")
        .setClickThroughUrl("http://www.google.com")
        .setMaxCpm(12345)
        .setCreative("<img src='https://www.google.com/images/srpr/logo3w.png'></img>")
        .build();

    Action actionB = Action.newBuilder()
        .setActionId("002")
        .setIsEnabled(true)
        .setDescription("Nexus 4")
        .setClickThroughUrl("http://www.google.com")
        .setMaxCpm(55555)
        .setCreative("<img src='https://www.google.com/images/srpr/logo4w.png'></img>")
        .build();

    Map<String, Action> actions = ImmutableMap.of(
        "action-project1-001", actionA,
        "action-project1-002", actionB);

    dao.createObjects(actions, BUCKET_NAME);

    remarketingService = new RemarketingServiceImpl(dao, executorService, BUCKET_NAME);

    TargetedUser user = TargetedUser.newBuilder()
        .setPubUserId("pub123")
        .addGoogleGid("goog123")
        .addAllActionId(Arrays.asList("001", "002"))
        .build();

    TargetedUser userNewAction = TargetedUser.newBuilder()
        .setPubUserId("pub123")
        .addGoogleGid("goog123")
        .addActionId("001")
        .build();

    remarketingService.addTargetedUser(user);
    remarketingService.addTargetedUser(userNewAction);

    assertEquals(singleton(actionB),
        ImmutableSet.copyOf(remarketingService.getActionsForUser("goog123")));
    assertEquals(emptySet(), ImmutableSet.copyOf(remarketingService.getActionsForUser("goog1")));
    assertEquals(emptySet(), ImmutableSet.copyOf(remarketingService.getActionsForUser("")));
  }

  @Test
  public void getActionForUser_actions_validActionsReturned() {
    Action actionA = Action.newBuilder()
        .setActionId("001")
        .setIsEnabled(false)
        .setDescription("Nexus 7")
        .setClickThroughUrl("http://www.google.com")
        .setMaxCpm(12345)
        .setCreative("<img src='https://www.google.com/images/srpr/logo3w.png'></img>")
        .build();

    Action actionB = Action.newBuilder()
        .setActionId("002")
        .setIsEnabled(true)
        .setDescription("Nexus 4")
        .setClickThroughUrl("http://www.google.com")
        .setMaxCpm(55555)
        .setCreative("<img src='https://www.google.com/images/srpr/logo4w.png'></img>")
        .build();

    Action actionC = Action.newBuilder()
        .setActionId("003")
        .setIsEnabled(true)
        .setDescription("Nexus 10")
        .setClickThroughUrl("http://www.google.com")
        .setMaxCpm(66666)
        .setCreative("<img src='https://www.google.com/images/srpr/logo2w.png'></img>")
        .build();

    Map<String, Action> actions = ImmutableMap.of(
        "action-project1-001", actionA,
        "action-project1-002", actionB,
        "action-project1-003", actionC);

    dao.createObjects(actions, BUCKET_NAME);

    remarketingService = new RemarketingServiceImpl(dao, executorService, BUCKET_NAME);

    TargetedUser user = TargetedUser.newBuilder()
        .setPubUserId("pub123")
        .addGoogleGid("goog123")
        .addAllActionId(Arrays.asList("003"))
        .build();

    TargetedUser irrelevantUser = TargetedUser.newBuilder()
        .setPubUserId("pub-zzzz")
        .addGoogleGid("goog1111")
        .addAllActionId(Arrays.asList("001", "002", "003"))
        .build();

    remarketingService.addTargetedUser(user);
    remarketingService.addTargetedUser(irrelevantUser);

    assertEquals(singleton(actionC),
        ImmutableSet.copyOf(remarketingService.getActionsForUser("goog123")));
  }

  @Test
  public void updateTargetedUsers_uniqueTargetedUsers_localAndRemoteUsersMerged() {
    TargetedUser user = TargetedUser.newBuilder()
        .setPubUserId("pub123")
        .addGoogleGid("goog123")
        .addAllActionId(Arrays.asList("001", "002"))
        .build();

    dao.createObjectList(singletonList(user), BUCKET_NAME, TARGETED_USERS_NAME);

    remarketingService = new RemarketingServiceImpl(dao, executorService, BUCKET_NAME);

    TargetedUser newUser = TargetedUser.newBuilder()
        .setPubUserId("pub321")
        .addGoogleGid("goog321")
        .addActionId("001")
        .build();

    remarketingService.addTargetedUser(newUser);
    remarketingService.updateTargetedUsers();

    assertEquals(user, remarketingService.getTargetedUser("pub123"));
    assertEquals(newUser, remarketingService.getTargetedUser("pub321"));
    assertEquals(ImmutableSet.of(newUser, user), ImmutableSet.copyOf(
        dao.getObjectList(TargetedUser.class, BUCKET_NAME, TARGETED_USERS_NAME)));
  }

  @Test
  public void updateTargetedUsers_targetedUsers_localAndRemoteUsersMerged() {
    TargetedUser user = TargetedUser.newBuilder()
        .setPubUserId("pub123")
        .addGoogleGid("goog123")
        .addAllActionId(Arrays.asList("001", "002"))
        .build();

    dao.createObjectList(singletonList(user), BUCKET_NAME, TARGETED_USERS_NAME);

    remarketingService = new RemarketingServiceImpl(dao, executorService, BUCKET_NAME);

    TargetedUser newUser = TargetedUser.newBuilder()
        .setPubUserId("pub123")
        .addGoogleGid("goog3")
        .addActionId("001")
        .build();

    remarketingService.addTargetedUser(newUser);
    remarketingService.updateTargetedUsers();

    TargetedUser expectedMergedUser = TargetedUser.newBuilder()
        .setPubUserId("pub123")
        .addAllGoogleGid(Arrays.asList("goog123", "goog3"))
        .addAllActionId(Arrays.asList("001", "002"))
        .build();

    assertEquals(expectedMergedUser, remarketingService.getTargetedUser("pub123"));
    assertNull(remarketingService.getTargetedUser("pub321"));
    assertEquals(singleton(expectedMergedUser), ImmutableSet.copyOf(dao.getObjectList(
        TargetedUser.class, BUCKET_NAME, TARGETED_USERS_NAME)));
  }
}
