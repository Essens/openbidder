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
import com.google.openbidder.api.bidding.BidController;
import com.google.openbidder.api.bidding.BidInterceptor;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.testing.bidding.BiddingTestUtil;
import com.google.openbidder.api.testing.bidding.CountingBidInterceptor;
import com.google.openbidder.api.testing.bidding.TestBidRequestBuilder;
import com.google.openbidder.api.testing.bidding.TestBidResponseBuilder;

import com.codahale.metrics.MetricRegistry;

import org.junit.Test;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Tests for {@link CompositeInterceptor}.
 */
public class CompositeInterceptorTest {

  @Test
  public void testComposite() {
    final CountingBidInterceptor comp1 = new CountingBidInterceptor() {
      @Override public void execute(InterceptorChain<BidRequest, BidResponse> chain) {
        invokeCount = 1;
        chain.proceed();
      }
    };
    final CountingBidInterceptor comp2 = new CountingBidInterceptor() {
      @Override public void execute(InterceptorChain<BidRequest, BidResponse> chain) {
        invokeCount = comp1.invokeCount + 1;
        chain.proceed();
      }
    };
    final CountingBidInterceptor comp3 = new CountingBidInterceptor() {
      @Override public void execute(InterceptorChain<BidRequest, BidResponse> chain) {
        chain.proceed();
        invokeCount = comp2.invokeCount + 1;
      }
    };
    final CountingBidInterceptor regular = new CountingBidInterceptor() {
      @Override public void execute(InterceptorChain<BidRequest, BidResponse> chain) {
        invokeCount = comp3.invokeCount + 1;
        chain.proceed();
      }
    };
    TestCompositeInterceptor composite = new TestCompositeInterceptor(
        ImmutableList.of(comp1, comp2, comp3));

    assertEquals(3, composite.getComponentInterceptors().size());
    assertNotNull(composite.toString());

    BidController controller = BiddingTestUtil.newBidController(composite, regular);
    controller.onRequest(
        TestBidRequestBuilder.create().build(),
        TestBidResponseBuilder.create().build());
    controller.stopAsync().awaitTerminated();

    assertEquals(1, comp1.invokeCount);
    assertEquals(2, comp2.invokeCount);
    assertEquals(3, comp3.invokeCount);
    assertEquals(4, regular.invokeCount);
    assertEquals(1, comp1.postConstructCount);
    assertEquals(1, comp1.preDestroyCount);
    assertEquals(1, composite.invokeCount);
    assertEquals(1, composite.postConstructCount);
    assertEquals(1, composite.preDestroyCount);
  }

  private class TestCompositeInterceptor
      extends CompositeInterceptor<BidRequest, BidResponse>
      implements BidInterceptor {
    public int invokeCount;
    public int postConstructCount;
    public int preDestroyCount;

    public TestCompositeInterceptor(List<? extends BidInterceptor> componentInterceptors) {
      super(componentInterceptors, new MetricRegistry());
    }

    @Override
    public void execute(InterceptorChain<BidRequest, BidResponse> chain) {
      ++invokeCount;
      super.execute(chain);
    }

    @Override @PostConstruct
    public void postConstruct() {
      super.postConstruct();
      ++postConstructCount;
    }

    @Override @PreDestroy
    public void preDestroy() {
      ++preDestroyCount;
      super.preDestroy();
    }
  }
}
