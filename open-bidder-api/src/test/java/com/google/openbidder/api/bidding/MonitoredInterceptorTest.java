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

package com.google.openbidder.api.bidding;

import static org.junit.Assert.assertEquals;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.openbidder.api.interceptor.InterceptorChain;
import com.google.openbidder.api.testing.bidding.TestBidRequestBuilder;
import com.google.openbidder.api.testing.bidding.TestBidResponseBuilder;
import com.google.openbidder.api.testing.interceptor.InterceptorTestUtil;
import com.google.openrtb.OpenRtb.BidRequest.Imp;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.UniformReservoir;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;

/**
 * Shows how to use monitoring in interceptors.
 */
public class MonitoredInterceptorTest {
  private BidController controller;
  private MetricRegistry metricRegistry;

  @Before
  public void setUp() {
    metricRegistry = new MetricRegistry();
    BidController controller1 = new BidController(
        InterceptorTestUtil.bind(new Module() {
          @Override public void configure(Binder binder) {
              // OpenBidder uses the Yammer Metrics library for monitoring. The same MetricsRegistry
              // used by the server's built-in monitoring will also be injected in all interceptors.
              binder.bind(MetricRegistry.class).toInstance(metricRegistry);
            }
          }, MonitoredInterceptor.class),
        metricRegistry);
    controller1.startAsync().awaitRunning();
    controller = controller1;
  }

  @After
  public void tearDown() {
    if (controller != null) {
      controller.stopAsync().awaitTerminated();
    }
  }

  @Test
  public void testMonitoring() {
    BidRequest request = TestBidRequestBuilder.create()
        .setRequest("10", 1, 1, 1.0).build();
    BidResponse response = TestBidResponseBuilder.create().build();
    controller.onRequest(request, response);
    Histogram histo = metricRegistry.histogram(
        MetricRegistry.name(MonitoredInterceptor.class, "bidPrice"));
    assertEquals(1.0, histo.getSnapshot().getMean(), 1e-9);
  }

  public static class MonitoredInterceptor implements BidInterceptor {
    private final Histogram bidPriceHistogram;

    @Inject
    public MonitoredInterceptor(MetricRegistry metricRegistry) {
      this.bidPriceHistogram = metricRegistry.register(
          MetricRegistry.name(MonitoredInterceptor.class, "bidPrice"),
          new Histogram(new UniformReservoir()));
    }

    @Override public void execute(InterceptorChain<BidRequest, BidResponse> chain) {

      for (Imp imp : chain.request().imps()) {
        bidPriceHistogram.update((long) imp.getBidfloor());
      }

      chain.proceed();
    }
  }
}
