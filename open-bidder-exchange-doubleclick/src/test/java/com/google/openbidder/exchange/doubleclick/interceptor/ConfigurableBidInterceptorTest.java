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

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.doubleclick.DcExt;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.openrtb.ObExt;
import com.google.openbidder.api.testing.bidding.TestBidRequestBuilder;
import com.google.openbidder.api.testing.bidding.TestBidResponseBuilder;
import com.google.openbidder.api.testing.interceptor.NoopInterceptorChain;
import com.google.openbidder.bidding.interceptor.ConfigurableBidInterceptor.SizeChoice;
import com.google.openrtb.OpenRtb;
import com.google.openrtb.OpenRtb.BidRequest.Imp;
import com.google.openrtb.OpenRtb.BidRequest.Imp.Banner;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid.Bid;
import com.google.protos.adx.NetworkBid.BidRequest.AdSlot;

import com.codahale.metrics.MetricRegistry;

import org.junit.Test;

import java.util.List;

/**
 * Unit tests for {@link ConfigurableBidInterceptor}.
 */
public class ConfigurableBidInterceptorTest {
  static final Bid protoBid = Bid.newBuilder()
      .setId("1")
      .addExtension(ObExt.bidClickThroughUrl, "https://www.iab.net")
      .buildPartial();

  @Test
  public void testMultisize() {
    Bid.Builder bid = Iterables.getOnlyElement(testMultisize(SizeChoice.MIN,
        Banner.newBuilder().setW(100).setH(200),
        AdSlot.newBuilder().addWidth(100).addHeight(200)));
    assertEquals(100, bid.getW());
    assertEquals(200, bid.getH());

    bid = Iterables.getOnlyElement(testMultisize(SizeChoice.MAX,
        Banner.newBuilder().setW(100).setH(200),
        AdSlot.newBuilder().addWidth(100).addHeight(200)));
    assertEquals(100, bid.getW());
    assertEquals(200, bid.getH());

    bid = Iterables.getOnlyElement(testMultisize(SizeChoice.ALL,
        Banner.newBuilder().setW(100).setH(200),
        AdSlot.newBuilder().addWidth(100).addHeight(200)));
    assertEquals(100, bid.getW());
    assertEquals(200, bid.getH());

    bid = Iterables.getOnlyElement(testMultisize(SizeChoice.MIN,
        Banner.newBuilder().setWmin(100).setWmax(200).setHmin(300).setHmax(400),
        AdSlot.newBuilder().addAllWidth(asList(100, 200)).addAllHeight(asList(300, 400))));
    assertEquals(100, bid.getW());
    assertEquals(300, bid.getH());

    bid = Iterables.getOnlyElement(testMultisize(SizeChoice.MAX,
        Banner.newBuilder().setWmin(100).setWmax(200).setHmin(300).setHmax(400),
        AdSlot.newBuilder().addAllWidth(asList(100, 200)).addAllHeight(asList(300, 400))));
    assertEquals(200, bid.getW());
    assertEquals(400, bid.getH());

    bid = Iterables.getOnlyElement(testMultisize(SizeChoice.RANDOM,
        Banner.newBuilder().setWmin(100).setWmax(200).setHmin(300).setHmax(400),
        AdSlot.newBuilder().addAllWidth(asList(100, 200)).addAllHeight(asList(300, 400))));
    assertTrue(asList(100, 200).contains(bid.getW()));
    assertTrue(asList(300, 400).contains(bid.getH()));

    List<Bid.Builder> bids = testMultisize(SizeChoice.ALL,
        Banner.newBuilder().setWmin(100).setWmax(200).setHmin(300).setHmax(400),
        AdSlot.newBuilder().addAllWidth(asList(100, 200)).addAllHeight(asList(300, 400)));
    assertEquals(2, bids.size());
    assertEquals(100, bids.get(0).getW());
    assertEquals(300, bids.get(0).getH());
    assertEquals(200, bids.get(1).getW());
    assertEquals(400, bids.get(1).getH());
  }

  private static List<Bid.Builder> testMultisize(
      SizeChoice sizeChoice, Banner.Builder banner, AdSlot.Builder adSlot) {
    BidRequest req = TestBidRequestBuilder.create().setRequest(OpenRtb.BidRequest.newBuilder()
        .setId("1")
        .addImp(Imp.newBuilder()
            .setId("1")
            .setBidfloor(4.0)
            .setBanner(banner)
            .setExtension(DcExt.adSlot, adSlot.setId(1).buildPartial())))
        .build();
    BidResponse resp = TestBidResponseBuilder.create().build();
    ConfigurableBidInterceptor interceptor = new ConfigurableBidInterceptor(
        protoBid.toBuilder().buildPartial(), null, 1.0, 1, 0, sizeChoice);
    NoopInterceptorChain.execute(interceptor, req, resp, new MetricRegistry());
    return ImmutableList.copyOf(resp.bids());
  }
}
