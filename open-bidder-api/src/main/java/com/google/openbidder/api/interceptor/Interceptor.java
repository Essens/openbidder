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

import javax.inject.Singleton;

/**
 * Root interceptor interface.
 * <p>
 * Interceptors may inject various Open Bidder provided dependencies using Guice.
 * <p>
 * Interceptors may annotate methods with {@link javax.annotation.PostConstruct} if they wish to
 * take some actions prior to handling requests and {@link javax.annotation.PreDestroy} if they wish
 * to take actions after the interceptor is handling requests and before the controller shuts down.
 *
 * @param <Req> The request type for this interceptor
 * @param <Resp> The response type for this interceptor
 */
@Singleton
public interface Interceptor<Req extends UserRequest, Resp extends UserResponse<Resp>> {

  /**
   * Executes the interceptor's action.  The interceptor is responsible for
   * continuing execution of the interceptor stack with the {@link InterceptorChain}.
   *
   * @param chain Execution chain
   * @throws InterceptorAbortException signals that the interceptors wants to abort the request
   */
  void execute(InterceptorChain<Req, Resp> chain);
}
