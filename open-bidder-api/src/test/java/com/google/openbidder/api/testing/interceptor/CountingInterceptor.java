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

package com.google.openbidder.api.testing.interceptor;

import static org.junit.Assert.assertNotNull;

import com.google.common.base.MoreObjects;
import com.google.openbidder.api.interceptor.Interceptor;
import com.google.openbidder.api.interceptor.InterceptorChain;
import com.google.openbidder.api.interceptor.UserRequest;
import com.google.openbidder.api.interceptor.UserResponse;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * An interceptor that monitors its own invocations.
 */
public class CountingInterceptor<Req extends UserRequest, Resp extends UserResponse<Resp>>
    implements Interceptor<Req, Resp> {

  public int invokeCount;
  public int postConstructCount;
  public int preDestroyCount;

  @Override
  public void execute(InterceptorChain<Req, Resp> chain) {
    assertNotNull(chain);
    assertNotNull(chain.request());
    assertNotNull(chain.response());
    ++invokeCount;
    chain.proceed();
  }

  @PostConstruct
  public void postConstruct() {
    ++postConstructCount;
  }

  @PreDestroy
  public void preDestroy() {
    ++preDestroyCount;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("invoke", invokeCount)
        .add("postConstruct", postConstructCount)
        .add("preDestroy", preDestroyCount)
        .toString();
  }
}
