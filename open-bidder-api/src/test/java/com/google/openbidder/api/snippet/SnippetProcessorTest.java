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

package com.google.openbidder.api.snippet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import com.google.common.collect.Iterables;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.openrtb.ObExt;
import com.google.openbidder.api.testing.bidding.TestBidRequestBuilder;
import com.google.openbidder.api.testing.bidding.TestBidResponseBuilder;
import com.google.openrtb.OpenRtb;
import com.google.openrtb.OpenRtb.BidRequest.Imp;
import com.google.openrtb.OpenRtb.BidRequest.Imp.Banner;
import com.google.openrtb.OpenRtb.BidRequest.Imp.Video;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid.Bid;
import com.google.openrtb.snippet.SnippetMacroType;
import com.google.openrtb.snippet.SnippetProcessor;
import com.google.openrtb.snippet.SnippetProcessorContext;
import com.google.openrtb.snippet.UndefinedMacroException;

import org.junit.Test;

/**
 * Tests for {@link SnippetProcessor}.
 */
public class SnippetProcessorTest {
  private final StandardSnippetProcessor processor = new StandardSnippetProcessor(
      "http://localhost",
      SnippetMacros.OB_CALLBACK_URL.key() + "/impression",
      SnippetMacros.OB_CALLBACK_URL.key() + "/click");

  @Test
  public void testMacro() {
    assertNotNull(processor.toString());

    assertEquals("http://localhost", process(SnippetMacros.OB_CALLBACK_URL));
    assertEquals("728", process(SnippetMacros.OB_AD_WIDTH, false, false));
    assertEquals("90", process(SnippetMacros.OB_AD_HEIGHT, false, false));
    assertEquals("640", process(SnippetMacros.OB_AD_WIDTH, true, false));
    assertEquals("480", process(SnippetMacros.OB_AD_HEIGHT, true, false));
    assertEquals("200", process(SnippetMacros.OB_AD_WIDTH, true, true));
    assertEquals("100", process(SnippetMacros.OB_AD_HEIGHT, true, true));
    assertEquals("https://www.iab.net", process(SnippetMacros.OB_AD_CLICKTHROUGH_URL));
    assertEquals("https://mycontent.com/creative.png", process(SnippetMacros.OB_AD_CREATIVE_URL));

    assertEquals("http://localhosthttp://localhost",
        process("${OB_CALLBACK_URL}${OB_CALLBACK_URL}", false, true));
    assertEquals("http://localhost/impression?p1=" + esc("http://localhost"),
        process(SnippetMacros.OB_IMPRESSION_URL));
    assertEquals("http://localhost/impression",
        process(SnippetMacros.OB_IMPRESSION_URL, false, false));
    assertEquals("http://localhost/click?p2=" + esc("http://mycontent"),
        process(SnippetMacros.OB_CLICK_URL));
    assertEquals("http://localhost/click",
        process(SnippetMacros.OB_CLICK_URL, false, false));
  }

  @Test
  public void testNullProcessor() {
    String snippet = SnippetMacros.OB_CALLBACK_URL.key();
    assertSame(snippet, process(StandardSnippetProcessor.STD_NULL, snippet, false, false));
  }

  @Test(expected = UndefinedMacroException.class)
  public void testMissingCallbackUrl() {
    testMissing(SnippetMacros.OB_CALLBACK_URL, "", "x", "x");
  }

  @Test(expected = UndefinedMacroException.class)
  public void testMissingImpressionUrl() {
    testMissing(SnippetMacros.OB_IMPRESSION_URL, "x", "", "x");
  }

  @Test(expected = UndefinedMacroException.class)
  public void testMissingClickUrl() {
    testMissing(SnippetMacros.OB_CLICK_URL, "x", "x", "");
  }

  @Test(expected = UndefinedMacroException.class)
  public void testMissingClickthroughUrl() {
    process(SnippetMacros.OB_AD_CLICKTHROUGH_URL.key(), false, false);
  }

  @Test(expected = UndefinedMacroException.class)
  public void testMissingCreativeUrl() {
    process(SnippetMacros.OB_AD_CREATIVE_URL.key(), false, false);
  }

  private void testMissing(
      SnippetMacroType macro, String callbackUrl, String impressionUrl, String clickUrl)  {
    StandardSnippetProcessor processor =
        new StandardSnippetProcessor(callbackUrl, impressionUrl, clickUrl);
    BidRequest request = TestBidRequestBuilder.create().setRequest("1").build();
    BidResponse response = createBidResponse(macro.key(), false);
    processor.process(
        new SnippetProcessorContext(
            request.openRtb(), response.openRtb(), Iterables.getOnlyElement(response.bids())),
        macro.key());
  }

  @Test
  public void testUndefinedMacroException() {
    UndefinedMacroException e = new UndefinedMacroException(SnippetMacros.OB_CALLBACK_URL);
    assertSame(SnippetMacros.OB_CALLBACK_URL, e.key());
  }

  private String process(SnippetMacroType macro) {
    return process(macro.key(), false, true);
  }

  private String process(SnippetMacroType macro, boolean video, boolean full) {
    return process(processor, macro.key(), video, full);
  }

  private String process(String snippet, boolean video, boolean full) {
    return process(processor, snippet, video, full);
  }

  private static String process(
      SnippetProcessor processor, String snippet, boolean video, boolean full) {
    OpenRtb.BidRequest.Imp.Builder imp = Imp.newBuilder()
        .setId("1")
        .setBidfloor(1.2)
        .addExtension(ObExt.impCid, String.valueOf("3"));
    if (video) {
      imp.setVideo(Video.newBuilder()
          .setW(640)
          .setH(480));
    } else {
      imp.setBanner(Banner.newBuilder()
          .setId("1")
          .setW(728)
          .setH(90));
    }
    BidRequest request = TestBidRequestBuilder.create()
        .setRequest(OpenRtb.BidRequest.newBuilder().addImp(imp)).build();
    BidResponse response = createBidResponse(snippet, full);
    return processor.process(
        new SnippetProcessorContext(
            request.openRtb(), response.openRtb(), Iterables.getOnlyElement(response.bids())),
        snippet);
  }

  private static BidResponse createBidResponse(String snippet, boolean full) {
    Bid.Builder bid = Bid.newBuilder()
        .setId("bid1")
        .setImpid("1")
        .setPrice(1.0);
    if (full) {
      bid
          .setAdid("ad1")
          .setAdm(snippet)
          .setW(200)
          .setH(100)
          .setIurl("https://mycontent.com/creative.png")
          .addExtension(ObExt.bidClickThroughUrl, "https://www.iab.net")
          .addExtension(ObExt.bidImpressionParameter, ObExt.UrlParameter.newBuilder()
              .setName("p1").setValue("%{http://localhost}%").build())
          .addExtension(ObExt.bidClickParameter, ObExt.UrlParameter.newBuilder()
              .setName("p2").setValue("%{http://mycontent}%").build());
    }
    return TestBidResponseBuilder.create().build().addBid("seat1", bid);
  }

  private static String esc(String s) {
    return SnippetProcessor.getEscaper().escape(s);
  }
}
