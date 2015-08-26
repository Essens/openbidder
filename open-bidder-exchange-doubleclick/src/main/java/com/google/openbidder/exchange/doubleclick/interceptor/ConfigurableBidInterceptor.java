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

import com.google.doubleclick.DcExt;
import com.google.openbidder.api.platform.CompatibleExchanges;
import com.google.openbidder.exchange.doubleclick.config.DoubleClick;
import com.google.openrtb.OpenRtb.BidRequest.Imp;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid.Bid;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * DoubleClick-specific ConfigurableBidInterceptor, adds full support for
 * multisize slots (which cannot be represented by the pure OpenRTB model).
 */
@CompatibleExchanges(DoubleClick.NAME)
public class ConfigurableBidInterceptor
extends com.google.openbidder.bidding.interceptor.ConfigurableBidInterceptor {

  @Inject
  public ConfigurableBidInterceptor(
      @BidPrototype Bid bid,
      @Nullable @CpmMultiplier Double cpmMultiplier,
      @Nullable @CpmValue Double cpmValue,
      @BidProbability float bidProbability,
      @ErrorProbability float errorProbability,
      SizeChoice sizeChoice) {
    super(bid, cpmMultiplier, cpmValue, bidProbability, errorProbability, sizeChoice);
  }

  @Override protected List<Integer> allWidths(Imp imp) {
    return imp.getExtension(DcExt.adSlot).getWidthList();
  }

  @Override protected List<Integer> allHeights(Imp imp) {
    return imp.getExtension(DcExt.adSlot).getHeightList();
  }
}
