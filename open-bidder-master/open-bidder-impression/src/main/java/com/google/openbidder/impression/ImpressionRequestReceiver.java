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

package com.google.openbidder.impression;

import com.google.common.net.MediaType;
import com.google.openbidder.api.impression.ImpressionController;
import com.google.openbidder.api.impression.ImpressionRequest;
import com.google.openbidder.api.impression.ImpressionResponse;
import com.google.openbidder.api.interceptor.InterceptorAbortException;
import com.google.openbidder.api.interceptor.RequestReceiver;
import com.google.openbidder.api.platform.Exchange;
import com.google.openbidder.http.HttpReceiverContext;
import com.google.openbidder.http.HttpRequest;
import com.google.openbidder.http.HttpResponse;
import com.google.openbidder.http.util.PixelImage;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * {@link com.google.openbidder.api.interceptor.RequestReceiver} for impression requests.
 */
@Singleton
public class ImpressionRequestReceiver extends RequestReceiver<ImpressionController> {

  @Inject
  public ImpressionRequestReceiver(
      Exchange exchange, MetricRegistry metricRegistry, ImpressionController controller) {
    super(exchange, metricRegistry, controller);
  }

  @Override
  public void receive(HttpReceiverContext ctx) {
    boolean unhandledException = true;

    try {
      Timer.Context timerContext = requestTimer().time();
      ImpressionRequest request = newRequest(ctx.httpRequest());
      ImpressionResponse response = newResponse(ctx.httpResponse());
      controller().onRequest(request, response);
      successResponseMeter().mark();
      timerContext.close();
      unhandledException = false;
    } catch (InterceptorAbortException e) {
      unhandledException = false;
      logger.error("InterceptorAbortException thrown", e);
      interceptorAbortMeter().mark();
    } finally {
      if (unhandledException) {
        interceptorOtherMeter().mark();
      }
    }
    ctx.httpResponse().setMediaType(MediaType.GIF);
    try {
      PixelImage.write(ctx.httpResponse().content());
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  protected ImpressionRequest newRequest(HttpRequest httpRequest) {
    return ImpressionRequest.newBuilder()
        .setExchange(getExchange())
        .setHttpRequest(httpRequest)
        .build();
  }

  protected ImpressionResponse newResponse(HttpResponse.Builder httpResponse) {
    return ImpressionResponse.newBuilder()
        .setExchange(getExchange())
        .setHttpResponse(httpResponse)
        .build();
  }
}
