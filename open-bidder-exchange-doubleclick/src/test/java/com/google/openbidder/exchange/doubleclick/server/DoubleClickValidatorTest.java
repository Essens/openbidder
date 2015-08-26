/*
 * Copyright 2013 Google Inc. All Rights Reserved.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.doubleclick.DcExt;
import com.google.doubleclick.crypto.DoubleClickCrypto;
import com.google.doubleclick.openrtb.DoubleClickLinkMapper;
import com.google.doubleclick.openrtb.DoubleClickOpenRtbMapper;
import com.google.doubleclick.util.DoubleClickValidator;
import com.google.openbidder.api.bidding.BidInterceptor;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.interceptor.InterceptorChain;
import com.google.openbidder.api.openrtb.ObExt;
import com.google.openbidder.api.testing.bidding.BiddingTestUtil;
import com.google.openbidder.exchange.doubleclick.DoubleClickConstants;
import com.google.openbidder.exchange.doubleclick.interceptor.FlashlessBidInterceptorTest;
import com.google.openbidder.exchange.doubleclick.testing.DoubleClickTestUtil;
import com.google.openbidder.http.HttpRequest;
import com.google.openbidder.http.HttpResponse;
import com.google.openbidder.http.receiver.DefaultHttpReceiverContext;
import com.google.openbidder.http.response.StandardHttpResponse;
import com.google.openbidder.util.testing.FakeClock;
import com.google.openrtb.OpenRtb.BidRequest.Imp;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid.Bid;
import com.google.protos.adx.NetworkBid;
import com.google.protos.adx.NetworkBid.BidRequest.AdSlot;
import com.google.protos.adx.NetworkBid.BidResponse.Ad;

import com.codahale.metrics.MetricRegistry;

import org.apache.http.HttpStatus;
import org.junit.Test;

import java.io.IOException;

/**
 * Tests for {@link DoubleClickValidator}.
 */
public class DoubleClickValidatorTest {

  @Test
  public void testFlashless() throws IOException {
    HttpRequest httpRequest = DoubleClickTestUtil.newHttpRequest(
        FlashlessBidInterceptorTest.newFlashlessRequest());
    HttpResponse.Builder httpResponse = StandardHttpResponse.newBuilder();
    DoubleClickBidRequestReceiver receiver = newReceiver(newFlashlessInterceptor(true));
    receiver.receive(new DefaultHttpReceiverContext(httpRequest, httpResponse));
    assertEquals(HttpStatus.SC_OK, httpResponse.getStatusCode());
    assertEquals(
        ImmutableList.of(NetworkBid.BidResponse.Ad.newBuilder()
            .addClickThroughUrl("https://www.iab.net")
            .setBuyerCreativeId("1")
            .setHtmlSnippet("snippet")
            .addAdslot(NetworkBid.BidResponse.Ad.AdSlot.newBuilder()
                .setId(1)
                .setMaxCpmMicros(1_000_000))
            .addAttribute(DoubleClickConstants.CREATIVE_NON_FLASH)
            .build()),
        NetworkBid.BidResponse.parseFrom(httpResponse.build().content()).getAdList());
  }

  @Test
  public void testFlashless_Bad() throws IOException {
    HttpRequest httpRequest = DoubleClickTestUtil.newHttpRequest(
        FlashlessBidInterceptorTest.newFlashlessRequest());
    HttpResponse.Builder httpResponse = StandardHttpResponse.newBuilder();
    DoubleClickBidRequestReceiver receiver = newReceiver(newFlashlessInterceptor(false));
    receiver.receive(new DefaultHttpReceiverContext(httpRequest, httpResponse));
    assertEquals(HttpStatus.SC_OK, httpResponse.getStatusCode());
    assertTrue(
        NetworkBid.BidResponse.parseFrom(httpResponse.build().content()).getAdList().isEmpty());
  }

  @Test
  public void testNoSlot() {
    NetworkBid.BidRequest dcRequest = NetworkBid.BidRequest.newBuilder()
        .setId(DoubleClickTestUtil.REQUEST_ID)
        .addAdslot(AdSlot.newBuilder().setId(1))
        .addAdslot(AdSlot.newBuilder().setId(10))
        .build();
    NetworkBid.BidResponse.Builder dcResponse = NetworkBid.BidResponse.newBuilder()
        .addAd(Ad.newBuilder().addAdslot(NetworkBid.BidResponse.Ad.AdSlot.newBuilder()
            .setId(5)
            .setMaxCpmMicros(100)));
    DoubleClickValidator validator =
        new DoubleClickValidator(new MetricRegistry(), DoubleClickTestUtil.getMetadata());
    assertEquals(1, dcResponse.getAdCount());
    validator.validate(dcRequest, dcResponse);
    assertEquals(0, dcResponse.getAdCount());
  }

  private static BidInterceptor newFlashlessInterceptor(final boolean good) {
    return new BidInterceptor() {
      @Override public void execute(InterceptorChain<BidRequest, BidResponse> chain) {
        NetworkBid.BidRequest dcRequest = chain.request().nativeRequest();
        assertTrue(dcRequest.getAdslot(0).getExcludedAttributeList().contains(
            DoubleClickConstants.CREATIVE_FLASH));

        for (Imp imp : chain.request().imps()) {
          newFlashlessBid(chain.response(), imp, good);
        }
      }
    };
  }

  private static void newFlashlessBid(BidResponse bidResponse, Imp imp, final boolean good) {
    Bid.Builder bid = Bid.newBuilder()
        .setId(imp.getId())
        .setImpid(imp.getId())
        .setCrid(imp.getId())
        .setAdid(imp.getId())
        .setPrice(1.0)
        .setAdm("snippet")
        .addExtension(ObExt.bidClickThroughUrl, "https://www.iab.net");
    if (good) {
      bid.setExtension(DcExt.ad, Ad.newBuilder()
          .addAttribute(DoubleClickConstants.CREATIVE_NON_FLASH).build());
    }
    bidResponse.addBid(bid);
  }

  static DoubleClickBidRequestReceiver newReceiver(
      BidInterceptor... interceptors) {

    MetricRegistry metricRegistry = new MetricRegistry();
    return new DoubleClickBidRequestReceiver(
        metricRegistry,
        BiddingTestUtil.newBidController(metricRegistry, interceptors),
        DoubleClickTestUtil.newSnippetProcessor(),
        newMapper(metricRegistry),
        new DoubleClickValidator(metricRegistry, DoubleClickTestUtil.getMetadata()),
        new FakeClock());
  }

  static DoubleClickOpenRtbMapper newMapper(MetricRegistry metricRegistry) {
    return new DoubleClickOpenRtbMapper(
        metricRegistry,
        DoubleClickTestUtil.getMetadata(),
        DoubleClickTestUtil.newOpenRtbJsonFactory(),
        new DoubleClickCrypto.Hyperlocal(DoubleClickTestUtil.ZERO_KEYS),
        ImmutableList.of(DoubleClickLinkMapper.INSTANCE, OpenBidderExtMapper.INSTANCE));
  }
}
