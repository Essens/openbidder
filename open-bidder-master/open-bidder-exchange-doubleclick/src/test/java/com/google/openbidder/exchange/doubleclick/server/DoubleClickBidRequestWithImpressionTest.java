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

import com.google.api.client.util.escape.CharEscapers;
import com.google.common.collect.ImmutableList;
import com.google.doubleclick.util.DoubleClickMacros;
import com.google.openbidder.api.bidding.BidInterceptor;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.interceptor.InterceptorChain;
import com.google.openbidder.api.openrtb.ObExt;
import com.google.openbidder.config.bid.ClickUrl;
import com.google.openbidder.config.bid.ImpressionUrl;
import com.google.openbidder.config.impression.PriceName;
import com.google.openbidder.exchange.doubleclick.testing.DoubleClickTestUtil;
import com.google.openbidder.http.HttpRequest;
import com.google.openbidder.http.HttpResponse;
import com.google.openbidder.http.receiver.DefaultHttpReceiverContext;
import com.google.openbidder.http.response.StandardHttpResponse;
import com.google.openrtb.OpenRtb.BidRequest.Impression;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid.Bid;
import com.google.protos.adx.NetworkBid;

import com.codahale.metrics.MetricRegistry;

import org.apache.http.HttpStatus;
import org.junit.Test;

import java.io.IOException;

/**
 * Tests {@link DoubleClickBidRequestReceiver}, with impression in the response.
 */
public class DoubleClickBidRequestWithImpressionTest {

  @Test
  public void testHttpRequest() throws IOException {
    HttpRequest httpRequest = DoubleClickTestUtil.newHttpRequest(TestData.newRequest(false));
    HttpResponse.Builder httpResponse = StandardHttpResponse.newBuilder();
    DoubleClickBidRequestReceiver receiver = DoubleClickTestUtil.newReceiver(
        new DoubleClickSnippetProcessor(
            "http://myzone.mybidders.com",
            ImpressionUrl.DEFAULT,
            ClickUrl.DEFAULT),
        new MetricRegistry(),
        new FixedCpmWithTrackingInterceptor());
    receiver.receive(new DefaultHttpReceiverContext(httpRequest, httpResponse));
    assertEquals(HttpStatus.SC_OK, httpResponse.getStatusCode());
    assertEquals(ImmutableList.of(NetworkBid.BidResponse.Ad.newBuilder()
        .addAdslot(NetworkBid.BidResponse.Ad.AdSlot.newBuilder()
            .setId(1)
            .setMaxCpmMicros(100_000))
        .addClickThroughUrl("https://www.iab.net")
        .setBuyerCreativeId("1")
        .setHtmlSnippet(
            "<a target='_blank' href='%%CLICK_URL_ESC%%"
              + CharEscapers.escapeUri("http://myzone.mybidders.com/click"
                + "?param2=value2&ad_url=https://www.iab.net") + "'>\n"
            + "<img src='http://mycontentserver.com/creative?id=ABCD'>\n"
            + "<img src='http://myzone.mybidders.com/impression"
              + "?price=%%WINNING_PRICE%%&param1=value1'>\n"
          + "</a>")
        .build()),
        NetworkBid.BidResponse.parseFrom(httpResponse.build().content()).getAdList());
  }

  public static class FixedCpmWithTrackingInterceptor implements BidInterceptor {
    @Override
    public void execute(InterceptorChain<BidRequest, BidResponse> chain) {
      for (Impression imp : chain.request().imps()) {
        chain.response().addBid(Bid.newBuilder()
            .setId(imp.getId())
            .setImpid(imp.getId())
            .setCid("10")
            .setCrid("1")
            .setAdid("1")
            .setPrice(0.1)
            .setAdm(
                "<a target='_blank' href='%%CLICK_URL_ESC%%"
                + "%{${OB_CLICK_URL}&ad_url=${OB_AD_CLICKTHROUGH_URL}}%'>\n"
                + "<img src='http://mycontentserver.com/creative?id=ABCD'>\n"
                + "<img src='${OB_IMPRESSION_URL}'>\n"
              + "</a>")
            .setExtension(ObExt.bid, ObExt.Bid.newBuilder()
              .addClickThroughUrl("https://www.iab.net")
              .addImpressionParameter(ObExt.Bid.UrlParameter.newBuilder()
                  .setName(PriceName.DEFAULT).setValue(DoubleClickMacros.WINNING_PRICE.key()))
              .addImpressionParameter(ObExt.Bid.UrlParameter.newBuilder()
                  .setName("param1").setValue("value1"))
              .addClickParameter(ObExt.Bid.UrlParameter.newBuilder()
                  .setName("param2").setValue("value2")).build()));
      }

      chain.proceed();
    }
  }
}
