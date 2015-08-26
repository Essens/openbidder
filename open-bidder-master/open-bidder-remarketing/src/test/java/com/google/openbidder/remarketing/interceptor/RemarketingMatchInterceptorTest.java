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

package com.google.openbidder.remarketing.interceptor;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.openbidder.api.match.MatchController;
import com.google.openbidder.cloudstorage.GoogleCloudStorage;
import com.google.openbidder.cloudstorage.testing.FakeGoogleCloudStorage;
import com.google.openbidder.exchange.doubleclick.match.DoubleClickMatchRequest;
import com.google.openbidder.exchange.doubleclick.match.DoubleClickMatchResponse;
import com.google.openbidder.exchange.doubleclick.testing.DoubleClickTestUtil;
import com.google.openbidder.exchange.doubleclick.testing.TestMatchRequestBuilder;
import com.google.openbidder.exchange.doubleclick.testing.TestMatchResponseBuilder;
import com.google.openbidder.http.cookie.StandardCookie;
import com.google.openbidder.remarketing.model.Remarketing.Action;
import com.google.openbidder.remarketing.services.RemarketingService;
import com.google.openbidder.remarketing.services.impl.RemarketingServiceImpl;
import com.google.openbidder.storage.dao.CloudStorageDao;
import com.google.openbidder.storage.dao.Dao;
import com.google.openbidder.storage.utils.ProtobufConverter;
import com.google.openbidder.util.testing.FakeClock;
import com.google.protobuf.MessageLite;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

/**
 * Unit tests for {@link RemarketingMatchInterceptor}
 */
public class RemarketingMatchInterceptorTest {

  private static final String BUCKET_NAME = "remarketing-bucket";
  private static final String GOOGLE_GID = "random-encrypted-gid";
  private static final ExecutorService executorService = MoreExecutors.newDirectExecutorService();

  private MatchController controller;
  private RemarketingService service;
  private GoogleCloudStorage cloudStorage;
  private ProtobufConverter converter;
  private Dao<MessageLite> dao;

  @Before
  public void setUp() throws IOException {
    cloudStorage = new FakeGoogleCloudStorage(new FakeClock());
    cloudStorage.putBucket(BUCKET_NAME);
    converter = new ProtobufConverter();
    dao = new CloudStorageDao<>(cloudStorage, converter);
    service = new RemarketingServiceImpl(dao, executorService, BUCKET_NAME);
    controller = DoubleClickTestUtil.newMatchController(new RemarketingMatchInterceptor(service));
  }

  @After
  public void tearDown() {
    if (controller != null) {
      controller.stopAsync().awaitTerminated();
    }
  }

  @Test
  public void addTargetedUser_matchRequest_targetedUserActionFound() {
    Action action = Action.newBuilder()
        .setActionId("001")
        .setDescription("some description")
        .setClickThroughUrl("http://www.google.com")
        .setMaxCpm(12345)
        .setCreative("<img src='https://www.google.com/images/srpr/logo4w.png'></img>")
        .setIsEnabled(true)
        .build();

    addAction(action);
    service.reloadActions();

    DoubleClickMatchRequest request = TestMatchRequestBuilder.create()
        .addCookie(StandardCookie.create("pub_id", "some-unique-pub-user-id"))
        .addParameters("add_action", "001", "google_gid", GOOGLE_GID)
        .build();
    DoubleClickMatchResponse response = TestMatchResponseBuilder.create().build();

    controller.onRequest(request, response);

    assertEquals(singleton(action), ImmutableSet.copyOf(service.getActionsForUser(GOOGLE_GID)));
    assertEquals(emptySet(), ImmutableSet.copyOf(service.getActionsForUser("nothing")));
  }

  @Test
  public void addAndDeleteTargetedUser_matchRequest_noActionsFound() {
    Action action = Action.newBuilder()
        .setActionId("001")
        .setDescription("some description")
        .setClickThroughUrl("http://www.google.com")
        .setMaxCpm(12345)
        .setCreative("<img src='https://www.google.com/images/srpr/logo4w.png'></img>")
        .setIsEnabled(true)
        .build();

    addAction(action);
    service.reloadActions();

    DoubleClickMatchRequest request = TestMatchRequestBuilder.create()
        .addCookie(StandardCookie.create("pub_id", "some-unique-pub-user-id"))
        .addParameters(
            "add_action", "001",
            "delete_action", "001",
            "google_gid", GOOGLE_GID)
        .build();
    DoubleClickMatchResponse response = TestMatchResponseBuilder.create().build();

    controller.onRequest(request,response);

    assertEquals(emptySet(), ImmutableSet.copyOf(service.getActionsForUser(GOOGLE_GID)));
  }

  @Test
  public void addTargetedUser_matchRequest_targetedUserActionsFound() {
    Action actionA = Action.newBuilder()
        .setActionId("001")
        .setDescription("some description")
        .setClickThroughUrl("http://www.google.com")
        .setMaxCpm(12345)
        .setCreative("<img src='https://www.google.com/images/srpr/logo3w.png'></img>")
        .setIsEnabled(true)
        .build();
    Action actionB = Action.newBuilder()
        .setActionId("002")
        .setDescription("some other description")
        .setClickThroughUrl("http://www.google.com")
        .setMaxCpm(12345)
        .setCreative("<img src='https://www.google.com/images/srpr/logo4w.png'></img>")
        .setIsEnabled(true)
        .build();

    addAction(actionA);
    addAction(actionB);
    service.reloadActions();

    DoubleClickMatchRequest request = TestMatchRequestBuilder.create()
        .addCookie(StandardCookie.create("pub_id", "some-unique-pub-user-id"))
        .addParameters(
            "add_action", "001",
            "add_action", "002",
            "google_gid", GOOGLE_GID)
        .build();
    DoubleClickMatchResponse response = TestMatchResponseBuilder.create().build();

    controller.onRequest(request,response);

    assertEquals(ImmutableSet.of(actionA, actionB),
        ImmutableSet.copyOf(service.getActionsForUser(GOOGLE_GID)));
  }

  private void addAction(Action action) {
    dao.createObject(action, "remarketing-bucket", "action-" + action.getActionId());
  }
}
