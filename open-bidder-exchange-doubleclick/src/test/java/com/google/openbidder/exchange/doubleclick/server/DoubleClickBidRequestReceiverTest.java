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

package com.google.openbidder.exchange.doubleclick.server;

import static org.junit.Assert.assertEquals;

import com.google.doubleclick.openrtb.NullDoubleClickOpenRtbMapper;
import com.google.openbidder.api.bidding.BidInterceptor;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.interceptor.InterceptorAbortException;
import com.google.openbidder.api.interceptor.InterceptorChain;
import com.google.openbidder.api.testing.bidding.BiddingTestUtil;
import com.google.openbidder.api.testing.bidding.CountingBidInterceptor;
import com.google.openbidder.exchange.doubleclick.testing.DoubleClickTestUtil;
import com.google.openbidder.http.HttpRequest;
import com.google.openbidder.http.HttpResponse;
import com.google.openbidder.http.Protocol;
import com.google.openbidder.http.receiver.DefaultHttpReceiverContext;
import com.google.openbidder.http.request.StandardHttpRequest;
import com.google.openbidder.http.response.StandardHttpResponse;
import com.google.openbidder.util.testing.FakeClock;
import com.google.protos.adx.NetworkBid;

import com.codahale.metrics.MetricRegistry;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * Tests for handling DoubleClick bid requests.
 */
public class DoubleClickBidRequestReceiverTest {
  private CountingBidInterceptor countingInterceptor;

  @Before
  public void setUp() {
    countingInterceptor = new CountingBidInterceptor();
  }

  @Test
  public void testHttpRequest() throws IOException {
    HttpRequest httpRequest = DoubleClickTestUtil.newHttpRequest(
        NetworkBid.BidRequest.newBuilder().setId(DoubleClickTestUtil.REQUEST_ID));
    HttpResponse.Builder httpResponse = StandardHttpResponse.newBuilder();
    DoubleClickBidRequestReceiver receiver =
        DoubleClickTestUtil.newReceiver(countingInterceptor);
    receiver.receive(new DefaultHttpReceiverContext(httpRequest, httpResponse));
    assertEquals(1, countingInterceptor.invokeCount);
    NetworkBid.BidResponse bidResponse =
        NetworkBid.BidResponse.parseFrom(httpResponse.build().content());
    assertEquals(1, bidResponse.getProcessingTimeMs());
  }

  @Test
  public void testHttpRequest_badRequest() {
    HttpRequest httpRequest = StandardHttpRequest.newBuilder()
        .setProtocol(Protocol.HTTP_1_1)
        .setMethod("POST")
        .setUri("http://localhost")
        .build();
    HttpResponse.Builder httpResponse = StandardHttpResponse.newBuilder();
    DoubleClickTestUtil.newReceiver()
        .receive(new DefaultHttpReceiverContext(httpRequest, httpResponse));
    assertEquals(HttpStatus.SC_BAD_REQUEST, httpResponse.getStatusCode());
  }

  @Test
  public void testHttpInterceptorAbortException() {
    HttpRequest httpRequest = DoubleClickTestUtil.newHttpRequest(TestData.newRequest(false));
    HttpResponse.Builder httpResponse = StandardHttpResponse.newBuilder();
    DoubleClickBidRequestReceiver receiver = DoubleClickTestUtil.newReceiver(new BidInterceptor() {
      @Override public void execute(InterceptorChain<BidRequest, BidResponse> chain) {
        throw new InterceptorAbortException("It's Friday, too tired to work");
      }});
    receiver.receive(new DefaultHttpReceiverContext(httpRequest, httpResponse));
    assertEquals(HttpStatus.SC_OK, httpResponse.getStatusCode());
  }

  @Test(expected = NullPointerException.class)
  public void testHttpInterceptorRuntimeException() {
    HttpRequest httpRequest = DoubleClickTestUtil.newHttpRequest(TestData.newRequest(false));
    HttpResponse.Builder httpResponse = StandardHttpResponse.newBuilder();
    DoubleClickBidRequestReceiver receiver = DoubleClickTestUtil.newReceiver(new BidInterceptor() {
      @Override public void execute(InterceptorChain<BidRequest, BidResponse> chain) {
        throw new NullPointerException();
      }});
    receiver.receive(new DefaultHttpReceiverContext(httpRequest, httpResponse));
  }

  @Test
  public void testPingRequest() throws IOException {
    HttpRequest httpRequest = DoubleClickTestUtil.newHttpRequest(NetworkBid.BidRequest.newBuilder()
        .setId(DoubleClickTestUtil.REQUEST_ID)
        .setIsPing(true));
    HttpResponse.Builder httpResponse = StandardHttpResponse.newBuilder();
    DoubleClickBidRequestReceiver receiver = DoubleClickTestUtil.newReceiver();
    receiver.receive(new DefaultHttpReceiverContext(httpRequest, httpResponse));
    assertEquals(0, countingInterceptor.invokeCount);
    assertEquals(NetworkBid.BidResponse.parseFrom(httpResponse.build().content()),
        NetworkBid.BidResponse.newBuilder().setProcessingTimeMs(1).build());
  }

  @Test
  public void testHttpRequestNativeOnly() {
    HttpRequest httpRequest = DoubleClickTestUtil.newHttpRequest(
        NetworkBid.BidRequest.newBuilder().setId(DoubleClickTestUtil.REQUEST_ID));
    HttpResponse.Builder httpResponse = StandardHttpResponse.newBuilder();
    MetricRegistry metricRegistry = new MetricRegistry();
    DoubleClickBidRequestReceiver receiver = new DoubleClickBidRequestReceiver(
        metricRegistry,
        BiddingTestUtil.newBidController(metricRegistry, countingInterceptor),
        DoubleClickTestUtil.newSnippetProcessor(),
        NullDoubleClickOpenRtbMapper.INSTANCE,
        null,
        new FakeClock());
    receiver.receive(new DefaultHttpReceiverContext(httpRequest, httpResponse));
    assertEquals(1, countingInterceptor.invokeCount);
  }
}
