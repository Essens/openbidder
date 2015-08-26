/*
 * Copyright 2015 Google Inc. All Rights Reserved.
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

package mobitrans.openbidder.exchange;

import com.google.common.reflect.TypeToken;
import com.google.gson.FieldNamingStrategy;
import com.google.openrtb.OpenRtb;
import com.google.openrtb.OpenRtb.BidRequest;
import com.google.openrtb.OpenRtb.BidResponse;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid.Bid;
import com.google.openrtb.mapper.OpenRtbMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.openrtb.json.OpenRtbJsonExtReader;
import com.google.openrtb.json.OpenRtbJsonFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;

import javax.inject.Singleton;

@Singleton public class MyExchangeOpenRtbMapper
implements OpenRtbMapper<String, String, StringBuilder, StringBuilder> {

  @Override public StringBuilder toExchangeBidResponse(BidRequest request, BidResponse response) {
    // Response format: "id;price;snippet;" -- repeated for each bid
    StringBuilder sb = new StringBuilder();
    for (SeatBid seatBid : response.getSeatbidList()) {
      for (Bid bid : seatBid.getBidList()) {
        sb.append(bid.getImpid()).append(';');
        sb.append(bid.getPrice()).append(';');
        sb.append(bid.getAdm()).append(';');
      }
    }
    return sb;
  }

  @Override public OpenRtb.BidRequest.Builder toOpenRtbBidRequest(String nativeRequest) {
    // Request format: "id;width;height" -- supports a single impression
   
           
            
                Gson gson = new Gson();
                //fromJson
                mobitrans.openbidder.entities.BidRequest bidRequest = gson.fromJson(nativeRequest, mobitrans.openbidder.entities.BidRequest.class);
                
                //OpenRtb.BidRequest.parseFrom(data)
                
                return null;
                  
      }
//    return OpenRtb.BidRequest.newBuilder()
//        .setId("1")
//        .addImp(Imp.newBuilder().setId(fields[0])
//            .setBanner(Banner.newBuilder()
//                .setW(Integer.parseInt(fields[1]))
//                .setH(Integer.parseInt(fields[2])).build()));
  

  @Override public StringBuilder toExchangeBidRequest(BidRequest request) 
  {
    throw new UnsupportedOperationException();
  }

  @Override public BidResponse.Builder toOpenRtbBidResponse(String request, String response) {
    throw new UnsupportedOperationException();
  }
}
