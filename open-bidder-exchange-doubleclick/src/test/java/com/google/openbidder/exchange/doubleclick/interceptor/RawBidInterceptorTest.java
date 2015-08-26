/*
 * Copyright 2015 Google Inc. All Rights Reserved.
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

package com.google.openbidder.exchange.doubleclick.interceptor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.testing.interceptor.NoopInterceptorChain;
import com.google.openbidder.exchange.doubleclick.testing.TestBidRequestBuilder;
import com.google.openbidder.exchange.doubleclick.testing.TestBidResponseBuilder;
import com.google.protobuf.ByteString;
import com.google.protos.adx.NetworkBid;

import com.codahale.metrics.MetricRegistry;

import org.junit.Test;

/**
 * Tests for {@link RawBidInterceptor}.
 */
public class RawBidInterceptorTest {

  @Test
  public void testOk() {
    assertTrue(test(NetworkBid.BidRequest.newBuilder().buildPartial()));
    assertTrue(test(NetworkBid.BidRequest.newBuilder()
        .setId(ByteString.copyFromUtf8("A")).buildPartial()));
    assertFalse(test(NetworkBid.BidRequest.newBuilder()
        .setId(ByteString.copyFromUtf8("B")).buildPartial()));
  }

  boolean test(NetworkBid.BidRequest rawRequest) {
    RawBidInterceptor interceptor = new RawBidInterceptor(
        rawRequest,
        NetworkBid.BidResponse.newBuilder().setDebugString("this works").buildPartial());
    BidRequest request = TestBidRequestBuilder.create()
        .setNativeRequest(NetworkBid.BidRequest.newBuilder()
            .setId(ByteString.copyFromUtf8("A"))
            .setIsPing(false)
            .buildPartial())
        .build();
    BidResponse response = TestBidResponseBuilder.create().build();
    NoopInterceptorChain.execute(interceptor, request, response, new MetricRegistry());
    NetworkBid.BidResponse.Builder resp = response.nativeResponse();
    return resp.hasDebugString();
  }
}
