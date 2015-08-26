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

package com.google.openbidder.bidding.json;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import com.google.openbidder.api.openrtb.ObExt;
import com.google.openbidder.bidding.BidModule;
import com.google.openrtb.OpenRtb.BidRequest;
import com.google.openrtb.OpenRtb.BidRequest.Imp;
import com.google.openrtb.OpenRtb.BidResponse;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid.Bid;
import com.google.openrtb.json.OpenRtbJsonFactory;

import org.junit.Test;

import java.io.IOException;

/**
 * Tests extension support for JSON serialization.
 */
public class JsonTest {
  private final OpenRtbJsonFactory jsonFactory =
      BidModule.registerObExt(OpenRtbJsonFactory.create());

  @Test
  public void testRequest() throws IOException {
    BidRequest req = BidRequest.newBuilder()
        .setId("1")
        .addImp(Imp.newBuilder()
            .setId("1")
            .setExtension(ObExt.impCid, asList("c1", "c2", "c3")))
        .build();

    String json = jsonFactory.newWriter().writeBidRequest(req);
    assertEquals(
        "{\"id\":\"1\",\"imp\":[{\"id\":\"1\",\"ext\":{\"cid\":[\"c1\",\"c2\",\"c3\"]}}]}",
        json);
    assertEquals(req, jsonFactory.newReader().readBidRequest(json));
  }

  @Test
  public void testResponse() throws IOException {
    BidResponse resp = BidResponse.newBuilder()
        .setId("1")
        .setBidid("1")
        .addSeatbid(SeatBid.newBuilder()
            .addBid(Bid.newBuilder()
                .setId("1")
                .setImpid("1")
                .setCid("1")
                .setCrid("1")
                .setPrice(0.1)
                .setAdm("snippet")
                .addExtension(ObExt.bidClickThroughUrl, "https://www.iab.net")
                .addExtension(ObExt.bidImpressionParameter, ObExt.UrlParameter.newBuilder()
                    .setName("i1").setValue("v1").build())
                .addExtension(ObExt.bidImpressionParameter, ObExt.UrlParameter.newBuilder()
                    .setName("i2").setValue("v2").build())
                .addExtension(ObExt.bidClickParameter, ObExt.UrlParameter.newBuilder()
                    .setName("c1").setValue("v3").build())))
        .build();

    String json = jsonFactory.newWriter().writeBidResponse(resp);
    assertEquals(
          "{\"id\":\"1\",\"seatbid\":[{\"bid\":[{\"id\":\"1\",\"impid\":\"1\","
        + "\"price\":0.1,\"adm\":\"snippet\",\"cid\":\"1\",\"crid\":\"1\","
        + "\"ext\":{\"click_through_url\":[\"https://www.iab.net\"],"
        + "\"impression_parameter\":[{\"name\":\"i1\",\"value\":\"v1\"},"
        + "{\"name\":\"i2\",\"value\":\"v2\"}],"
        + "\"click_parameter\":[{\"name\":\"c1\",\"value\":\"v3\"}]}}]}],"
        + "\"bidid\":\"1\"}",
        json);
    assertEquals(resp, jsonFactory.newReader().readBidResponse(json));
  }
}
