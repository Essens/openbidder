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

package com.google.openbidder.match.interceptor;

import com.google.openbidder.api.interceptor.InterceptorChain;
import com.google.openbidder.api.match.MatchInterceptor;
import com.google.openbidder.api.match.MatchRequest;
import com.google.openbidder.api.match.MatchResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * No-op interceptor that will simply cause the correct pixel match response to be sent back.
 */
public class SimpleMatchInterceptor implements MatchInterceptor {

  private static final Logger logger = LoggerFactory.getLogger(SimpleMatchInterceptor.class);

  @Override
  public void execute(InterceptorChain<MatchRequest, MatchResponse> chain) {
    chain.proceed();
    logger.info("Match request {}, response {}", chain.request(), chain.response());
  }
}
