/*
 * Copyright 2014 Google Inc. All Rights Reserved.
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

package com.google.openbidder.exchange.doubleclick.impression;

import static org.junit.Assert.assertEquals;

import com.google.doubleclick.crypto.DoubleClickCrypto.Price;
import com.google.openbidder.api.impression.ImpressionRequest;
import com.google.openbidder.api.impression.ImpressionResponse;
import com.google.openbidder.api.interceptor.InterceptorChain;
import com.google.openbidder.api.platform.NoExchange;
import com.google.openbidder.api.testing.impression.CountingImpressionInterceptor;
import com.google.openbidder.api.testing.impression.ImpressionTestUtil;
import com.google.openbidder.config.impression.PriceName;
import com.google.openbidder.exchange.doubleclick.testing.DoubleClickTestUtil;
import com.google.openbidder.http.HttpRequest;
import com.google.openbidder.http.HttpResponse;
import com.google.openbidder.http.Protocol;
import com.google.openbidder.http.receiver.DefaultHttpReceiverContext;
import com.google.openbidder.http.request.StandardHttpRequest;
import com.google.openbidder.http.response.StandardHttpResponse;

import com.codahale.metrics.MetricRegistry;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for handling DoubleClick bid requests.
 */
public class DoubleClickImpressionRequestReceiverTest {
  private DoubleClickCountingImpressionInterceptor countingInterceptor;

  @Before
  public void setUp() {
    countingInterceptor = new DoubleClickCountingImpressionInterceptor();
  }

  @Test
  public void testHttpRequest() {
    Price priceCrypto = DoubleClickTestUtil.zeroPriceCrypto();
    HttpRequest httpRequest = StandardHttpRequest.newBuilder()
        .setProtocol(Protocol.HTTP_1_1)
        .setMethod("GET")
        .setUri("http://localhost?" + PriceName.DEFAULT
            + "=" + priceCrypto.encodePriceValue(0.1, new byte[16]))
        .build();
    HttpResponse.Builder httpResponse = StandardHttpResponse.newBuilder();
    MetricRegistry metricRegistry = new MetricRegistry();
    DoubleClickImpressionRequestReceiver receiver = new DoubleClickImpressionRequestReceiver(
        NoExchange.INSTANCE,
        metricRegistry,
        ImpressionTestUtil.newImpressionController(metricRegistry, countingInterceptor),
        priceCrypto,
        PriceName.DEFAULT);
    receiver.receive(new DefaultHttpReceiverContext(httpRequest, httpResponse));
    assertEquals(1, countingInterceptor.invokeCount);
  }

  static class DoubleClickCountingImpressionInterceptor extends CountingImpressionInterceptor {
    @Override public void execute(InterceptorChain<ImpressionRequest, ImpressionResponse> chain) {
      super.execute(chain);
      assertEquals(0.1, chain.request().getPriceValue(), 1e-9);
    }
  }
}
