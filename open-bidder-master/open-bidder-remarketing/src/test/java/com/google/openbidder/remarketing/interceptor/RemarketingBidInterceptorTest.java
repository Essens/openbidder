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

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.openbidder.api.bidding.BidController;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.testing.bidding.BiddingTestUtil;
import com.google.openbidder.cloudstorage.GoogleCloudStorage;
import com.google.openbidder.cloudstorage.testing.FakeGoogleCloudStorage;
import com.google.openbidder.exchange.doubleclick.testing.TestBidRequestBuilder;
import com.google.openbidder.exchange.doubleclick.testing.TestBidResponseBuilder;
import com.google.openbidder.remarketing.model.Remarketing.Action;
import com.google.openbidder.remarketing.model.Remarketing.TargetedUser;
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
 * Unit tests for {@link RemarketingBidInterceptor}
 */
public class RemarketingBidInterceptorTest {

  private static final String BUCKET_NAME = "remarketing-bucket";
  private static final String GOOGLE_GID = "random-encrypted-gid";
  private static final ExecutorService executorService = MoreExecutors.newDirectExecutorService();

  private BidController controller;
  private RemarketingService service;
  private Dao<MessageLite> dao;

  @Before
  public void setUp() throws IOException {
    GoogleCloudStorage cloudStorage = new FakeGoogleCloudStorage(new FakeClock());
    cloudStorage.putBucket(BUCKET_NAME);
    ProtobufConverter converter = new ProtobufConverter();
    dao = new CloudStorageDao<>(cloudStorage, converter);
    service = new RemarketingServiceImpl(dao, executorService, BUCKET_NAME);

    controller = BiddingTestUtil.newBidController(new RemarketingBidInterceptor(service));
  }

  @After
  public void tearDown() {
    if (controller != null) {
      controller.stopAsync().awaitTerminated();
    }
  }

  @Test
  public void addTargetedUser_bidRequest_targetedUserActionFound() {
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

    TargetedUser user = TargetedUser.newBuilder()
        .setPubUserId("publisher_id")
        .addGoogleGid(GOOGLE_GID)
        .addActionId("001")
        .build();

    service.addTargetedUser(user);

    BidRequest request = generateRequest("");
    BidResponse response = TestBidResponseBuilder.create().build();
    controller.onRequest(request, response);
    assertEquals(response.httpResponse().getRedirectParameters(), ImmutableMultimap.of());
    assertEquals(response.httpResponse().getCookies(), ImmutableMultimap.of());

    request = generateRequest(GOOGLE_GID);
    response = TestBidResponseBuilder.create().build();
    controller.onRequest(request, response);
    BiddingTestUtil.assertBidAmounts(response, 0.012345);
  }

  private void addAction(Action action) {
    dao.createObject(action, "remarketing-bucket", "action-" + action.getActionId());
  }

  private BidRequest generateRequest(String googleGid) {
    TestBidRequestBuilder request = TestBidRequestBuilder.create().setRequest("1", 1, 1, 100);
    request.nativeBuilder().setGoogleUserId(googleGid);
    return request.build();
  }
}
