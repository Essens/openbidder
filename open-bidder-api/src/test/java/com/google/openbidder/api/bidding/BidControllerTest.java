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
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Service.State;
import com.google.openbidder.api.interceptor.InterceptorAbortException;
import com.google.openbidder.api.interceptor.InterceptorChain;
import com.google.openbidder.api.testing.bidding.BiddingTestUtil;
import com.google.openbidder.api.testing.bidding.CountingBidInterceptor;
import com.google.openbidder.api.testing.bidding.TestBidRequestBuilder;
import com.google.openbidder.api.testing.bidding.TestBidResponseBuilder;

import com.codahale.metrics.MetricRegistry;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Tests for {@link BidController}.
 */
public class BidControllerTest {
  protected TestInterceptor testInterceptor;
  protected CountingBidInterceptor countingInterceptor;

  @Before
  public void setUp() {
    testInterceptor = new TestInterceptor();
    countingInterceptor = new CountingBidInterceptor();
  }

  @Test
  public void testEmptyBid() {
    BidController controller = BiddingTestUtil.newBidController(
        testInterceptor, countingInterceptor);
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
  public void testNoExchangeData() {
    BiddingTestUtil.newBidController(testInterceptor, countingInterceptor).onRequest(
        TestBidRequestBuilder.create().build(),
        TestBidResponseBuilder.create().build());
  }

  @Test(expected = IllegalStateException.class)
  public void testNotRunning() {
    BidController controller = new BidController(
        ImmutableList.<BidInterceptor>of(testInterceptor, countingInterceptor),
        new MetricRegistry());
    controller.onRequest(
        TestBidRequestBuilder.create().build(),
        TestBidResponseBuilder.create().build());
  }

  @Test(expected = IllegalStateException.class)
  public void testFailedTermination() {
    BidController controller = BiddingTestUtil.newBidController(
        new TestInterceptorPreDestroyError());
    controller.stopAsync().awaitTerminated();
  }

  @Test(expected = InterceptorAbortException.class)
  public void testInterceptorAbortException() {
    BidInterceptor interceptor = Mockito.mock(BidInterceptor.class);
    doThrow(new InterceptorAbortException())
        .when(interceptor).execute(any(InterceptorChain.class));

    BidController controller = BiddingTestUtil.newBidController(interceptor);
    controller.onRequest(
        TestBidRequestBuilder.create().build(),
        TestBidResponseBuilder.create().build());
  }

  @Test(expected = IllegalStateException.class)
  public void testInterceptorPostConstructException() {
    BiddingTestUtil.newBidController(new TestInterceptorBadPostConstruct());
  }

  @Test
  public void testInterceptorPreDestroyException() {
    BidController controller = BiddingTestUtil.newBidController(
        new TestInterceptorBadPreDestroy());
    controller.stopAsync().awaitTerminated();
  }

  @Test
  public void testPostConstructWithParameters() {
    class PostConstructWithParameters extends TestInterceptor {
      boolean called = false;

      /** @param aParam Unused */
      @PostConstruct
      public void postConstructWithParams(int aParam) {
        called = true;
      }
    }

    PostConstructWithParameters interceptor = new PostConstructWithParameters();
    BidController controller = BiddingTestUtil.newBidController(interceptor);
    assertFalse(interceptor.called);
    controller.stopAsync().awaitTerminated();
  }

  @Test
  public void testPreDestroyWithParameters() {
    class PreDestroyWithParameters extends TestInterceptor {
      boolean called = false;

      /** @param aParam Unused */
      @PreDestroy
      public void preDestroyWithParams(int aParam) {
        called = true;
      }
    }

    PreDestroyWithParameters interceptor = new PreDestroyWithParameters();
    BidController controller = BiddingTestUtil.newBidController(interceptor);
    assertFalse(interceptor.called);
    controller.stopAsync().awaitTerminated();
  }

  @Test
  public void testMultiplePostConstructMethods() {
    class MultiplePostConstructMethods extends TestInterceptor {
      boolean called = false;

      @PostConstruct
      public void postConstruct1() {
        called = true;
      }

      @PostConstruct
      public void postConstruct2() {
        called = true;
      }
    }

    MultiplePostConstructMethods interceptor = new MultiplePostConstructMethods();
    BidController controller = BiddingTestUtil.newBidController(interceptor);
    assertFalse(interceptor.called);
    controller.stopAsync().awaitTerminated();
  }

  @Test
  public void testMultiplePreDestroyMethods() {
    class MultiplePreDestroyMethods extends TestInterceptor {
      boolean called = false;

      @PreDestroy
      public void preDestroy1() {
        called = true;
      }

      @PreDestroy
      public void preDestroy2() {
        called = true;
      }
    }

    MultiplePreDestroyMethods interceptor = new MultiplePreDestroyMethods();
    BidController controller = BiddingTestUtil.newBidController(interceptor);
    assertFalse(interceptor.called);
    controller.stopAsync().awaitTerminated();
  }

  public static final class TestInterceptorPreDestroyError extends TestInterceptor {
    @PreDestroy
    public void throwError() {
      throw new Error();
    }
  }

  public static final class TestInterceptorBadPostConstruct extends TestInterceptor {
    @PostConstruct
    public void postConstruct() {
      throw new NullPointerException();
    }
  }

  public static final class TestInterceptorBadPreDestroy extends TestInterceptor {
    @PreDestroy
    public void preDestroy() {
      throw new NullPointerException();
    }
  }
}
