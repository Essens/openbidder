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

package com.google.openbidder.exchange.doubleclick.server;

import com.google.doubleclick.openrtb.ExtMapper;
import com.google.openbidder.api.openrtb.ObExt;
import com.google.openrtb.OpenRtb;
import com.google.openrtb.OpenRtb.BidRequest;
import com.google.openrtb.OpenRtb.BidRequest.Imp;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid.Bid;
import com.google.protos.adx.NetworkBid;

/**
 * Extension mapper for Open Bidder's internal, toolkit-specific extensions.
 */
public class OpenBidderExtMapper extends ExtMapper {
  public static final ExtMapper INSTANCE = new OpenBidderExtMapper();

  private OpenBidderExtMapper() {
  }

  @Override public void toOpenRtbImp(NetworkBid.BidRequest.AdSlot dcSlot, Imp.Builder imp) {
    for (NetworkBid.BidRequest.AdSlot.MatchingAdData dcAdData :
      dcSlot.getMatchingAdDataList()) {
      if (dcAdData.hasAdgroupId()) {
        imp.addExtension(ObExt.impCid, String.valueOf(dcAdData.getAdgroupId()));
      }
    }
  }

  @Override public void toDoubleClickAd(BidRequest request, OpenRtb.BidResponse response,
      Bid bid, NetworkBid.BidResponse.Ad.Builder dcAd) {
    dcAd.addAllClickThroughUrl(bid.getExtension(ObExt.bidClickThroughUrl));
  }
}
