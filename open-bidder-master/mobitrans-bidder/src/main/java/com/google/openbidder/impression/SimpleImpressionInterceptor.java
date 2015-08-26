
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
package com.google.openbidder.impression;

import com.google.openbidder.api.impression.ImpressionInterceptor;
import com.google.openbidder.api.impression.ImpressionRequest;
import com.google.openbidder.api.impression.ImpressionResponse;
import com.google.openbidder.api.interceptor.InterceptorChain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * (DoubleClick-specific) Impression interceptor that records spend in a
 * {@link SimpleBudget}.
 */
public class SimpleImpressionInterceptor implements ImpressionInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(SimpleImpressionInterceptor.class);

    @Override
    public void execute(InterceptorChain<ImpressionRequest, ImpressionResponse> chain) {

        logger.info("Impression Interceptor is being called !!!!!!!!!!!");
        logger.info("Impression Interceptor Price is: " + chain.request().getPriceValue());

        chain.proceed();
    }
}
