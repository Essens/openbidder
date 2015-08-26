/*
 * Copyright 2014 Google Inc. All Rights Reserved.
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

package com.google.openbidder.exchange.doubleclick.interceptor;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.doubleclick.DcExt;
import com.google.openbidder.api.bidding.BidController;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.testing.bidding.BiddingTestUtil;
import com.google.openbidder.api.testing.bidding.SimpleBidInterceptor;
import com.google.openbidder.exchange.doubleclick.DoubleClickConstants;
import com.google.openbidder.exchange.doubleclick.testing.DoubleClickTestUtil;
import com.google.openbidder.exchange.doubleclick.testing.TestBidRequestBuilder;
import com.google.openbidder.exchange.doubleclick.testing.TestBidResponseBuilder;
import com.google.protos.adx.NetworkBid;
import com.google.protos.adx.NetworkBid.BidRequest.AdSlot;
import com.google.protos.adx.NetworkBid.BidRequest.AdSlot.MatchingAdData;

import com.codahale.metrics.MetricRegistry;

import org.junit.Test;

/**
 * Tests for {@link FlashlessBidInterceptor}.
 */
public class FlashlessBidInterceptorTest {

  @Test
  public void testFlashless() {
    BidRequest request = TestBidRequestBuilder.create()
        .setNativeRequest(newFlashlessRequest()).build();
    BidResponse response = TestBidResponseBuilder.create().build();
    MetricRegistry metricRegistry = new MetricRegistry();
    BidController controller = BiddingTestUtil.newBidController(
        metricRegistry,
        new FlashlessBidInterceptor(metricRegistry),
        new SimpleBidInterceptor());
    controller.onRequest(request, response);
    assertTrue(response.bidWithId("1").getExtension(DcExt.ad)
        .getAttributeList().contains(DoubleClickConstants.CREATIVE_NON_FLASH));
  }

  @Test
  public void testNonFlashless() {
    BidRequest request = TestBidRequestBuilder.create().setRequest("1", 1, 1, 100).build();
    BidResponse response = TestBidResponseBuilder.create().build();
    MetricRegistry metricRegistry = new MetricRegistry();
    BidController controller = BiddingTestUtil.newBidController(
        metricRegistry,
        new FlashlessBidInterceptor(metricRegistry),
        new SimpleBidInterceptor());
    controller.onRequest(request, response);
    assertFalse(response.bidWithId("1").getExtension(DcExt.ad)
        .getAttributeList().contains(DoubleClickConstants.CREATIVE_NON_FLASH));
  }

  public static NetworkBid.BidRequest.Builder newFlashlessRequest() {
    return NetworkBid.BidRequest.newBuilder()
        .setId(DoubleClickTestUtil.REQUEST_ID)
        .addAdslot(AdSlot.newBuilder()
            .setId(1)
            .addWidth(200)
            .addHeight(50)
            .addAllExcludedAttribute(asList(DoubleClickConstants.CREATIVE_FLASH))
            .addMatchingAdData(MatchingAdData.newBuilder().setAdgroupId(10)));
  }
}
