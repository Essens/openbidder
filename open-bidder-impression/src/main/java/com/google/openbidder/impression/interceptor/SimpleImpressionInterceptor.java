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

package com.google.openbidder.impression.interceptor;

import com.google.openbidder.api.impression.ImpressionInterceptor;
import com.google.openbidder.api.impression.ImpressionRequest;
import com.google.openbidder.api.impression.ImpressionResponse;
import com.google.openbidder.api.interceptor.InterceptorChain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * (DoubleClick-specific) A simple impression tracking interceptor.
 */
public class SimpleImpressionInterceptor implements ImpressionInterceptor {
  private static final Logger logger = LoggerFactory.getLogger(SimpleImpressionInterceptor.class);
  private static final NumberFormat fmt = new DecimalFormat("$0.######");

  @Override
  public void execute(InterceptorChain<ImpressionRequest, ImpressionResponse> chain) {
    ImpressionRequest request = chain.request();

    if (request.hasPrice()) {
      if (logger.isDebugEnabled()) {
        logger.debug("Received impression, price {}: {}", fmt.format(request.getPriceValue()), request);
      }
    } else {
      logger.warn("Received impression, price zero or unknown: {}", request);
    }

    chain.proceed();
  }
}
