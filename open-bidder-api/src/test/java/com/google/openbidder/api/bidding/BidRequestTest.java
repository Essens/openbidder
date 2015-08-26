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
import com.google.openbidder.http.request.StandardHttpRequest;
import com.google.openbidder.util.testing.TestUtil;
import com.google.openrtb.OpenRtb;
import com.google.openrtb.OpenRtb.BidRequest.Imp;
import com.google.openrtb.OpenRtb.BidRequest.Imp.Banner;
import com.google.openrtb.OpenRtb.BidRequest.Imp.Video;

import org.junit.Test;

/**
 * Unit tests for {@link BidResponse}.
 */
public class BidRequestTest {

  @Test
  public void testCommonMethods() {
    BidRequest req = TestBidRequestBuilder.create().build();
    TestUtil.testCommonMethods(req);
    assertEquals(NoExchange.INSTANCE, req.getExchange());
    assertNull(req.nativeRequest());
    assertEquals(NoExchange.INSTANCE, req.getExchange());
    assertNotNull(req.openRtb());
  }

  @Test
  public void testBuilder() {
    BidRequest.Builder req = BidRequest.newBuilder()
        .setExchange(NoExchange.INSTANCE)
        .setHttpRequest(StandardHttpRequest.newBuilder().setUri("http://a.com").build())
        .setNativeRequest("x")
        .setRequest(OpenRtb.BidRequest.newBuilder().setId("1"));
    TestUtil.testCommonMethods(req);
    assertEquals("x", req.getNativeRequest());
    assertEquals(NoExchange.INSTANCE, req.getExchange());
    assertNotNull(req.getRequest());
    assertNotNull(req.build().toBuilder().build());
  }

  @Test
  public void imps() {
    BidRequest request = TestBidRequestBuilder.create()
        .setRequest(OpenRtb.BidRequest.newBuilder()
            .setId("1")
            .addImp(Imp.newBuilder().setId("1"))
            .buildPartial()).build();
    assertEquals(1, request.imps().size());
    assertTrue(Iterables.isEmpty(request.impsWith(Predicates.<Imp>alwaysFalse())));
    assertNotNull(request.impWithId("1"));
  }

  @Test(expected = IllegalStateException.class)
  public void testOpenRtb_bad() {
    BidRequest request = BidRequest.newBuilder()
        .setHttpRequest(StandardHttpRequest.newBuilder().setUri("http://a.com").build())
        .build();
    assertNotNull(request.toString());
    request.openRtb();
  }

  @Test
  public void banners() {
    BidRequest request = TestBidRequestBuilder.create()
        .setRequest(OpenRtb.BidRequest.newBuilder()
            .setId("1")
            .addImp(Imp.newBuilder().setId("1").setBanner(Banner.newBuilder().setId("0")))
            .buildPartial()).build();
    assertEquals(1, Iterables.size(request.bannerImps()));
    assertTrue(Iterables.isEmpty(request.bannerImpsWith(Predicates.<Imp>alwaysFalse())));
    assertNotNull(request.bannerImpWithId("1", "0"));
  }

  @Test
  public void videos() {
    BidRequest request = TestBidRequestBuilder.create()
        .setRequest(OpenRtb.BidRequest.newBuilder()
            .setId("1")
            .addImp(Imp.newBuilder().setId("1").setVideo(Video.newBuilder()))
            .buildPartial())
        .build();
    assertEquals(1, Iterables.size(request.videoImps()));
    assertTrue(Iterables.isEmpty(request.videoImpsWith(Predicates.<Imp>alwaysFalse())));
  }

  public void testOpenRtb_notAvailable() {
    assertNull(BidRequest.newBuilder().build().openRtb());
  }
}
