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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import com.google.common.collect.Iterables;
import com.google.doubleclick.util.DoubleClickMacros;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.snippet.SnippetMacros;
import com.google.openbidder.api.testing.bidding.BiddingTestUtil;
import com.google.openbidder.config.bid.ClickUrl;
import com.google.openbidder.config.bid.ImpressionUrl;
import com.google.openbidder.exchange.doubleclick.testing.DoubleClickTestUtil;
import com.google.openbidder.exchange.doubleclick.testing.TestBidRequestBuilder;
import com.google.openbidder.exchange.doubleclick.testing.TestBidResponseBuilder;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid.Bid;
import com.google.openrtb.snippet.OpenRtbMacros;
import com.google.openrtb.snippet.SnippetMacroType;
import com.google.openrtb.snippet.SnippetProcessorContext;
import com.google.protos.adx.NetworkBid;

import org.junit.Test;

/**
 * Tests for {@link DoubleClickSnippetProcessor}.
 */
public class DoubleClickSnippetProcessorTest {
  private final DoubleClickSnippetProcessor processor = new DoubleClickSnippetProcessor(
      BiddingTestUtil.DEFAULT_CALLBACK_URL,
      ImpressionUrl.DEFAULT,
      ClickUrl.DEFAULT);

  @Test
  public void testMacro() {
    assertEquals("728", process(SnippetMacros.OB_AD_WIDTH));
    assertEquals("728", process(SnippetMacros.OB_AD_WIDTH));
    assertEquals("90", process(SnippetMacros.OB_AD_HEIGHT));
    assertEquals("90", process(SnippetMacros.OB_AD_HEIGHT));
    assertEquals(DoubleClickMacros.WINNING_PRICE.key(), process(OpenRtbMacros.AUCTION_PRICE));
  }

  @Test
  public void testNullProcessor() {
    String snippet = SnippetMacros.OB_CALLBACK_URL.key();
    assertSame(snippet, process(DoubleClickSnippetProcessor.DC_NULL, SnippetMacros.OB_CALLBACK_URL));
  }

  private String process(SnippetMacroType macro) {
    return process(processor, macro);
  }

  private String process(DoubleClickSnippetProcessor processor, SnippetMacroType macro) {
    String snippet = macro.key();
    BidRequest request = TestBidRequestBuilder.create().setNativeRequest(
        NetworkBid.BidRequest.newBuilder()
            .setId(DoubleClickTestUtil.REQUEST_ID)
            .addAdslot(NetworkBid.BidRequest.AdSlot.newBuilder()
                .setId(1)
                .addAllWidth(asList(111, 728, 222))
                .addAllHeight(asList(111, 90, 222))
                .addMatchingAdData(NetworkBid.BidRequest.AdSlot.MatchingAdData.newBuilder()
                    .setAdgroupId(0)
                    .setMinimumCpmMicros(50))))
        .build();

    BidResponse response = TestBidResponseBuilder.create().build().addBid(Bid.newBuilder()
        .setId("1")
        .setImpid("1")
        .setPrice(1.0)
        .setAdm(snippet)
        .setW(728)
        .setH(90));
    return processor.process(
        new SnippetProcessorContext(
            request.openRtb(), response.openRtb(), Iterables.getOnlyElement(response.bids())),
        snippet);
  }
}
