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

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.google.inject.BindingAnnotation;
import com.google.openbidder.api.bidding.BidInterceptor;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.interceptor.InterceptorChain;
import com.google.openbidder.api.platform.CompatibleExchanges;
import com.google.openbidder.bidding.interceptor.RawMessageUtils;
import com.google.openbidder.exchange.doubleclick.config.DoubleClick;
import com.google.protobuf.TextFormat;
import com.google.protos.adx.NetworkBid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Inject;

/**
 * Bids by matching a "raw" request pattern, and sending a "raw" response.
 */
@CompatibleExchanges(DoubleClick.NAME)
public class RawBidInterceptor implements BidInterceptor {
  private static final Logger logger = LoggerFactory.getLogger(RawBidInterceptor.class);

  private final NetworkBid.BidRequest rawRequest;
  private final NetworkBid.BidResponse rawResponse;

  @Inject
  public RawBidInterceptor(
      @RawRequest NetworkBid.BidRequest rawRequest,
      @RawResponse NetworkBid.BidResponse rawResponse) {
    this.rawRequest = rawRequest;
    this.rawResponse = rawResponse;

    logger.debug("Matching request fragment: {}", TextFormat.shortDebugString(rawRequest));
    logger.debug("Response fragment: {}", TextFormat.shortDebugString(rawResponse));
  }

  @Override
  public void execute(InterceptorChain<BidRequest, BidResponse> chain) {
    NetworkBid.BidRequest req = chain.request().nativeRequest();

    if (RawMessageUtils.matches(req, rawRequest)) {
      NetworkBid.BidResponse.Builder resp = chain.response().nativeResponse();
      resp.mergeFrom(rawResponse);

      logger.debug("Merging native response");
    }

    chain.proceed();
  }

  @BindingAnnotation
  @Target({ FIELD, PARAMETER, METHOD })
  @Retention(RUNTIME)
  public @interface RawRequest {
    String DEFAULT = ""; // Will parse to empty protobuf: no filters
  }

  @BindingAnnotation
  @Target({ FIELD, PARAMETER, METHOD })
  @Retention(RUNTIME)
  public @interface RawResponse {
  }
}
