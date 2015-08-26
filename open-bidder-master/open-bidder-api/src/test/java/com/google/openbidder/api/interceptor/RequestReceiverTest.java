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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import com.google.common.collect.ImmutableList;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.platform.NoExchange;
import com.google.openbidder.api.testing.bidding.TestBidResponseBuilder;
import com.google.openbidder.http.HttpReceiverContext;
import com.google.openbidder.http.cookie.StandardCookie;

import com.codahale.metrics.MetricRegistry;

import org.junit.Test;

/**
 * Tests for {@link RequestReceiver}.
 */
public class RequestReceiverTest {

  @Test
  public void testHandler() {
    MetricRegistry metricRegistry = new MetricRegistry();
    StandardInterceptorController<UserRequest, BidResponse> controller =
        new StandardInterceptorController<>(
            ImmutableList.<Interceptor<UserRequest, BidResponse>>of(),
            metricRegistry);
    RequestReceiver<?> receiver = new NoopRequestReceiver(metricRegistry, controller);
    assertSame(controller, receiver.controller());
    assertSame(metricRegistry, receiver.metricRegistry());
    assertNotNull(receiver.buildHistogram("histo"));
    assertNotNull(receiver.buildMeter("meter"));
    assertNotNull(receiver.buildTimer("timer"));
    receiver.successResponseMeter();
    receiver.interceptorAbortMeter();
    receiver.interceptorOtherMeter();
    receiver.requestTimer();
    UserResponse<?> response = TestBidResponseBuilder.create().build();
    receiver.dontSetCookies(response);
    response.httpResponse().addCookie(StandardCookie.create("name", "value"));
    receiver.dontSetCookies(response);
  }

  private static class NoopRequestReceiver
      extends RequestReceiver<InterceptorController<UserRequest, BidResponse>> {

    protected NoopRequestReceiver(
        MetricRegistry metricRegistry,
        InterceptorController<UserRequest, BidResponse> controller) {
      super(NoExchange.INSTANCE, metricRegistry, controller);
    }

    @Override
    public void receive(HttpReceiverContext ctx) {
    }
  }
}
