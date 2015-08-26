/*
 * Copyright 2012 Google Inc. All Rights Reserved.
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
import static org.junit.Assert.assertNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Service.State;
import com.google.openbidder.api.testing.bidding.CountingBidInterceptor;
import com.google.openbidder.api.testing.bidding.TestBidRequestBuilder;
import com.google.openbidder.api.testing.bidding.TestBidResponseBuilder;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;

import org.junit.Before;
import org.junit.Test;

import java.lang.management.ManagementFactory;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

/**
 * Tests for {@link BidController} monitoring.
 */
public class MonitoredBidControllerTest {
  protected TestInterceptor testInterceptor;
  protected CountingBidInterceptor countingInterceptor;
  protected BidController controller;
  private ObjectName name;

  @Before
  public void setUp() throws MalformedObjectNameException {
    testInterceptor = new TestInterceptor();
    countingInterceptor = new CountingBidInterceptor();
    name = new ObjectName(
        "metrics:name=com.google.openbidder.api.bidding.BidController.interceptors");
  }

  protected BidController initController() {
    MetricRegistry metricRegistry = new MetricRegistry();
    JmxReporter jmxReporter = JmxReporter.forRegistry(metricRegistry).build();
    jmxReporter.start();
    controller = new BidController(
        ImmutableList.of(testInterceptor, countingInterceptor),
        metricRegistry);
    controller.startAsync().awaitRunning();
    assertEquals(State.RUNNING, controller.state());
    return controller;
  }

  @Test
  public void testEmptyBid() {
    initController();
    assertEquals(1, countingInterceptor.postConstructCount);
    BidResponse response = TestBidResponseBuilder.create().build();
    controller.onRequest(TestBidRequestBuilder.create().build(), response);
    assertEquals(0, response.openRtb().getSeatbidCount());
    controller.stopAsync().awaitTerminated();
    assertEquals(State.TERMINATED, controller.state());
    controller.stopAsync().awaitTerminated();
    assertEquals(State.TERMINATED, controller.state());
    assertEquals(1, countingInterceptor.preDestroyCount);
  }

  @Test
  public void testMonitoring() throws AttributeNotFoundException, InstanceNotFoundException,
      MBeanException, ReflectionException {
    initController().onRequest(
        TestBidRequestBuilder.create().build(),
        TestBidResponseBuilder.create().build());
    MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    assertNotNull(mbs.getAttribute(name, "Value"));
  }
}
