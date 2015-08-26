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

package com.google.openbidder.exchange.doubleclick.testing;

import com.google.common.collect.ImmutableList;
import com.google.doubleclick.crypto.DoubleClickCrypto;
import com.google.doubleclick.openrtb.DoubleClickLinkMapper;
import com.google.doubleclick.openrtb.DoubleClickOpenRtbMapper;
import com.google.doubleclick.util.DoubleClickMetadata;
import com.google.inject.Module;
import com.google.openbidder.api.bidding.BidInterceptor;
import com.google.openbidder.api.match.MatchController;
import com.google.openbidder.api.match.MatchInterceptor;
import com.google.openbidder.api.testing.bidding.BiddingTestUtil;
import com.google.openbidder.api.testing.interceptor.InterceptorTestUtil;
import com.google.openbidder.bidding.BidModule;
import com.google.openbidder.config.bid.ClickUrl;
import com.google.openbidder.config.bid.ImpressionUrl;
import com.google.openbidder.exchange.doubleclick.server.DoubleClickBidRequestReceiver;
import com.google.openbidder.exchange.doubleclick.server.DoubleClickSnippetProcessor;
import com.google.openbidder.exchange.doubleclick.server.OpenBidderExtMapper;
import com.google.openbidder.http.HttpRequest;
import com.google.openbidder.http.Protocol;
import com.google.openbidder.http.request.StandardHttpRequest;
import com.google.openbidder.util.testing.FakeClock;
import com.google.openrtb.json.OpenRtbJsonFactory;
import com.google.openrtb.util.ProtoUtils;
import com.google.protobuf.ByteString;
import com.google.protos.adx.NetworkBid;

import com.codahale.metrics.MetricRegistry;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;

/**
 * DoubleClick Ad Exchange-specific utilities for unit tests.
 */
public final class DoubleClickTestUtil {
  public static final ByteString REQUEST_ID = ByteString.copyFromUtf8("01234567");
  public static final DoubleClickCrypto.Keys ZERO_KEYS;
  private static DoubleClickMetadata metadata;
  private static DoubleClickCrypto.Price priceCrypto;
  private static DoubleClickSnippetProcessor snippetProcessor;

  static {
    try {
      ZERO_KEYS = new DoubleClickCrypto.Keys(
          new SecretKeySpec(new byte[32], DoubleClickCrypto.KEY_ALGORITHM),
          new SecretKeySpec(new byte[32], DoubleClickCrypto.KEY_ALGORITHM));
    } catch (InvalidKeyException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  private DoubleClickTestUtil() {
  }

  public static synchronized DoubleClickMetadata getMetadata() {
    if (metadata == null) {
      metadata = new DoubleClickMetadata(new DoubleClickMetadata.ResourceTransport());
    }

    return metadata;
  }

  public static String zeroKeyEncoded() {
    return "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";
  }

  public static DoubleClickCrypto.Price zeroPriceCrypto () {
    if (priceCrypto == null) {
      priceCrypto = new DoubleClickCrypto.Price(ZERO_KEYS);
    }
    return priceCrypto;
  }

  public static String encryptedPrice(double priceValue) {
    return zeroPriceCrypto().encodePriceValue(
        priceValue, priceCrypto.createInitVector(new Date(0), 1));
  }

  public static DoubleClickSnippetProcessor newSnippetProcessor() {
    if (snippetProcessor == null) {
      snippetProcessor = new DoubleClickSnippetProcessor(
          BiddingTestUtil.DEFAULT_CALLBACK_URL,
          ImpressionUrl.DEFAULT,
          ClickUrl.DEFAULT);
    }
    return snippetProcessor;
  }

  public static DoubleClickBidRequestReceiver newReceiver(BidInterceptor... interceptors) {
    return newReceiver(
        newSnippetProcessor(),
        new MetricRegistry(),
        interceptors);
  }

  public static DoubleClickBidRequestReceiver newReceiver(
      DoubleClickSnippetProcessor snippetProcessor,
      MetricRegistry metricRegistry,
      BidInterceptor... interceptors) {

    return new DoubleClickBidRequestReceiver(
        metricRegistry,
        BiddingTestUtil.newBidController(metricRegistry, interceptors),
        snippetProcessor,
        new DoubleClickOpenRtbMapper(
            metricRegistry,
            getMetadata(),
            newOpenRtbJsonFactory(),
            new DoubleClickCrypto.Hyperlocal(ZERO_KEYS),
            ImmutableList.of(DoubleClickLinkMapper.INSTANCE, OpenBidderExtMapper.INSTANCE)),
        null,
        new FakeClock());
  }

  public static OpenRtbJsonFactory newOpenRtbJsonFactory() {
    return BidModule.registerObExt(OpenRtbJsonFactory.create());
  }

  public static HttpRequest newHttpRequest(NetworkBid.BidRequestOrBuilder bidRequest) {
    try {
      HttpRequest.Builder req = StandardHttpRequest.newBuilder()
          .setProtocol(Protocol.HTTP_1_1)
          .setMethod("POST")
          .setUri("http://localhost");
      ProtoUtils.built(bidRequest).writeTo(req.content());
      return req.build();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  @SafeVarargs
  public static <I extends MatchInterceptor> MatchController newMatchController(
      I... interceptors) {
    return newMatchController(new MetricRegistry(), interceptors);
  }

  @SafeVarargs
  public static <I extends MatchInterceptor> MatchController newMatchController(
      MetricRegistry metricRegistry, I... interceptors) {
    MatchController controller = new MatchController(
        ImmutableList.copyOf(interceptors),
        metricRegistry);
    controller.startAsync().awaitRunning();
    return controller;
  }

  @SafeVarargs
  public static MatchController newMatchController(
      Module userRootModule,
      Class<? extends MatchInterceptor>... interceptorClasses) {
    MatchController controller = new MatchController(
        InterceptorTestUtil.bind(userRootModule, interceptorClasses),
        new MetricRegistry());
    controller.startAsync().awaitRunning();
    return controller;
  }
}
