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
import com.google.protos.adx.NetworkBid.BidResponse.Ad;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;

import javax.inject.Inject;

/**
 * Automatically handles <a href="https://developers.google.com/ad-exchange/rtb/adx-video-guide">
 * Video ads</a>.  Just add this to the beginning of your bid interceptor chain.
 * Your interceptors are still responsible for using the correct kind of video ads for each
 * request (considering the request's creative type exclusions).
 */
@CompatibleExchanges(DoubleClick.NAME)
public class VideoBidInterceptor implements BidInterceptor {
  private final Counter videoFixed = new Counter();

  @Inject
  public VideoBidInterceptor(MetricRegistry metricRegistry) {
    metricRegistry.register(MetricRegistry.name(getClass(), "video-fixed"), videoFixed);
  }

  @Override
  public void execute(final InterceptorChain<BidRequest, BidResponse> chain) {
    chain.proceed();

    chain.response().updateBids(new Function<Bid.Builder, Boolean>() {
      @Override public Boolean apply(Bid.Builder bid) {
        Imp imp = chain.request().impWithId(bid.getImpid());
        if (imp == null || !imp.hasVideo()) {
          return false;
        }

        Ad ad = bid.getExtension(DcExt.ad);
        if (!ad.getAttributeList().contains(DoubleClickConstants.CREATIVE_VAST)
            && !ad.getAttributeList().contains(DoubleClickConstants.CREATIVE_VPAID_FLASH)) {
          bid.setExtension(DcExt.ad, ad.toBuilder()
              .addAttribute(DoubleClickConstants.CREATIVE_VAST).build());
          videoFixed.inc();
          return true;
        }

        return false;
      }});
  }
}
