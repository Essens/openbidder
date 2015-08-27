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

package mobitrans.openbidder.bidding;

import static java.lang.Math.random;

import com.google.openbidder.api.bidding.BidInterceptor;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.interceptor.InterceptorChain;
import com.google.openrtb.OpenRtb.BidRequest.Imp;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid.Bid;
import java.util.UUID;
import static java.util.UUID.randomUUID;

/**
 * Dummy example interceptor: will make a random bid for all impressions.
 * Totally useless, but shows how to create a simple interceptor.
 */
public class RandomBidInterceptor implements BidInterceptor {

  @Override
  public void execute(InterceptorChain<BidRequest, BidResponse> chain) {

    /*** PRE-PROCESSING STEP ***/


        for (Imp imp : chain.request().imps()) {
            double price = imp.getBidfloor();

            if (chain.request() != null) {
                UUID bidId = randomUUID();
                Bid responseBid = Bid.newBuilder()
                        .setId(bidId.toString())
                        .setImpid(imp.getId())
                        .setPrice(price)
                        //.setAdm("snippet")
                        .build();

                chain.response().addBid(responseBid);             

                //CounterCache.BidsPerSecondCounter.incrementAndGet();
            }
        }

        //CounterCache.RequestsPerSecondCounter.incrementAndGet();
      
      chain.proceed();
      
      /* DECIDE TO BID OR NOT */


    /*** POST-PROCESSING STEP ***/

    // This interceptor doesn't have any post-processing.
  }
}
