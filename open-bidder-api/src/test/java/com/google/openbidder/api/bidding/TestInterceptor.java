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

package com.google.openbidder.api.bidding;

import static java.util.Arrays.asList;

import com.google.openbidder.api.interceptor.InterceptorChain;
import com.google.openrtb.OpenRtb.BidRequest.Imp;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid.Bid;
import com.google.openrtb.OpenRtb.CreativeAttribute;

/**
 * Simple interceptor for tests in this package.
 */
public class TestInterceptor implements BidInterceptor {
  @Override
  public void execute(InterceptorChain<BidRequest, BidResponse> chain) {

    for (Imp imp : chain.request().imps()) {
      chain.response().openRtb()
        .setId("1234567890")
        .addSeatbid(SeatBid.newBuilder()
            .addBid(Bid.newBuilder()
                .setId(imp.getId())
                .setImpid(imp.getId())
                .setPrice(9.43)
                .setAdid("314")
                .setNurl("http://adserver.com/winnotice?impid=102")
                .setAdm(
                      "%3C!DOCTYPE%20html%20PUBLIC%20%5C%22-"
                    + "%2F%2FW3C%2F%2FDTD%20XHTML%201.0%20Transitional%2F%2FEN%5C%22%20%5C%22htt"
                    + "p%3A%2F%2Fwww.w3.org%2FTR%2Fxhtml1%2FDTD%2Fxhtml1-"
                    + "transitional.dtd%5C%22%3E%3Chtml%20xmlns%3D%5C%22http%3A%2F%2Fwww.w3.org%2F1"
                    + "999%2Fxhtml%5C%22%20xml%3Alang%3D%5C%22en%5C%22%20lang%3D%5C%22en%5C%22"
                    + "%3E...%3C%2Fhtml%3E")
                .addAdomain("advertiserdomain.com")
                .setIurl("http://adserver.com/pathtosampleimage")
                .setCid("campaign111")
                .setCrid("creative112")
                .addAllAttr(asList(CreativeAttribute.ANNOYING, CreativeAttribute.AUDIO_AUTO_PLAY)))
            .setSeat("512"))
        .setBidid("abc1123")
        .setCur("USD");
    }

    chain.proceed();
  }
}
