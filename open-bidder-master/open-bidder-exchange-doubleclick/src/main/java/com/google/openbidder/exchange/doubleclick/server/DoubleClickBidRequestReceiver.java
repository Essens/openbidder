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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.net.MediaType;
import com.google.doubleclick.openrtb.MapperException;
import com.google.doubleclick.util.DoubleClickValidator;
import com.google.openbidder.api.bidding.BidController;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.interceptor.InterceptorAbortException;
import com.google.openbidder.bidding.BidRequestReceiver;
import com.google.openbidder.exchange.doubleclick.DoubleClickConstants;
import com.google.openbidder.http.HttpReceiverContext;
import com.google.openbidder.http.HttpRequest;
import com.google.openbidder.http.HttpResponse;
import com.google.openbidder.util.Clock;
import com.google.openrtb.mapper.OpenRtbMapper;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protos.adx.NetworkBid;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import org.apache.http.HttpStatus;

import java.io.IOException;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * {@link BidRequestReceiver} base class for DoubleClick Ad Exchange.
 */
@Singleton
public class DoubleClickBidRequestReceiver
    extends BidRequestReceiver<NetworkBid.BidRequest, NetworkBid.BidResponse> {
  private final DoubleClickSnippetProcessor snippetProcessor;
  private final OpenRtbMapper<
      NetworkBid.BidRequest, NetworkBid.BidResponse,
      NetworkBid.BidRequest.Builder, NetworkBid.BidResponse.Builder> mapper;
  private final DoubleClickValidator validator;
  private final Clock clock;
  private final Meter successResponseWithAdsMeter;
  private final Meter successResponseNoAdsMeter;
  private final ThreadLocal<CodedHelpers> codedHelpers = new ThreadLocal<CodedHelpers>() {
    @Override protected CodedHelpers initialValue() {
      return new CodedHelpers();
    }};

  @Inject
  public DoubleClickBidRequestReceiver(
      MetricRegistry metricRegistry,
      BidController controller,
      DoubleClickSnippetProcessor snippetProcessor,
      OpenRtbMapper<
          NetworkBid.BidRequest, NetworkBid.BidResponse,
          NetworkBid.BidRequest.Builder, NetworkBid.BidResponse.Builder> mapper,
      @Nullable DoubleClickValidator validator,
      Clock clock) {

    super(DoubleClickConstants.EXCHANGE, metricRegistry, controller);

    this.snippetProcessor = snippetProcessor;
    this.mapper = mapper;
    this.successResponseNoAdsMeter = buildMeter("success-response-no-ads");
    this.successResponseWithAdsMeter = buildMeter("success-response-with-ads");
    this.clock = checkNotNull(clock);
    this.validator = validator;
  }

  @Override
  public void receive(HttpReceiverContext ctx) {
    boolean unhandledException = true;

    Timer.Context timerContext = requestTimer().time();
    CodedHelpers helpers = codedHelpers.get();

    try {
      long start = clock.nanoTime();
      BidRequest request = newRequest(ctx.httpRequest(), helpers).build();

      if (logger.isDebugEnabled()) {
        logger.debug("DoubleClick request:\n{}", request.nativeRequest());
        logger.debug("Open Bidder Request:\n{}", request);
      }

      BidResponse response = newResponse(ctx.httpResponse()).build();
      NetworkBid.BidResponse.Builder responseBuilder = handleBidRequest(request, response);
      long end = clock.nanoTime();

      responseBuilder.setProcessingTimeMs((int) ((end - start) / 1000000));
      NetworkBid.BidResponse dcResponse = responseBuilder.build();

      if (logger.isDebugEnabled()) {
        logger.debug("Open Bidder response:\n{}", response);
        logger.debug("DoubleClick response:\n{}", dcResponse);
      }

      try {
        try {
          helpers.osw.setTarget(ctx.httpResponse().content());
          dcResponse.writeTo(helpers.cos);
        } finally {
          helpers.cos.flush();
        }
      } finally {
        helpers.osw.close();
      }
      ctx.httpResponse().setStatusOk();
      ctx.httpResponse().setMediaType(MediaType.OCTET_STREAM);
      unhandledException = false;
    } catch (InvalidProtocolBufferException e) {
      logger.error("Bad DoubleClick request: {}\nUnfinished message: {}\n",
          e, e.getUnfinishedMessage());
      ctx.httpResponse().setStatusCode(HttpStatus.SC_BAD_REQUEST);
    } catch (IOException e) {
      logger.error(e.toString());
      ctx.httpResponse().setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    } finally {
      if (unhandledException) {
        interceptorOtherMeter().mark();
      }
      timerContext.close();
    }
  }

  NetworkBid.BidResponse.Builder handleBidRequest(BidRequest request, BidResponse response) {
    NetworkBid.BidRequest dcRequest = request.nativeRequest();

    if (dcRequest.getIsPing()) {
      return NetworkBid.BidResponse.newBuilder();
    }

    try {
      controller().onRequest(request, response);
      dontSetCookies(response);

      NetworkBid.BidResponse.Builder dcResponse;
      if (response.nativeResponse() == null) {
        snippetProcessor.process(request.openRtb(), response.openRtb());
        dcResponse = mapper.toNativeBidResponse(request.openRtb(), response.openRtb().build());
      } else {
        dcResponse = (NetworkBid.BidResponse.Builder) response.nativeResponse();
      }

      if (validator != null) {
        validator.validate(dcRequest, dcResponse);
      }

      (dcResponse.getAdCount() == 0
          ? successResponseNoAdsMeter
          : successResponseWithAdsMeter
      ).mark();

      successResponseMeter().mark();
      return dcResponse;
    } catch (InterceptorAbortException e) {
      logger.error("InterceptorAbortException thrown", e);
      interceptorAbortMeter().mark();
      return NetworkBid.BidResponse.newBuilder();
    } catch (MapperException e) {
      logger.error(e.toString());
      return NetworkBid.BidResponse.newBuilder();
    }
  }

  protected BidRequest.Builder newRequest(HttpRequest httpRequest, CodedHelpers helpers)
      throws IOException {
    try {
      helpers.isw.setSource(httpRequest.content());
      try {
        NetworkBid.BidRequest dcRequest = NetworkBid.BidRequest.parseFrom(helpers.cis);
        return BidRequest.newBuilder()
            .setExchange(getExchange())
            .setHttpRequest(httpRequest)
            .setNativeRequest(dcRequest)
            .setRequest(mapper.toOpenRtbBidRequest(dcRequest));
      } finally {
        helpers.cis.resetSizeCounter();
      }
    } finally {
      helpers.isw.close();
    }
  }

  protected BidResponse.Builder newResponse(HttpResponse.Builder httpResponse) {
    return BidResponse.newBuilder()
        .setExchange(getExchange())
        .setHttpResponse(httpResponse);
  }

  static class CodedHelpers {
    final InputStreamWrapper isw = new InputStreamWrapper();
    final CodedInputStream cis = CodedInputStream.newInstance(isw);
    final OutputStreamWrapper osw = new OutputStreamWrapper();
    final CodedOutputStream cos = CodedOutputStream.newInstance(osw);
  }
}
