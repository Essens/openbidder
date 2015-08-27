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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.google.openrtb.OpenRtb.BidRequest.Site;
import com.google.openrtb.json.OpenRtbJsonExtReader;
import com.google.openrtb.json.OpenRtbJsonFactory;
import com.googlecode.protobuf.format.JsonFormat;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

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
                
                
//                OpenRtb.BidRequest.Imp.newBuilder().set
//                private Object id_;
//            private List<Imp> imp_;
//            private RepeatedFieldBuilder<Imp, Imp.Builder, ImpOrBuilder> impBuilder_;
//            private Site site_;
//            private SingleFieldBuilder<Site, Site.Builder, SiteOrBuilder> siteBuilder_;
//            private App app_;
//            private SingleFieldBuilder<App, App.Builder, AppOrBuilder> appBuilder_;
//            private Device device_;
//            private SingleFieldBuilder<Device, Device.Builder, DeviceOrBuilder> deviceBuilder_;
//            private User user_;
//            private SingleFieldBuilder<User, User.Builder, UserOrBuilder> userBuilder_;
//            private boolean test_;
//            private AuctionType at_;
//            private int tmax_;
//            private LazyStringList wseat_;
//            private boolean allimps_;
//            private LazyStringList cur_;
//            private List<ContentCategory> bcat_;
//            private LazyStringList badv_;
//            private Regs regs_;
//            private SingleFieldBuilder<Regs, Regs.Builder, RegsOrBuilder> regsBuilder_;
//                
                BidRequest.Device deviceObj =   BidRequest.Device.newBuilder()
                                                .setUa(bidRequest.getDevice().getUa())
                                                .setCarrier(nativeRequest)
                        .setDevicetype(BidRequest.Device.DeviceType.PHONE)
                        .setH(1)
                        .setW(1)
                        .setIp(bidRequest.getDevice().getIp())
                        .setOs(bidRequest.getDevice().getOs())
                        .setMake(bidRequest.getDevice().getMake())
                        .build();
               //BidRequest.Imp impObj = BidRequest.Imp.newBuilder()
                
                return OpenRtb.BidRequest.newBuilder()
                        .setId(bidRequest.getId())
                        .setSite(Site.newBuilder().setDomain(bidRequest.getSite().getDomain()))
                        .setDevice(deviceObj);
                        //.setDevice(bidRequest.getDevice())
                        
                        //.setImp(OpenRtb.BidRequest.Imp.newBuilder().set, null)
//                        ;
 
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
