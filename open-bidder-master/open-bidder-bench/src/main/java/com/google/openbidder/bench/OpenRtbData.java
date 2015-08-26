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
import com.google.openrtb.OpenRtb.BidRequest.Content;
import com.google.openrtb.OpenRtb.BidRequest.Device;
import com.google.openrtb.OpenRtb.BidRequest.Device.DeviceType;
import com.google.openrtb.OpenRtb.BidRequest.Geo;
import com.google.openrtb.OpenRtb.BidRequest.Impression;
import com.google.openrtb.OpenRtb.BidRequest.Impression.AdPosition;
import com.google.openrtb.OpenRtb.BidRequest.Impression.ApiFramework;
import com.google.openrtb.OpenRtb.BidRequest.Impression.Banner;
import com.google.openrtb.OpenRtb.BidRequest.Impression.Banner.ExpandableDirection;
import com.google.openrtb.OpenRtb.BidRequest.Impression.PMP;
import com.google.openrtb.OpenRtb.BidRequest.Impression.PMP.DirectDeal;
import com.google.openrtb.OpenRtb.BidRequest.Regulations;
import com.google.openrtb.OpenRtb.BidRequest.User;
import com.google.openrtb.OpenRtb.CreativeAttribute;
import com.google.openrtb.OpenRtb.Flag;

public class OpenRtbData {

  public static OpenRtb.BidRequest newBidRequest(boolean coppa) {
    OpenRtb.BidRequest.Builder req = OpenRtb.BidRequest.newBuilder()
        .setId("3031323334353637")
        .addImp(Impression.newBuilder()
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
                .addApi(ApiFramework.MRAID))
            .setPmp(PMP.newBuilder()
                .addDeals(DirectDeal.newBuilder()
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
                .setRegion("New York"))
            .setDpidmd5("")
            .setCarrier("77777")
            .setModel("MotoX")
            .setOs("Android")
            .setOsv("3.2.1")
            .setDevicetype(DeviceType.MOBILE))
        .setUser(User.newBuilder()
            .setId("j")
            .setCustomdata(""))
        .setTmax(100)
        .addAllBcat(asList("IAB5-3", "IAB14-8",
            "IAB20-2", "IAB20-4", "IAB20-8", "IAB20-13", "IAB20-14", "IAB20-15", "IAB20-16",
            "IAB20-19", "IAB20-20", "IAB20-21", "IAB20-23", "IAB24",
            "IAB25", "IAB25-1", "IAB25-2", "IAB25-3", "IAB25-4", "IAB25-5", "IAB25-6", "IAB25-7",
            "IAB26", "IAB26-1", "IAB26-2", "IAB26-3", "IAB26-4",
            "IAB11-5"));
    if (coppa) {
      req.setRegs(Regulations.newBuilder()
          .setCoppa(Flag.YES));
    }
    return req.build();
  }
}
