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
import com.google.openbidder.api.interceptor.RequestReceiver;
import com.google.openbidder.api.match.MatchController;
import com.google.openbidder.api.match.MatchRequest;
import com.google.openbidder.api.match.MatchResponse;
import com.google.openbidder.api.platform.Exchange;
import com.google.openbidder.http.HttpReceiverContext;
import com.google.openbidder.http.HttpRequest;
import com.google.openbidder.http.HttpResponse;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import javax.inject.Inject;

/**
 * {@link RequestReceiver} for pixel and cookie matching requests.
 */
public abstract class MatchRequestReceiver extends RequestReceiver<MatchController> {

  @Inject
  public MatchRequestReceiver(
      Exchange exchange,
      MetricRegistry metricRegistry,
      MatchController controller) {
    super(exchange, metricRegistry, controller);
  }

  @Override
  public void receive(HttpReceiverContext ctx) {
    boolean unhandledException = true;

    try {
      Timer.Context timerContext = requestTimer().time();
      @SuppressWarnings("unchecked")
      MatchRequest request = newRequest(ctx.httpRequest()).build();
      @SuppressWarnings("unchecked")
      MatchResponse response = newResponse(request, ctx.httpResponse()).build();
      controller().onRequest(request, response);
      successResponseMeter().mark();
      timerContext.close();
      unhandledException = false;
    } catch (InterceptorAbortException e) {
      ctx.httpResponse().setStatusOk();
      logger.error("InterceptorAbortException thrown", e);
      interceptorAbortMeter().mark();
    } finally {
      if (unhandledException) {
        interceptorOtherMeter().mark();
      }
    }
  }

  protected abstract MatchRequest.Builder newRequest(HttpRequest httpRequest);

  protected abstract MatchResponse.Builder newResponse(
      MatchRequest request, HttpResponse.Builder httpResponse);
}
