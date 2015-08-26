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

package com.google.openbidder.api.testing.bidding;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.inject.Module;
import com.google.openbidder.api.bidding.BidController;
import com.google.openbidder.api.bidding.BidInterceptor;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.snippet.SnippetMacros;
import com.google.openbidder.api.snippet.StandardSnippetProcessor;
import com.google.openbidder.api.testing.interceptor.InterceptorTestUtil;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid.Bid;

import com.codahale.metrics.MetricRegistry;

/**
 * Utilities for {@link BidInterceptor} unit tests.
 */
public final class BiddingTestUtil {
  public static final String DEFAULT_CALLBACK_URL = "http://localhost";
  private static final String DEFAULT_CLICK_URL =
      SnippetMacros.OB_CALLBACK_URL.key() + "/click";
  private static final String DEFAULT_IMPRESSION_URL =
      SnippetMacros.OB_CALLBACK_URL.key() + "/impression";

  private BiddingTestUtil() {
  }

  /**
   * Same as {@link #newBidController(MetricRegistry, BidInterceptor...)},
   * but provides a default {@link MetricRegistry}.
   */
  public static BidController newBidController(BidInterceptor... interceptors) {
    return newBidController(new MetricRegistry(), interceptors);
  }

  /**
   * Creates a {@link BidController} for some interceptor(s).
   * This can be used for "high-level" interceptor tests: when the method
   * {@link BidController#onRequest} is invoked, it will create the response, invoke all
   * the interceptor's lifecycle methods as expected (Callers should invoke the controller's
   * {@link com.google.common.util.concurrent.Service#startAsync()} before any use and call
   * {@link com.google.common.util.concurrent.Service#stopAsync()} after all testing is done),
   * and start the chain that calls {@link BidInterceptor#execute} on all provided interceptors.
   *
   * @param metricRegistry the {@link MetricRegistry}
   * @param interceptors the chain of interceptors used by this bidder
   * @return the new controller
   */
  public static BidController newBidController(
      MetricRegistry metricRegistry, BidInterceptor... interceptors) {
    BidController bidController = new BidController(
        ImmutableList.<BidInterceptor>copyOf(interceptors),
        metricRegistry);
    bidController.startAsync().awaitRunning();
    return bidController;
  }

  /**
   * Creates a controller, with support for Guice dependency injection on its interceptors.
   * You can provide a "root module", that will be installed in the user context's injector
   * (multiple modules are supported by installing them in the root module's configure()).
   * Unit tests may need to use this module to provide bindings for HttpTransport and/or for
   * GoogleCloudStorage; both are automatically provided by the system in normal deployments.
   *
   * @param userRootModule Optional "root" module.
   * @param interceptorClasses Classes for each interceptor, in chain order.
   * @return the controller
   */
  @SafeVarargs
  public static BidController newBidController(
      Module userRootModule,
      Class<? extends BidInterceptor>... interceptorClasses) {

    BidController controller = new BidController(
        InterceptorTestUtil.bind(userRootModule, interceptorClasses),
        new MetricRegistry());
    controller.startAsync().awaitRunning();
    return controller;
  }

  /**
   * Asserts that a bid response contains bids with specific prices, in order of insertion.
   */
  public static void assertBidAmounts(BidResponse response, double... bidAmounts) {
    assertEquals("Invalid number of bids", bidAmounts.length, Iterables.size(response.bids()));
    int iAmount = 0;

    for (Bid.Builder bid : response.bids()) {
      assertEquals("Mismatched bid price #" + iAmount,
          bidAmounts[iAmount++], bid.getPrice(), 1e-9);
    }
  }

  public static StandardSnippetProcessor newSnippetProcessor() {
    return new StandardSnippetProcessor(
        DEFAULT_CALLBACK_URL, DEFAULT_IMPRESSION_URL, DEFAULT_CLICK_URL);
  }
}
