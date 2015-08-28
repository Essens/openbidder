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
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.UUID;
import static java.util.UUID.randomUUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RandomBidInterceptor implements BidInterceptor {

       private final Logger logger = LoggerFactory.getLogger(RandomBidInterceptor.class);

    // To regularly count received requests/second
    private final Timer CounterTimer = new Timer();

    // To regularly update the local cache
    private final Timer CacheTimer = new Timer();

    // Local cache of all the rules fetched from the DB.
    private List<Rule> Rules = new ArrayList<Rule>();

    // Local cache of all creatives fetched from the DB.
    private List<Creative> Creatives = new ArrayList<Creative>();

    // Local cache of all the config values fetched from the DB.
    private List<Config> Configs = new ArrayList<Config>();

    // To build bid responses
    private NativeBidBuilder BidBuilder = new NativeBidBuilder();
    
    public RandomBidInterceptor(){
        
    }
    
    
  @Override
  public void execute(InterceptorChain<BidRequest, BidResponse> chain) {

      for (Imp imp : chain.request().imps()) {

            if (chain.request() != null) {
                
                if(!chain.response().openRtb().hasId()){
                    chain.response().openRtb().setId(randomUUID().toString());
                }
                chain.response().openRtb().setCur("USD"); //Checkk if we use different currency
                
                Bid responseBid = Bid.newBuilder()
                        .setId(randomUUID().toString())
                        .setImpid(imp.getId())
                        .setPrice(imp.getBidfloor())
                        .setAdid("1") //get from database
                        .setNurl("http://m.mobileacademy.com?campaignid=1&forcedPage=727") //get from database
                        .setAdm("<a href=\\\"http://ads.com/click/112770_1386565997\\\"><img src=\\\"http://ads.com/img/112770_1386565997?won=${AUCTION_PRICE}\\\" width=\\\"728\\\" height=\\\"90\\\" border=\\\"0\\\" alt=\\\"Advertisement\\\" /></a>") //get from database
                        .setAdomain(0,"mobileacademy.com")
                        .setIurl("http://m.mobileacademy.com/banerimg.jpg")
                        .setCid("1")//Campaign id
                        .setCrid("1")//Creative id
                        .build();

                //chain.response().addBid(responseBid);             
                chain.response()
                        .seatBid()
                        .setGroup(false) //0 = impressions can be won individually; 1 = impressions must be won or lost as a group.
                        .addBid(responseBid);
                        
            }
        }

        chain.proceed();
    

  }
}
