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

import com.google.common.util.concurrent.Service;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Root interface for controllers for some kind of interceptor.
 *
 * @param <Req> The request type for this controller
 * @param <Resp> The response type for this controller
 */
public interface InterceptorController<Req extends UserRequest, Resp extends UserResponse<Resp>>
    extends Service {

  /**
   * Runs the business logic for an interceptor request. All processing steps
   * are actually carried by {@link Interceptor}s, so this is only a root controller.
   *
   * @param request Request being processed
   * @param response Response produced by processing this request
   * @throws InterceptorAbortException signals that the interceptors wants to abort the request
   */
  void onRequest(Req request, Resp response);

  /**
   * Returns the interceptor chain.
   */
  List<? extends Interceptor<Req, Resp>> getInterceptors();

  /**
   * Returns a controller-managed resource specific to one of its interceptors,
   * or {@code null} if the desired resource is not available.
   */
  @Nullable
  <R> R getResource(Class<R> resourceType, Interceptor<Req, Resp> interceptor);
}
