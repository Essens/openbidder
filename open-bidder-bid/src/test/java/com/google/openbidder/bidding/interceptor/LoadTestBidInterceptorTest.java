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

import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.testing.bidding.TestBidRequestBuilder;
import com.google.openbidder.api.testing.bidding.TestBidResponseBuilder;
import com.google.openbidder.api.testing.interceptor.NoopInterceptorChain;
import com.google.openrtb.OpenRtb;

import com.codahale.metrics.MetricRegistry;

import org.junit.Test;

/**
 * Tests for {@link LoadTestBidInterceptor}.
 */
public class LoadTestBidInterceptorTest {

  @Test
  public void testDelay() {
    LoadTestBidInterceptor interceptor = new LoadTestBidInterceptor(10L, null);
    BidRequest request = TestBidRequestBuilder.create()
        .setRequest(OpenRtb.BidRequest.newBuilder().buildPartial())
        .build();
    BidResponse response = TestBidResponseBuilder.create().build();
    long time = System.currentTimeMillis();
    NoopInterceptorChain.execute(interceptor, request, response, new MetricRegistry());
    time = System.currentTimeMillis() - time;
    assertTrue(time >= 10);
  }

  @Test
  public void testWork() {
    LoadTestBidInterceptor interceptor = new LoadTestBidInterceptor(null, 10L);
    BidRequest request = TestBidRequestBuilder.create()
        .setRequest(OpenRtb.BidRequest.newBuilder().buildPartial())
        .build();
    BidResponse response = TestBidResponseBuilder.create().build();
    long time = System.currentTimeMillis();
    NoopInterceptorChain.execute(interceptor, request, response, new MetricRegistry());
    time = System.currentTimeMillis() - time;
    assertTrue(time >= 10);
  }
}
