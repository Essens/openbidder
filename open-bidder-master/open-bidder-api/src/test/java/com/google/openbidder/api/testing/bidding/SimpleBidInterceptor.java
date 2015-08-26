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

package com.google.openbidder.api.testing.bidding;

import com.google.openbidder.api.bidding.BidInterceptor;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.interceptor.InterceptorChain;
import com.google.openrtb.OpenRtb.BidRequest.Impression;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid.Bid;

public class SimpleBidInterceptor implements BidInterceptor {
  private double price = 1.0;
  private String snippet = "<html/>";

  public SimpleBidInterceptor setPrice(double price) {
    this.price = price;
    return this;
  }

  public SimpleBidInterceptor setSnippet(String snippet) {
    this.snippet = snippet;
    return this;
  }

  @Override public void execute(InterceptorChain<BidRequest, BidResponse> chain) {
    for (Impression imp : chain.request().openRtb().getImpList()) {
      chain.response().addBid(Bid.newBuilder()
          .setId(imp.getId())
          .setImpid(imp.getId())
          .setPrice(price)
          .setAdm(snippet));
    }

    chain.proceed();
  }
}
