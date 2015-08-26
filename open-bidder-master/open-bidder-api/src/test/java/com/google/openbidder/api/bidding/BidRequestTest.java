/*
 * Copyright 2012 Google Inc. All Rights Reserved.
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

package com.google.openbidder.api.bidding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.openbidder.api.platform.NoExchange;
import com.google.openbidder.api.testing.bidding.TestBidRequestBuilder;
import com.google.openbidder.util.testing.TestUtil;
import com.google.openrtb.OpenRtb;
import com.google.openrtb.OpenRtb.BidRequest.Impression;
import com.google.openrtb.OpenRtb.BidRequest.Impression.Banner;
import com.google.openrtb.OpenRtb.BidRequest.Impression.Video;
import com.google.openrtb.OpenRtb.BidRequest.Impression.Video.Linearity;
import com.google.openrtb.OpenRtb.BidRequest.Impression.Video.Protocol;

import org.junit.Test;

/**
 * Unit tests for {@link BidResponse}.
 */
public class BidRequestTest {

  @Test
  public void testCommonMethods() {
    BidRequest r1 = TestBidRequestBuilder.create().build();
    TestUtil.testCommonMethods(r1);
    assertEquals(NoExchange.INSTANCE, r1.getExchange());
  }

  @Test
  public void test() {
    BidRequest request = TestBidRequestBuilder.create().build();
    assertNull(request.nativeRequest());
    assertEquals(NoExchange.INSTANCE, request.getExchange());
    assertNotNull(request.openRtb());
  }

  @Test
  public void imps() {
    BidRequest request = TestBidRequestBuilder.create()
        .setRequest(OpenRtb.BidRequest.newBuilder()
            .setId("1")
            .addImp(Impression.newBuilder().setId("1"))
            .buildPartial()).build();
    assertEquals(1, request.imps().size());
    assertTrue(Iterables.isEmpty(request.impsWith(Predicates.<Impression>alwaysFalse())));
    assertNotNull(request.impWithId("1"));
  }

  @Test
  public void banners() {
    BidRequest request = TestBidRequestBuilder.create()
        .setRequest(OpenRtb.BidRequest.newBuilder()
            .setId("1")
            .addImp(Impression.newBuilder().setId("1").setBanner(Banner.newBuilder().setId("0")))
            .buildPartial()).build();
    assertEquals(1, Iterables.size(request.bannerImps()));
    assertTrue(Iterables.isEmpty(request.bannerImpsWith(Predicates.<Impression>alwaysFalse())));
    assertNotNull(request.bannerImpWithId("1", "0"));
  }

  @Test
  public void videos() {
    BidRequest request = TestBidRequestBuilder.create()
        .setRequest(OpenRtb.BidRequest.newBuilder()
            .setId("1")
            .addImp(Impression.newBuilder().setId("1").setVideo(Video.newBuilder()
                .setLinearity(Linearity.LINEAR)
                .setMinduration(100)
                .setMaxduration(200)
                .setProtocol(Protocol.VAST_3_0)))
            .buildPartial())
        .build();
    assertEquals(1, Iterables.size(request.videoImps()));
    assertTrue(Iterables.isEmpty(request.videoImpsWith(Predicates.<Impression>alwaysFalse())));
  }

  public void testOpenRtb_notAvailable() {
    assertNull(BidRequest.newBuilder().build().openRtb());
  }
}
