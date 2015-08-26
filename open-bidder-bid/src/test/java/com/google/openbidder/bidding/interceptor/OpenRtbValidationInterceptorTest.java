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

package com.google.openbidder.bidding.interceptor;

import static org.junit.Assert.assertFalse;

import com.google.common.collect.Iterables;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.testing.bidding.TestBidRequestBuilder;
import com.google.openbidder.api.testing.bidding.TestBidResponseBuilder;
import com.google.openbidder.api.testing.interceptor.NoopInterceptorChain;
import com.google.openrtb.OpenRtb;
import com.google.openrtb.OpenRtb.BidRequest.Imp;
import com.google.openrtb.OpenRtb.BidRequest.Imp.Banner;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid.Bid;
import com.google.openrtb.util.OpenRtbValidator;

import com.codahale.metrics.MetricRegistry;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link OpenRtbValidationInterceptor}
 */
public class OpenRtbValidationInterceptorTest {
  private static BidRequest request = TestBidRequestBuilder.create()
      .setRequest(OpenRtb.BidRequest.newBuilder()
          .setId("1")
          .addImp(Imp.newBuilder()
              .setId("1")
              .setBanner(Banner.newBuilder().setId("1"))))
      .build();

  private MetricRegistry metricRegistry;
  private OpenRtbValidationInterceptor interceptor;

  @Before
  public void setUp() {
    metricRegistry = new MetricRegistry();
    interceptor = new OpenRtbValidationInterceptor(new OpenRtbValidator(metricRegistry));
  }

  @Test
  public void testBannerNoAttrs() {
    BidResponse response = TestBidResponseBuilder.create().build().addBid(basicBid());
    NoopInterceptorChain.execute(interceptor, request, response, metricRegistry);
    assertFalse(Iterables.isEmpty(response.bids()));
  }

  private static Bid.Builder basicBid() {
    return Bid.newBuilder()
        .setId("1")
        .setImpid("1")
        .setPrice(0.1);
  }
}
