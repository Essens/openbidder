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

package com.google.openbidder.match;

import com.google.openbidder.api.interceptor.InterceptorAbortException;
import com.google.openbidder.api.interceptor.InterceptorChain;
import com.google.openbidder.api.match.MatchController;
import com.google.openbidder.api.match.MatchInterceptor;
import com.google.openbidder.api.match.MatchRequest;
import com.google.openbidder.api.match.MatchResponse;
import com.google.openbidder.api.platform.NoExchange;
import com.google.openbidder.api.testing.match.MatchTestUtil;
import com.google.openbidder.api.testing.match.TestMatchRequestBuilder;
import com.google.openbidder.api.testing.match.TestMatchResponseBuilder;
import com.google.openbidder.http.HttpRequest;
import com.google.openbidder.http.HttpResponse;
import com.google.openbidder.http.receiver.DefaultHttpReceiverContext;
import com.google.openbidder.http.request.StandardHttpRequest;
import com.google.openbidder.http.response.StandardHttpResponse;
import com.google.openbidder.match.interceptor.SimpleMatchInterceptor;

import com.codahale.metrics.MetricRegistry;

import org.junit.Test;

/**
 * Tests for {@link MatchRequestReceiver}.
 */
public class MatchRequestReceiverTest {

  private static final String DEFAULT_URI = "http://example.com";
  private static final HttpRequest DEFAULT_REQUEST = StandardHttpRequest.newBuilder()
      .setMethod("GET")
      .setUri(DEFAULT_URI)
      .build();

  @Test
  public void testReceiver() {
    MetricRegistry metricRegistry = new MetricRegistry();
    MatchController controller = MatchTestUtil.newMatchController(
        metricRegistry, new SimpleMatchInterceptor());
    TestReceiver receiver = new TestReceiver(metricRegistry, controller);
    receiver.receive(new DefaultHttpReceiverContext(
        DEFAULT_REQUEST, StandardHttpResponse.newBuilder()));
    controller.stopAsync().awaitTerminated();
  }

  @Test
  public void testHttpInterceptorAbortException() {
    MetricRegistry metricRegistry = new MetricRegistry();
    MatchController controller = MatchTestUtil.newMatchController(
        metricRegistry,
        new MatchInterceptor() {
          @Override public void execute(
              InterceptorChain<MatchRequest, MatchResponse> chain) {
            throw new InterceptorAbortException("It's Friday, too tired to work");
          }});
    TestReceiver receiver = new TestReceiver(metricRegistry, controller);
    receiver.receive(new DefaultHttpReceiverContext(
        DEFAULT_REQUEST, StandardHttpResponse.newBuilder()));
    controller.stopAsync().awaitTerminated();
  }

  static class TestReceiver extends MatchRequestReceiver {
    TestReceiver(MetricRegistry metricRegistry, MatchController controller) {
      super(NoExchange.INSTANCE, metricRegistry, controller);
    }

    @Override protected MatchRequest.Builder newRequest(HttpRequest httpRequest) {
      return TestMatchRequestBuilder.create().setHttpRequest(httpRequest);
    }

    @Override protected MatchResponse.Builder newResponse(
        MatchRequest request, HttpResponse.Builder httpResponse) {
      return TestMatchResponseBuilder.create().setHttpResponse(httpResponse);
    }
  }
}
