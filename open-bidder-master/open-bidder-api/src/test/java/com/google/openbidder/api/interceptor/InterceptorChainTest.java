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

package com.google.openbidder.api.interceptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.openbidder.api.bidding.BidInterceptor;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.bidding.TestInterceptor;
import com.google.openbidder.api.testing.bidding.TestBidRequestBuilder;
import com.google.openbidder.api.testing.bidding.TestBidResponseBuilder;

import com.codahale.metrics.MetricRegistry;

import org.junit.Test;

/**
 * Tests for {@link InterceptorChain}.
 */
public class InterceptorChainTest {

  @Test
  public void testChain() {
    StandardInterceptorController<BidRequest, BidResponse> controller =
        new StandardInterceptorController<>(
            ImmutableList.<BidInterceptor>of(new TestInterceptor()), new MetricRegistry());
    InterceptorChain<BidRequest, BidResponse> chain = new InterceptorChain<>(
        controller,
        TestBidRequestBuilder.create().build(),
        TestBidResponseBuilder.create().build());
    assertNotNull(chain.toString());
    assertEquals(1, Iterators.size(chain.nextInterceptors()));
  }
}
