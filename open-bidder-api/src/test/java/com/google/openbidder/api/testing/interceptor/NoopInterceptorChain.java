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

package com.google.openbidder.api.testing.interceptor;

import com.google.common.collect.ImmutableList;
import com.google.openbidder.api.interceptor.Interceptor;
import com.google.openbidder.api.interceptor.InterceptorChain;
import com.google.openbidder.api.interceptor.StandardInterceptorController;
import com.google.openbidder.api.interceptor.UserRequest;
import com.google.openbidder.api.interceptor.UserResponse;

import com.codahale.metrics.MetricRegistry;

/**
 * Interceptor chain that doesn't proceed to the next interceptor.
 *
 * @param <Req> The request type for this chain
 * @param <Resp> The response type for this chain
 */
public class NoopInterceptorChain<Req extends UserRequest, Resp extends UserResponse<Resp>>
    extends InterceptorChain<Req, Resp> {
  public NoopInterceptorChain(Req request, Resp response, MetricRegistry metricRegistry) {
    super(new StandardInterceptorController<>(
        ImmutableList.<Interceptor<Req, Resp>>of(), metricRegistry),
        request,
        response);
  }

  public static <Req extends UserRequest, Resp extends UserResponse<Resp>>
    InterceptorChain<Req, Resp> create(Req request, Resp response, MetricRegistry metricRegistry) {
    return new NoopInterceptorChain<>(request,  response, metricRegistry);
  }

  public static <
      Req extends UserRequest,
      Resp extends UserResponse<Resp>,
      I extends Interceptor<Req, Resp>>
  void execute(I interceptor, Req request, Resp response, MetricRegistry metricRegistry) {

    interceptor.execute(create(request, response, metricRegistry));
  }
}
