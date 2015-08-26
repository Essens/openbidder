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

package com.google.openbidder.exchange.doubleclick.interceptor;

import com.google.common.base.Function;
import com.google.doubleclick.DcExt;
import com.google.openbidder.api.bidding.BidInterceptor;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.interceptor.InterceptorChain;
import com.google.openbidder.api.platform.CompatibleExchanges;
import com.google.openbidder.exchange.doubleclick.DoubleClickConstants;
import com.google.openbidder.exchange.doubleclick.config.DoubleClick;
import com.google.openrtb.OpenRtb.BidRequest.Imp;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid.Bid;
import com.google.protos.adx.NetworkBid;
import com.google.protos.adx.NetworkBid.BidResponse.Ad;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;

import javax.inject.Inject;

/**
 * Automatically handles <a href="https://support.google.com/adxbuyer/answer/3016708?hl=en">
 * SSL-enabled ads</a>.  Just add this to the beginning of your bid interceptor chain.
 * Your interceptors are still responsible for using the correct creatives/snippets for each
 * kind of request (SSL creative if an impression requires that); unless you only have
 * SSL creative, then no additional work is necessary but this interceptor is still
 * useful to add the SSL attribute if and only if it is required.
 */
@CompatibleExchanges(DoubleClick.NAME)
public class SSLBidInterceptor implements BidInterceptor {
  private final Counter sslAdded = new Counter();

  @Inject
  public SSLBidInterceptor(MetricRegistry metricRegistry) {
    metricRegistry.register(MetricRegistry.name(getClass(), "ssl-added"), sslAdded);
  }

  @Override
  public void execute(final InterceptorChain<BidRequest, BidResponse> chain) {
    chain.proceed();

    chain.response().updateBids(new Function<Bid.Builder, Boolean>() {
      @Override public Boolean apply(Bid.Builder bid) {
        Imp imp = chain.request().impWithId(bid.getImpid());
        if (imp == null) {
          return false; // Shouldn't happen... but not this interceptor's problem
        }

        NetworkBid.BidRequest.AdSlot adSlot = imp.getExtension(DcExt.adSlot);
        if (!adSlot.getExcludedAttributeList().contains(DoubleClickConstants.CREATIVE_NON_SSL)) {
          return false;
        }

        Ad ad = bid.getExtension(DcExt.ad);
        if (!ad.getAttributeList().contains(DoubleClickConstants.CREATIVE_SSL)) {
          bid.setExtension(DcExt.ad, ad.toBuilder()
              .addAttribute(DoubleClickConstants.CREATIVE_SSL).build());
          sslAdded.inc();
          return true;
        }

        return false;
      }});
  }
}
