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

import static org.junit.Assert.assertEquals;

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
import com.google.openbidder.bidding.interceptor.ConfigurableBidInterceptor.BidProbability;
import com.google.openbidder.bidding.interceptor.ConfigurableBidInterceptor.BidPrototype;
import com.google.openbidder.bidding.interceptor.ConfigurableBidInterceptor.CpmMultiplier;
import com.google.openbidder.bidding.interceptor.ConfigurableBidInterceptor.CpmValue;
import com.google.openbidder.bidding.interceptor.ConfigurableBidInterceptor.ErrorProbability;
import com.google.openrtb.OpenRtb;
import com.google.openrtb.OpenRtb.BidRequest.Impression;
import com.google.openrtb.OpenRtb.BidRequest.Impression.Video;
import com.google.openrtb.OpenRtb.BidRequest.Impression.Video.Linearity;
import com.google.openrtb.OpenRtb.BidRequest.Impression.Video.Protocol;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid.Bid;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid.BidOrBuilder;

import org.junit.Test;

/**
 * Unit tests for {@link ConfigurableBidInterceptor}.
 */
public class ConfigurableBidInterceptorTest {

  @Test
  public void testOkBanner() {
    testOk(
        "<img src='ad.png'/>",
        TestBidRequestBuilder.create().setRequest("1", 1, 1, 4.0).getRequest());
  }

  @Test
  public void testOkVideo() {
    OpenRtb.BidRequest.Builder req = OpenRtb.BidRequest.newBuilder()
        .setId("1")
        .addImp(Impression.newBuilder()
          .setId("1")
          .setBidfloor(4.0)
          .setVideo(Video.newBuilder()
              .setLinearity(Linearity.LINEAR)
              .setMinduration(100)
              .setMaxduration(200)
              .setProtocol(Protocol.VAST_3_0)
              .setW(640)
              .setH(480)));
    testOk("http://vast-ad.xml", req);
  }

  private void testOk(final String snippet, OpenRtb.BidRequest.Builder req) {
    BidRequest request = TestBidRequestBuilder.create().setRequest(req).build();
    BidController controller = BiddingTestUtil.newBidController(new Module() {
      @Override public void configure(Binder binder) {
        binder.bind(Bid.class).annotatedWith(BidPrototype.class).toInstance(Bid.newBuilder()
            .setId("1")
            .setAdm(snippet)
            .setExtension(ObExt.bid, ObExt.Bid.newBuilder()
                .addClickThroughUrl("https://www.iab.net").build())
            .buildPartial());
        binder.bind(Double.class).annotatedWith(CpmMultiplier.class).toInstance(0.5);
        binder.bind(Double.class).annotatedWith(CpmValue.class)
            .toProvider(Providers.<Double>of(null));
        binder.bind(float.class).annotatedWith(BidProbability.class).toInstance(1f);
        binder.bind(float.class).annotatedWith(ErrorProbability.class).toInstance(0f);
      }},
      ConfigurableBidInterceptor.class);
    BidResponse response = TestBidResponseBuilder.create().build();
    controller.onRequest(request, response);
    controller.stopAsync().awaitTerminated();
    BidOrBuilder bid = Iterables.getOnlyElement(response.bids());
    assertEquals(2.0, bid.getPrice(), 1e-9);
    assertEquals("https://www.iab.net", bid.getExtension(ObExt.bid).getClickThroughUrl(0));
    assertEquals(snippet, bid.getAdm());
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
