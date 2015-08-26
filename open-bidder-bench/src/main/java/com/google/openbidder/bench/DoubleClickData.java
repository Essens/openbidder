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

import com.google.protobuf.ByteString;
import com.google.protobuf.UnknownFieldSet;
import com.google.protobuf.UnknownFieldSet.Field;
import com.google.protos.adx.NetworkBid;
import com.google.protos.adx.NetworkBid.BidRequest.AdSlot;
import com.google.protos.adx.NetworkBid.BidRequest.AdSlot.MatchingAdData;
import com.google.protos.adx.NetworkBid.BidRequest.AdSlot.SlotVisibility;
import com.google.protos.adx.NetworkBid.BidRequest.Mobile;
import com.google.protos.adx.NetworkBid.BidRequest.Mobile.DeviceOsVersion;
import com.google.protos.adx.NetworkBid.BidRequest.Mobile.MobileDeviceType;
import com.google.protos.adx.NetworkBid.BidRequest.UserDataTreatment;
import com.google.protos.adx.NetworkBid.BidRequest.Vertical;

public class DoubleClickData {
  public static final ByteString REQUEST_ID = ByteString.copyFromUtf8("01234567");

  public static NetworkBid.BidRequest newRequest(boolean coppa) {
    NetworkBid.BidRequest.Builder req = NetworkBid.BidRequest.newBuilder()
        .setId(REQUEST_ID)
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
                .setMinimumCpmMicros(1500000)
                .addDirectDeal(MatchingAdData.DirectDeal.newBuilder()
                    .setDirectDealId(1)
                    .setFixedCpmMicros(200000))));
    if (coppa) {
      req.addUserDataTreatment(UserDataTreatment.TAG_FOR_CHILD_DIRECTED_TREATMENT);
    }
    return req.build();
  }

  public static NetworkBid.BidRequest newRealRequest() {
    return NetworkBid.BidRequest.newBuilder()
        .setId(ByteString.copyFromUtf8("SYm\244\000\tNE\n4)\217\327\000!O"))
        .setIp(ByteString.copyFromUtf8("B9\027"))
        .setUserAgent(
              "Mozilla/5.0 (iPad; CPU OS 6_1_3 like Mac OS X) AppleWebKit/536.26 "
            + "(KHTML, like Gecko) Mobile/10B329 [FBAN/FBIOS;FBAV/8.0.0.28.18;FBBV/1665515;"
            + "FBDV/iPad3,4;FBMD/iPad;FBSN/iPhone OS;FBSV/6.1.3;FBSS/2; "
            + "FBCR/;FBID/tablet;FBLC/en_US;FBOP/1],gzip(gfe)")
        .setUrl("http://blog.sfgate.com/ontheblock/2014/04/24/"
              + "robin-williams-30-million-napa-estate-returns-to-market/")
        .addDetectedLanguage("en")
        .addDetectedVertical(Vertical.newBuilder().setId(29).setWeight(0.5282414f))
        .addDetectedVertical(Vertical.newBuilder().setId(34).setWeight(0.24850088f))
        .addDetectedVertical(Vertical.newBuilder().setId(3).setWeight(0.22325774f))
        .addDetectedVertical(Vertical.newBuilder().setId(408).setWeight(1.0f))
        .addDetectedVertical(Vertical.newBuilder().setId(5265).setWeight(0.75f))
        .addAdslot(AdSlot.newBuilder()
            .setId(1)
            .addWidth(300)
            .addHeight(250)
            .addAllExcludedAttribute(asList(7, 32, 34, 22, 13, 14, 15, 16, 17, 18, 19, 20, 25, 27))
            .addAllAllowedVendorType(asList(
                10, 28, 42, 43, 51, 60, 63, 65, 71, 92, 94, 113, 126, 128, 130, 144, 145, 146, 148,
                156, 179, 182, 198, 204, 225, 529, 534, 537, 538, 231, 232, 233, 234, 236, 237, 238,
                255, 260, 267, 285, 303, 315, 542, 325, 331, 332, 334, 335, 342, 414, 543, 432, 441,
                445, 472, 474, 476, 477, 480, 481, 485, 486, 489, 490, 497, 499, 504, 550))
            .addAllExcludedSensitiveCategory(asList(4, 30, 10, 31, 8, 24))
            .addMatchingAdData(MatchingAdData.newBuilder()
                .setAdgroupId(9917521869L)
                .setMinimumCpmMicros(1080000L))
            .addAllTargetableChannel(asList("pack-brand-Hearst Newspapers::All", ""))
            .setSlotVisibility(SlotVisibility.NO_DETECTION)
            .addAllExcludedProductCategory(asList(10040, 10042, 10137, 10746, 13470, 13566, 13656))
            .setAdBlockKey(4117730669L)
            .setPublisherSettingsListId(7790212871164385391L)
            .setUnknownFields(UnknownFieldSet.newBuilder()
                .addField(17, Field.newBuilder().addFixed32(704).build())
                .addField(18, Field.newBuilder().addFixed32(1024).build()).build()))
        .setIsTest(false)
        .setCookieVersion(1)
        .setGoogleUserId("ANONYMIZED-ANONYMIZED-ANONY")
        .setVerticalDictionaryVersion(2)
        .setTimezoneOffset(-240)
        .setMobile(Mobile.newBuilder()
            .setPlatform("ipad")
            .setMobileDeviceType(MobileDeviceType.TABLET)
            .setBrand("apple")
            .setModel("ipad")
            .setOsVersion(DeviceOsVersion.newBuilder()
                .setOsVersionMajor(6)
                .setOsVersionMinor(1))
            .setScreenWidth(768)
            .setScreenHeight(1024)
            .setCarrierId(0)
            .setDevicePixelRatioMillis(2000))
        .setCookieAgeSeconds(7942774)
        .setPostalCode("27516")
        .setGeoCriteriaId(9009671)
        .setSellerNetworkId(1614)
        .setPublisherSettingsListId(6915063772784805391L)
        .build();
  }
}
