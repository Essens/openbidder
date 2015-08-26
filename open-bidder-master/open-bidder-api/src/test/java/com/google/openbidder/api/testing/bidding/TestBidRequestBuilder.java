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

package com.google.openbidder.api.testing.bidding;

import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.openrtb.ObExt;
import com.google.openbidder.http.HttpRequest;
import com.google.openbidder.http.request.StandardHttpRequest;
import com.google.openrtb.OpenRtb;
import com.google.openrtb.OpenRtb.BidRequest.Impression;
import com.google.openrtb.OpenRtb.BidRequest.Impression.Banner;

/**
 * Extends {@link com.google.openbidder.api.bidding.BidRequest.Builder}
 * with additional features and defaults for unit testing on generic exchanges.
 */
public class TestBidRequestBuilder extends BidRequest.Builder {
  private static final HttpRequest DEFAULT_REQUEST = StandardHttpRequest.newBuilder()
      .setMethod("GET")
      .setUri("http://localhost")
      .build();

  protected TestBidRequestBuilder() {
    setHttpRequest(DEFAULT_REQUEST);
    setExchange(defaultExchange());
  }

  public static TestBidRequestBuilder create() {
    return new TestBidRequestBuilder();
  }

  /**
   * Populates bid request data from an easy, ad-hoc dataset.
   *
   * @param id {@link BidRequest} ID.
   * @param adCidMincpm Sequence of numbers of size multiple of 3, each triplet is a tuple
   *        {@code (adId, adgroupId, minimumCpm)}.
   * @return Request with one AdSlot per {@code adGroupMincpm} tuple, size 728x90.
   */
  public TestBidRequestBuilder setRequest(String id, Object... adCidMincpm) {
    OpenRtb.BidRequest.Builder request = getRequest();

    if (request == null) {
      setRequest(request = OpenRtb.BidRequest.newBuilder());
    }

    request.setId(id);

    for (int i = 0; i < adCidMincpm.length; i += 3) {
      String adid = String.valueOf(adCidMincpm[i + 0]);
      request.addImp(Impression.newBuilder()
          .setId(adid)
          .setBidfloor(((Number) adCidMincpm[i + 2]).floatValue())
          .setBanner(Banner.newBuilder()
              .setId(adid)
              .setW(728)
              .setH(90))
          .setExtension(ObExt.imp, ObExt.Impression.newBuilder()
              .addCid(String.valueOf(adCidMincpm[i + 1])).build()));
    }

    return this;
  }

  @Override
  public BidRequest build() {
    OpenRtb.BidRequest.Builder request = getRequest();

    if (request == null) {
      setRequest(request = OpenRtb.BidRequest.newBuilder());
    }

    if (!request.hasId()) {
      request.setId("1");
    }

    return super.build();
  }
}
