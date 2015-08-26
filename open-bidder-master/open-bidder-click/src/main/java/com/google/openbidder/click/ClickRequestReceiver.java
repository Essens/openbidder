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

package com.google.openbidder.click;

import com.google.openbidder.api.click.ClickController;
import com.google.openbidder.api.click.ClickRequest;
import com.google.openbidder.api.click.ClickResponse;
import com.google.openbidder.api.interceptor.InterceptorAbortException;
import com.google.openbidder.api.interceptor.RequestReceiver;
import com.google.openbidder.api.platform.Exchange;
import com.google.openbidder.http.HttpReceiverContext;
import com.google.openbidder.http.HttpRequest;
import com.google.openbidder.http.HttpResponse;
import com.google.openbidder.http.HttpStatusType;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import org.apache.http.HttpStatus;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * {@link com.google.openbidder.api.interceptor.RequestReceiver} for click requests.
 */
@Singleton
public class ClickRequestReceiver extends RequestReceiver<ClickController> {
  private final Meter noRedirectMeter;

  @Inject
  public ClickRequestReceiver(
      Exchange exchange, MetricRegistry metricRegistry, ClickController controller) {
    super(exchange, metricRegistry, controller);

    this.noRedirectMeter = buildMeter("no-redirect");
  }

  @Override
  public void receive(HttpReceiverContext ctx) {
    boolean unhandledException = true;

    try {
      Timer.Context timerContext = requestTimer().time();
      ClickRequest request = newRequest(ctx.httpRequest()).build();
      ClickResponse response = newResponse(ctx.httpResponse()).build();
      controller().onRequest(request, response);
      if (!(HttpStatusType.REDIRECT.contains(ctx.httpResponse().getStatusCode())
          && ctx.httpResponse().hasRedirectUri())) {
        noRedirectMeter.mark();
        logger.debug("No redirect location: {}", response);
        ctx.httpResponse().setStatusOk();
      } else {
        if (logger.isDebugEnabled()) {
          logger.debug("Redirect: {}", response.getRedirectLocation());
        }
        ctx.httpResponse().setStatusCode(HttpStatus.SC_MOVED_TEMPORARILY);
        ctx.httpResponse().setRedirectUri(response.getRedirectLocation());
      }
      successResponseMeter().mark();
      timerContext.close();
      unhandledException = false;
    } catch (InterceptorAbortException e) {
      unhandledException = false;
      ctx.httpResponse().setStatusOk();
      logger.error("InterceptorAbortException thrown", e);
      interceptorAbortMeter().mark();
    } finally {
      if (unhandledException) {
        interceptorOtherMeter().mark();
      }
    }
  }

  protected ClickRequest.Builder newRequest(HttpRequest httpRequest) {
    return ClickRequest.newBuilder()
        .setExchange(getExchange())
        .setHttpRequest(httpRequest);
  }

  protected ClickResponse.Builder newResponse(HttpResponse.Builder httpResponse) {
    return ClickResponse.newBuilder()
        .setExchange(getExchange())
        .setHttpResponse(httpResponse);
  }
}
