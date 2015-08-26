/*
 * Copyright 2014 Google Inc. All Rights Reserved.
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

package com.google.openbidder.bidding.interceptor;

import com.google.openbidder.api.bidding.BidInterceptor;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.interceptor.InterceptorChain;
import com.google.openrtb.util.OpenRtbValidator;

import javax.inject.Inject;

/**
 * An interceptor that validates the response produced by bid interceptors.
 * <p>
 * This interceptor is highly recommended (at least for test deployments) if you have
 * code that performs "pure" bidding logic, relying only on the OpenRTB model, not using
 * any exchange-specific extensions (or at least, not using anything that might impact validation).
 *
 * @see OpenRtbValidator
 */
public class OpenRtbValidationInterceptor implements BidInterceptor {
  private final OpenRtbValidator validator;

  @Inject
  public OpenRtbValidationInterceptor(OpenRtbValidator validator) {
    this.validator = validator;
  }

  @Override
  public void execute(final InterceptorChain<BidRequest, BidResponse> chain) {
    chain.proceed();

    validator.validate(chain.request().openRtb(), chain.response().openRtb());
  }
}
