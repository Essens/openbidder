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

package com.google.openbidder.api.interceptor;

import com.google.openbidder.api.platform.Exchange;
import com.google.openbidder.http.HttpReceiver;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.UniformReservoir;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic request receiver: the object that takes care of all transport-neutral activities
 * to handle some kind of request. Subclasses may extend functionality for different types
 * of requests, and/or for particular needs of specific exchanges.
 */
public abstract class RequestReceiver<C extends InterceptorController<?, ?>>
implements HttpReceiver {

  protected final Logger logger = LoggerFactory.getLogger(getClass());
  private final C controller;
  private final MetricRegistry metricRegistry;
  private final Exchange exchange;
  private final Meter successResponseMeter;
  private final Meter interceptorAbortMeter;
  private final Meter interceptorOtherMeter;
  private final Timer requestTimer;

  protected RequestReceiver(Exchange exchange, MetricRegistry metricRegistry, C controller) {
    this.exchange = exchange;
    this.controller = controller;
    this.metricRegistry = metricRegistry;

    successResponseMeter = buildMeter("success-response");
    interceptorAbortMeter = buildMeter("interceptor-abort-exceptions");
    interceptorOtherMeter = buildMeter("interceptor-exceptions");
    requestTimer = buildTimer("request-timer");
  }

  /**
   * Create a {@link Timer} for this receiver.
   */
  protected Timer buildTimer(String name) {
    return metricRegistry.register(MetricRegistry.name(getClass(), name), new Timer());
  }

  /**
   * Create a {@link Meter} for this receiver.
   */
  protected Meter buildMeter(String name) {
    return metricRegistry.register(MetricRegistry.name(getClass(), name), new Meter());
  }

  protected Histogram buildHistogram(String name) {
    return metricRegistry.register(
        MetricRegistry.name(getClass(), name), new Histogram(new UniformReservoir()));
  }

  /**
   * Does NOT sets the {@link UserResponse}'s cookies in the HTTP response.
   * Call this when the response is not supposed to have any cookies.
   */
  protected void dontSetCookies(UserResponse<?> response) {
    if (!response.httpResponse().getCookies().isEmpty()) {
      logger.warn("Output cookies are not supported for this endpoint");
    }
  }

  protected final C controller() {
    return controller;
  }

  protected final MetricRegistry metricRegistry() {
    return metricRegistry;
  }

  protected Exchange getExchange() {
    return exchange;
  }

  protected final Meter successResponseMeter() {
    return successResponseMeter;
  }

  protected final Meter interceptorAbortMeter() {
    return interceptorAbortMeter;
  }

  protected final Meter interceptorOtherMeter() {
    return interceptorOtherMeter;
  }

  protected final Timer requestTimer() {
    return requestTimer;
  }
}
