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

import static java.util.Arrays.asList;

import com.google.openbidder.api.openrtb.ObExt;
import com.google.openbidder.exchange.doubleclick.testing.DoubleClickTestUtil;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid.Bid;
import com.google.protobuf.ByteString;
import com.google.protos.adx.NetworkBid;
import com.google.protos.adx.NetworkBid.BidRequest.AdSlot;
import com.google.protos.adx.NetworkBid.BidRequest.AdSlot.MatchingAdData;
import com.google.protos.adx.NetworkBid.BidRequest.AdSlot.SlotVisibility;
import com.google.protos.adx.NetworkBid.BidRequest.Mobile;
import com.google.protos.adx.NetworkBid.BidRequest.Mobile.DeviceOsVersion;
import com.google.protos.adx.NetworkBid.BidRequest.Mobile.MobileDeviceType;
import com.google.protos.adx.NetworkBid.BidRequest.UserDataTreatment;

public class TestData {

  public static Bid.Builder newBid() {
    return Bid.newBuilder()
        .setId("0")
        .setImpid("1")
        .setAdid("2")
        .setCid("3")
        .setCrid("4")
        .setPrice(1.0)
        .setAdm("<blink>hello world</blink>")
        .addExtension(ObExt.bidClickThroughUrl, "https://www.iab.net");
  }

  public static NetworkBid.BidRequest newRequest(boolean coppa) {
    NetworkBid.BidRequest.Builder req = NetworkBid.BidRequest.newBuilder()
        .setId(DoubleClickTestUtil.REQUEST_ID)
        .setIp(ByteString.copyFrom(new byte[] { (byte) 192, (byte) 168, (byte) 1 } ))
        .setGoogleUserId("john")
        .setConstrainedUsageGoogleUserId("j")
        .setHostedMatchData(ByteString.EMPTY)
        .setConstrainedUsageHostedMatchData(ByteString.EMPTY)
        .setGeoCriteriaId(9058770)
        .setAnonymousId("mysite.com")
        .setUrl("mysite.com/newsfeed")
        .addDetectedLanguage("en_US")
        .setMobile(Mobile.newBuilder()
            .setAppId("com.mygame")
            .setCarrierId(77777)
            .setPlatform("Android")
            .setMobileDeviceType(MobileDeviceType.HIGHEND_PHONE)
            .setOsVersion(DeviceOsVersion.newBuilder()
                .setOsVersionMajor(3).setOsVersionMinor(2).setOsVersionMicro(1))
            .setModel("MotoX")
            .setEncryptedHashedIdfa(ByteString.EMPTY)
            .setConstrainedUsageEncryptedHashedIdfa(ByteString.EMPTY))
        .addAdslot(AdSlot.newBuilder()
            .setId(1)
            .setSlotVisibility(SlotVisibility.ABOVE_THE_FOLD)
            .addWidth(200)
            .addHeight(50)
            .addAllAllowedVendorType(asList(10, 94, 97))
            .addAllExcludedSensitiveCategory(asList(0, 3, 4))
            .addAllExcludedAttribute(asList(1, 2, 3))
            .addAllExcludedProductCategory(asList(13, 14))
            .addTargetableChannel(coppa ? "afv_user_id_PewDiePie" : "pack-anon-x::y")
            .addMatchingAdData(MatchingAdData.newBuilder()
                .setAdgroupId(10)
                .addDirectDeal(MatchingAdData.DirectDeal.newBuilder()
                    .setDirectDealId(1)
                    .setFixedCpmMicros(200))));
    if (coppa) {
      req.addUserDataTreatment(UserDataTreatment.TAG_FOR_CHILD_DIRECTED_TREATMENT);
    }
    return req.build();
  }
}
