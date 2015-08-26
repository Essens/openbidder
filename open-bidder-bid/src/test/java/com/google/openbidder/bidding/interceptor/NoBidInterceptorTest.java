/*
 * Copyright 2015 Google Inc. All Rights Reserved.
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

package com.google.openbidder.bidding.interceptor;

import static org.junit.Assert.assertTrue;

import com.google.common.collect.Iterables;
import com.google.openbidder.api.bidding.BidController;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.testing.bidding.BiddingTestUtil;
import com.google.openbidder.api.testing.bidding.TestBidRequestBuilder;
import com.google.openbidder.api.testing.bidding.TestBidResponseBuilder;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid.Bid;

import org.junit.Test;

/**
 * Tests for {@link NoBidInterceptor}.
 */
public class NoBidInterceptorTest {

  @Test
  public void testOk() {
    BidRequest request = TestBidRequestBuilder.create().setRequest("1", 1, 1, 1.0).build();
    BidResponse response =
        TestBidResponseBuilder.create().build().addBid(Bid.newBuilder().buildPartial());

    BidController controller = BiddingTestUtil.newBidController(new NoBidInterceptor());
    controller.onRequest(request, response);
    assertTrue(Iterables.isEmpty(response.bids()));
  }
}
