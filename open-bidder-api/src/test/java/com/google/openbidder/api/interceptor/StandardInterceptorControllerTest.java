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
import static org.junit.Assert.assertNull;

import com.google.common.collect.ImmutableList;
import com.google.openbidder.api.bidding.BidInterceptor;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.bidding.TestInterceptor;
import com.google.openbidder.util.testing.TestUtil;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import org.junit.Test;

/**
 * Tests for {@link StandardInterceptorController}.
 */
public class StandardInterceptorControllerTest {

  @Test
  public void testController() {
    TestInterceptor interceptor = new TestInterceptor();
    MetricRegistry metricRegistry = new MetricRegistry();
    StandardInterceptorController<BidRequest, BidResponse> controller =
        new StandardInterceptorController<>(
            ImmutableList.<BidInterceptor>of(interceptor),
            metricRegistry);
    assertNotNull(controller.toString());
    assertEquals(1, controller.getInterceptors().size());
    assertNotNull(controller.getResource(Timer.class, interceptor));
    assertNotNull(metricRegistry.getGauges(new MetricFilter() {
      @Override public boolean matches(String name, Metric metric) {
        return name.equals(MetricRegistry.name(InterceptorController.class, "interceptors"));
      }}));
    assertNull(controller.getResource(String.class, interceptor));
  }

  @Test
  public void testInterceptorAbortException() {
    TestUtil.testCommonException(InterceptorAbortException.class);
  }
}
