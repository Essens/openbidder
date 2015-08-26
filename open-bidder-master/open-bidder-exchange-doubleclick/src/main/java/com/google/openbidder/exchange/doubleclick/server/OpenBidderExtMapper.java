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
import com.google.openrtb.OpenRtb.BidRequest.Impression;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid.Bid;
import com.google.protos.adx.NetworkBid;

import java.util.ArrayList;
import java.util.List;

/**
 * Extension mapper for Open Bidder's internal, toolkit-specific extensions.
 */
public class OpenBidderExtMapper extends ExtMapper {
  public static final ExtMapper INSTANCE = new OpenBidderExtMapper();

  private OpenBidderExtMapper() {
  }

  @Override public void toOpenRtbImpression(
      NetworkBid.BidRequest.AdSlot dcSlot, Impression.Builder imp) {
    List<String> cid = null;

    for (NetworkBid.BidRequest.AdSlot.MatchingAdData dcAdData :
      dcSlot.getMatchingAdDataList()) {
      if (dcAdData.hasAdgroupId()) {
        if (cid == null) {
          cid = new ArrayList<>();
        }
        cid.add(String.valueOf(dcAdData.getAdgroupId()));
      }
    }

    if (cid == null) {
      return;
    }

    imp.setExtension(ObExt.imp, ObExt.Impression.newBuilder().addAllCid(cid).build());
  }

  @Override public void toNativeAd(BidRequest request, OpenRtb.BidResponse response,
      Bid bid, NetworkBid.BidResponse.Ad.Builder dcAd) {
    if (bid.hasExtension(ObExt.bid)) {
      ObExt.Bid obBid = bid.getExtension(ObExt.bid);

      if (obBid.getClickThroughUrlCount() != 0) {
        dcAd.addAllClickThroughUrl(obBid.getClickThroughUrlList());
      }
    }
  }
}
