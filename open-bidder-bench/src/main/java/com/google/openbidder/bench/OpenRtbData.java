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

package com.google.openbidder.bench;

import static java.util.Arrays.asList;

import com.google.openrtb.OpenRtb;
import com.google.openrtb.OpenRtb.BidRequest.App;
import com.google.openrtb.OpenRtb.BidRequest.AuctionType;
import com.google.openrtb.OpenRtb.BidRequest.Content;
import com.google.openrtb.OpenRtb.BidRequest.Device;
import com.google.openrtb.OpenRtb.BidRequest.Device.DeviceType;
import com.google.openrtb.OpenRtb.BidRequest.Geo;
import com.google.openrtb.OpenRtb.BidRequest.Imp;
import com.google.openrtb.OpenRtb.BidRequest.Imp.APIFramework;
import com.google.openrtb.OpenRtb.BidRequest.Imp.AdPosition;
import com.google.openrtb.OpenRtb.BidRequest.Imp.Banner;
import com.google.openrtb.OpenRtb.BidRequest.Imp.Banner.ExpandableDirection;
import com.google.openrtb.OpenRtb.BidRequest.Imp.Pmp;
import com.google.openrtb.OpenRtb.BidRequest.Imp.Pmp.Deal;
import com.google.openrtb.OpenRtb.BidRequest.Regs;
import com.google.openrtb.OpenRtb.BidRequest.User;
import com.google.openrtb.OpenRtb.ContentCategory;
import com.google.openrtb.OpenRtb.CreativeAttribute;

public class OpenRtbData {

  public static OpenRtb.BidRequest newBidRequest(boolean coppa) {
    OpenRtb.BidRequest.Builder req = OpenRtb.BidRequest.newBuilder()
        .setId("MDEyMzQ1Njc")
        .setAt(AuctionType.SECOND_PRICE)
        .addImp(Imp.newBuilder()
            .setId("1")
            .setBidfloor(1.5)
            .setBanner(Banner.newBuilder()
                .setW(200)
                .setH(50)
                .setId("1")
                .setPos(AdPosition.ABOVE_THE_FOLD)
                .addBattr(CreativeAttribute.TEXT_ONLY)
                .addAllExpdir(asList(
                    ExpandableDirection.LEFT, ExpandableDirection.RIGHT,
                    ExpandableDirection.UP, ExpandableDirection.DOWN))
                .addApi(APIFramework.MRAID_1)
                .addApi(APIFramework.MRAID_2))
            .setPmp(Pmp.newBuilder()
                .addDeals(Deal.newBuilder()
                    .setId("1")
                    .setBidfloor(0.2))))
        .setApp(App.newBuilder()
            .setId("PewDiePie")
            .setBundle("com.mygame")
            .setContent(Content.newBuilder()
                .setUrl("mysite.com/newsfeed")
                .setLanguage("en")))
        .setDevice(Device.newBuilder()
            .setIp("192.168.1.0")
            .setGeo(Geo.newBuilder()
                .setCountry("USA")
                .setRegion("New York")
                .setMetro("New York, NY"))
            .setDpidmd5("")
            .setCarrier("77777")
            .setModel("MotoX")
            .setOs("Android")
            .setOsv("3.2.1")
            .setDevicetype(DeviceType.PHONE)
            .setLmt(false))
        .setUser(User.newBuilder()
            .setId("j")
            .setCustomdata(""))
        .setTmax(100)
        .addAllBcat(asList(
            ContentCategory.IAB5_3, ContentCategory.IAB11_5, ContentCategory.IAB14_1,
            ContentCategory.IAB14_8, ContentCategory.IAB20_2, ContentCategory.IAB20_4,
            ContentCategory.IAB20_8, ContentCategory.IAB20_13, ContentCategory.IAB20_14,
            ContentCategory.IAB20_15, ContentCategory.IAB20_16, ContentCategory.IAB20_19,
            ContentCategory.IAB20_20, ContentCategory.IAB20_21, ContentCategory.IAB20_23,
            ContentCategory.IAB24, ContentCategory.IAB25, ContentCategory.IAB25_1,
            ContentCategory.IAB25_2, ContentCategory.IAB25_3, ContentCategory.IAB25_4,
            ContentCategory.IAB25_5, ContentCategory.IAB25_6, ContentCategory.IAB25_7,
            ContentCategory.IAB26, ContentCategory.IAB26_1, ContentCategory.IAB26_2,
            ContentCategory.IAB26_3, ContentCategory.IAB26_4));
    if (coppa) {
      req.setRegs(Regs.newBuilder()
          .setCoppa(true));
    }
    return req.build();
  }
}
