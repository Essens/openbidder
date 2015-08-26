/*
 * Copyright 2012 Google Inc. All Rights Reserved.
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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;

import com.codahale.metrics.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * Object used by the controller to coordinate the invocation to a chain of interceptors.
 *
 * @param <Req> The request type for this chain
 * @param <Resp> The response type for this chain
 */
public class InterceptorChain<Req extends UserRequest, Resp extends UserResponse<Resp>> {
  private static final Logger logger = LoggerFactory.getLogger(InterceptorChain.class);

  private final InterceptorController<Req, Resp> controller;
  private final Req request;
  private final Resp response;
  private final Iterator<? extends Interceptor<Req, Resp>> interceptors;

  protected InterceptorChain(InterceptorController<Req, Resp> controller,
      Req request, Resp response) {
    this.request = checkNotNull(request);
    this.response = checkNotNull(response);
    this.controller = checkNotNull(controller);
    this.interceptors = controller.getInterceptors().iterator();
  }

  public static <Req extends UserRequest, Resp extends UserResponse<Resp>>
  InterceptorChain<Req, Resp> create(
      InterceptorController<Req, Resp> controller, Req request, Resp response) {
    return new InterceptorChain<>(controller, request,  response);
  }

  /**
   * Invoked by interceptors, to dispatch execution to the next interceptor in the chain.
   *
   * @throws InterceptorAbortException signals that the interceptors wants to abort the request
   */
  public final void proceed() {
    if (interceptors.hasNext()) {
      Interceptor<Req, Resp> interceptor = interceptors.next();

      if (logger.isDebugEnabled()) {
        logger.debug(">> Interceptor: {}", interceptor.getClass().getSimpleName());
      }

      Timer timer = controller.getResource(Timer.class, interceptor);
      Timer.Context timerContext = timer == null ? null : timer.time();

      call(interceptor);

      if (logger.isDebugEnabled()) {
        logger.debug("<< Interceptor: {}", interceptor.getClass().getSimpleName());
      }

      if (timerContext != null) {
        timerContext.close();
      }
    }
  }

  protected <I extends Interceptor<Req, Resp>> void call(I interceptor) {
    interceptor.execute(this);
  }

  /**
   * Returns the request object.
   */
  public final Req request() {
    return request;
  }

  /**
   * Returns the response object.
   */
  public final Resp response() {
    return response;
  }

  /**
   * Returns the iterator of the remaining interceptors for this chain. You could use this to
   * create delegate chains, implement wrappers, skip or rearrange remaining interceptors, etc.
   */
  public Iterator<? extends Interceptor<Req, Resp>> nextInterceptors() {
    return interceptors;
  }

  protected ToStringHelper toStringHelper() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("request", request)
        .add("response", response);
  }

  @Override
  public final String toString() {
    return toStringHelper().toString();
  }
}
