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

package com.google.openbidder.bidding.interceptor;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.util.Providers;
import com.google.openbidder.api.bidding.BidController;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.openrtb.ObExt;
import com.google.openbidder.api.testing.bidding.BiddingTestUtil;
import com.google.openbidder.api.testing.bidding.TestBidRequestBuilder;
import com.google.openbidder.api.testing.bidding.TestBidResponseBuilder;
import com.google.openbidder.api.testing.interceptor.NoopInterceptorChain;
import com.google.openbidder.bidding.interceptor.ConfigurableBidInterceptor.BidProbability;
import com.google.openbidder.bidding.interceptor.ConfigurableBidInterceptor.BidPrototype;
import com.google.openbidder.bidding.interceptor.ConfigurableBidInterceptor.CpmMultiplier;
import com.google.openbidder.bidding.interceptor.ConfigurableBidInterceptor.CpmValue;
import com.google.openbidder.bidding.interceptor.ConfigurableBidInterceptor.ErrorProbability;
import com.google.openbidder.bidding.interceptor.ConfigurableBidInterceptor.SizeChoice;
import com.google.openrtb.OpenRtb;
import com.google.openrtb.OpenRtb.BidRequest.Imp;
import com.google.openrtb.OpenRtb.BidRequest.Imp.Banner;
import com.google.openrtb.OpenRtb.BidRequest.Imp.Video;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid.Bid;

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
  public void testOkBanner() {
    Bid.Builder bid = testOk(
        "<a target='_blank' href='%%CLICK_URL_UNESC%%%{${OB_CLICK_URL}}%'>\n" +
        "<img src='ad.png'/>\n" +
        "</a>",
        TestBidRequestBuilder.create().setRequest("1", 1, 1, 4.0).getRequest());
    assertFalse(bid.hasNurl());

    bid = testOk(
        "<a target='_blank' href='%%CLICK_URL_UNESC%%%{${OB_CLICK_URL}}%'>\n" +
        "<img src='ad.png'/>\n" +
        "<img src='${OB_IMPRESSION_URL}?price=${AUCTION_PRICE}'>\n" +
        "</a>",
        TestBidRequestBuilder.create().setRequest("1", 1, 1, 4.0).getRequest());
    assertTrue(bid.hasNurl());
  }

  @Test
  public void testOkVideo() {
    OpenRtb.BidRequest.Builder req = OpenRtb.BidRequest.newBuilder()
        .setId("1")
        .addImp(Imp.newBuilder()
          .setId("1")
          .setBidfloor(4.0)
          .setVideo(Video.newBuilder()));
    Bid.Builder bid = testOk("http://vast-ad.xml", req);
    assertFalse(bid.hasNurl());
  }

  @Test
  public void testMultisize() {
    assertTrue(Iterables.isEmpty(testMultisize(SizeChoice.ALL, Banner.newBuilder())));
    assertTrue(Iterables.isEmpty(testMultisize(SizeChoice.RANDOM, Banner.newBuilder())));

    Bid.Builder bid = Iterables.getOnlyElement(testMultisize(SizeChoice.MIN,
        Banner.newBuilder().setW(100).setH(200)));
    assertEquals(100, bid.getW());
    assertEquals(200, bid.getH());

    bid = Iterables.getOnlyElement(testMultisize(SizeChoice.MAX,
        Banner.newBuilder().setW(100).setH(200)));
    assertEquals(100, bid.getW());
    assertEquals(200, bid.getH());

    bid = Iterables.getOnlyElement(testMultisize(SizeChoice.ALL,
        Banner.newBuilder().setW(100).setH(200)));
    assertEquals(100, bid.getW());
    assertEquals(200, bid.getH());

    bid = Iterables.getOnlyElement(testMultisize(SizeChoice.RANDOM,
        Banner.newBuilder().setW(100).setH(200)));
    assertEquals(100, bid.getW());
    assertEquals(200, bid.getH());

    bid = Iterables.getOnlyElement(testMultisize(SizeChoice.MIN,
        Banner.newBuilder().setWmin(100).setWmax(200).setHmin(300).setHmax(400)));
    assertEquals(100, bid.getW());
    assertEquals(300, bid.getH());

    bid = Iterables.getOnlyElement(testMultisize(SizeChoice.MAX,
        Banner.newBuilder().setWmin(100).setWmax(200).setHmin(300).setHmax(400)));
    assertEquals(200, bid.getW());
    assertEquals(400, bid.getH());

    assertTrue(Iterables.isEmpty(testMultisize(SizeChoice.RANDOM,
        Banner.newBuilder().setWmin(100).setWmax(200).setHmin(300).setHmax(400))));

    assertTrue(Iterables.isEmpty(testMultisize(SizeChoice.ALL,
        Banner.newBuilder().setWmin(100).setWmax(200).setHmin(300).setHmax(400))));
  }

  private static List<Bid.Builder> testMultisize(SizeChoice sizeChoice, Banner.Builder banner) {
    BidRequest req = TestBidRequestBuilder.create().setRequest(OpenRtb.BidRequest.newBuilder()
        .setId("1")
        .addImp(Imp.newBuilder()
          .setId("1")
          .setBidfloor(4.0)
          .setBanner(banner))).build();
    BidResponse resp = TestBidResponseBuilder.create().build();
    ConfigurableBidInterceptor interceptor = new ConfigurableBidInterceptor(
        protoBid.toBuilder().buildPartial(), null, 1.0, 1, 0, sizeChoice);
    checkNotNull(interceptor.toString());
    NoopInterceptorChain.execute(interceptor, req, resp, new MetricRegistry());
    return ImmutableList.copyOf(resp.bids());
  }

  private Bid.Builder testOk(final String snippet, OpenRtb.BidRequest.Builder req) {
    BidRequest request = TestBidRequestBuilder.create().setRequest(req).build();
    BidController controller = BiddingTestUtil.newBidController(new Module() {
      @Override public void configure(Binder binder) {
        binder.bind(Bid.class).annotatedWith(BidPrototype.class).toInstance(
            protoBid.toBuilder().setAdm(snippet).buildPartial());
        binder.bind(Double.class).annotatedWith(CpmMultiplier.class).toInstance(0.5);
        binder.bind(Double.class).annotatedWith(CpmValue.class)
            .toProvider(Providers.<Double>of(null));
        binder.bind(float.class).annotatedWith(BidProbability.class).toInstance(1f);
        binder.bind(float.class).annotatedWith(ErrorProbability.class).toInstance(0f);
        binder.bind(SizeChoice.class).toInstance(SizeChoice.MIN);
      }},
      ConfigurableBidInterceptor.class);
    BidResponse response = TestBidResponseBuilder.create().build();
    controller.onRequest(request, response);
    controller.stopAsync().awaitTerminated();
    Bid.Builder bid = Iterables.getOnlyElement(response.bids());
    assertEquals(2.0, bid.getPrice(), 1e-9);
    assertEquals("https://www.iab.net",
        Iterables.getOnlyElement(bid.getExtension(ObExt.bidClickThroughUrl)));
    assertEquals(snippet, bid.getAdm());
    return bid;
  }

  @Test(expected = RuntimeException.class)
  public void testError() {
    BidRequest request = TestBidRequestBuilder.create().setRequest("1", 1, 1, 4.0).build();
    BidController controller = BiddingTestUtil.newBidController(new Module() {
      @Override public void configure(Binder binder) {
        binder.bind(Double.class).annotatedWith(ConfigurableBidInterceptor.BidProbability.class)
            .toInstance(1.0);
        binder.bind(Double.class).annotatedWith(ConfigurableBidInterceptor.ErrorProbability.class)
            .toInstance(1.0);
      }},
      ConfigurableBidInterceptor.class);
    controller.onRequest(request, TestBidResponseBuilder.create().build());
  }
}
