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

package com.google.openbidder.bidding;

import com.google.openbidder.api.bidding.BidController;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.platform.NoExchange;
import com.google.openbidder.api.testing.bidding.BiddingTestUtil;
import com.google.openbidder.http.HttpReceiverContext;
import com.google.openbidder.http.HttpRequest;
import com.google.openbidder.http.receiver.DefaultHttpReceiverContext;
import com.google.openbidder.http.request.StandardHttpRequest;
import com.google.openbidder.http.response.StandardHttpResponse;

import com.codahale.metrics.MetricRegistry;

import org.junit.Test;

/**
 * Tests for {@link BidRequestReceiver}.
 */
public class BidRequestReceiverTest {

  private static final String DEFAULT_URI = "http://example.com";
  private static final HttpRequest DEFAULT_REQUEST = StandardHttpRequest.newBuilder()
      .setMethod("GET")
      .setUri(DEFAULT_URI)
      .build();

  @Test
  public void testReceiver() {
    MetricRegistry metricRegistry = new MetricRegistry();
    TestReceiver receiver = new TestReceiver(
        metricRegistry, BiddingTestUtil.newBidController(metricRegistry));
    receiver.receive(new DefaultHttpReceiverContext(
        DEFAULT_REQUEST, StandardHttpResponse.newBuilder()));
  }

  static class TestReceiver extends BidRequestReceiver<BidRequest, BidResponse> {
    TestReceiver(MetricRegistry metricRegistry, BidController controller) {
      super(NoExchange.INSTANCE, metricRegistry, controller);
    }

    @Override public void receive(HttpReceiverContext ctx) {
    }
  }
}
