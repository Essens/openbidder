/*
 * Copyright 2015 Google Inc. All Rights Reserved.
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

import com.google.common.base.Predicates;
import com.google.openbidder.api.bidding.BidInterceptor;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.interceptor.InterceptorChain;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid.Bid;

/**
 * A {@link com.google.openbidder.api.bidding.BidInterceptor} that removes all bids.
 * Useful for being placed at the top of an interceptor stack to test the latency of your
 * interceptors, but ensuring that no bids are returned.
 */
public class NoBidInterceptor implements BidInterceptor {

  @Override
  public void execute(InterceptorChain<BidRequest, BidResponse> chain) {
    chain.proceed();
    chain.response().filterBids(Predicates.<Bid.Builder>alwaysFalse());
  }
}
