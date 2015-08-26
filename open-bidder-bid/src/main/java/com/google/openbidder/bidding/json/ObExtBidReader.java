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

import static com.google.openrtb.json.OpenRtbJsonUtils.endArray;
import static com.google.openrtb.json.OpenRtbJsonUtils.endObject;
import static com.google.openrtb.json.OpenRtbJsonUtils.getCurrentName;
import static com.google.openrtb.json.OpenRtbJsonUtils.startArray;
import static com.google.openrtb.json.OpenRtbJsonUtils.startObject;

import com.google.openbidder.api.openrtb.ObExt;
import com.google.openbidder.api.openrtb.ObExt.UrlParameter;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid.Bid;
import com.google.openrtb.json.OpenRtbJsonExtReader;
import com.google.protobuf.Message;

import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;

public class ObExtBidReader
extends OpenRtbJsonExtReader<Bid.Builder, Message.Builder> {

  public ObExtBidReader() {
    super(ObExt.bidClickThroughUrl);
  }

  @Override protected void read(Bid.Builder msg, JsonParser par)
      throws IOException {
    switch (getCurrentName(par)) {
      case "click_through_url":
        for (startArray(par); endArray(par); par.nextToken()) {
          msg.addExtension(ObExt.bidClickThroughUrl, par.getText());
        }
        break;
      case "impression_parameter":
        for (startArray(par); endArray(par); par.nextToken()) {
          msg.addExtension(ObExt.bidImpressionParameter, readUrlParameter(par));
        }
        break;
      case "click_parameter":
        for (startArray(par); endArray(par); par.nextToken()) {
          msg.addExtension(ObExt.bidClickParameter, readUrlParameter(par));
        }
        break;
    }
  }

  public UrlParameter readUrlParameter(JsonParser par) throws IOException {
    UrlParameter.Builder urlParam = UrlParameter.newBuilder();
    for (startObject(par); endObject(par); par.nextToken()) {
      switch (getCurrentName(par)) {
        case "name":
          urlParam.setName(par.nextTextValue());
          break;
        case "value":
          urlParam.setValue(par.nextTextValue());
          break;
      }
    }
    return urlParam.build();
  }
}
