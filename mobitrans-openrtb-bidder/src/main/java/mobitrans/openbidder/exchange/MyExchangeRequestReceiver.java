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

import com.google.doubleclick.openrtb.MapperException;
import com.google.openbidder.api.bidding.BidController;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.interceptor.RequestReceiver;
import com.google.openbidder.http.HttpReceiverContext;
import com.google.openbidder.http.util.HttpUtil;
import com.google.openrtb.mapper.OpenRtbMapper;
import com.google.openrtb.snippet.OpenRtbSnippetProcessor;
import com.codahale.metrics.MetricRegistry;
import com.google.openrtb.json.OpenRtbJsonFactory;
import com.google.protobuf.ByteString;
import org.apache.http.HttpStatus;
import javax.inject.Inject;
import javax.inject.Singleton;
import com.google.openrtb.json.*;
import com.google.openrtb.snippet.SnippetProcessor;
import mobitrans.openbidder.openrtbexchange.MyOpenRtbExchangeExtBannerReader;
import mobitrans.openbidder.openrtbexchange.MyOpenRtbExchangeExtBannerWriter;
import com.fasterxml.jackson.core.*;


@Singleton public class MyExchangeRequestReceiver
extends RequestReceiver<BidController> {
  private final OpenRtbSnippetProcessor snippetProcessor;
  private final OpenRtbMapper<String, String, StringBuilder, StringBuilder> mapper;
 
  @Inject public MyExchangeRequestReceiver(
      MetricRegistry metricsRegistry,
      BidController controller,
      OpenRtbSnippetProcessor snippetProcessor,
      OpenRtbMapper<String, String, StringBuilder, StringBuilder> mapper) {
    super(MyExchange.INSTANCE, metricsRegistry, controller);
    this.snippetProcessor = snippetProcessor;
    this.mapper = mapper;
  }

  @Override public void receive(HttpReceiverContext ctx) {
    try {
      // Step 1. Map the HTTP request to an Open Bidder bid request
      String nativeRequest = HttpUtil.readContentString(ctx.httpRequest());
      BidRequest request = BidRequest.newBuilder()
          .setExchange(MyExchange.INSTANCE)
          .setHttpRequest(ctx.httpRequest())
          .setNativeRequest(nativeRequest)
          .setRequest(mapper.toOpenRtbBidRequest(nativeRequest))
          .build();
      
      setRequestId(request.openRtb().getId());

      // Step 2. Execute the Open Bidder interceptor stack on the bid request
      BidResponse response = BidResponse.newBuilder()
          .setExchange(MyExchange.INSTANCE)
          .setHttpResponse(ctx.httpResponse())
          .build();
      controller().onRequest(request, response);

      // Step 3. Map the Open Bidder BidResponse back to the exchange's format.
      snippetProcessor.process(request.openRtb(), response.openRtb());
      if (response.getResponseMode() == BidResponse.ResponseMode.OPENRTB) {
        if (!response.openRtb().hasId()) {
          response.openRtb().setId(request.openRtb().getId());
        }
        //ctx.httpResponse().printContent(mapper.toExchangeBidResponse(request.openRtb(), response.openRtb().build()).toString());
        // How to serialize
        //https://github.com/google/openrtb/wiki
        OpenRtbJsonFactory openrtbJson = OpenRtbJsonFactory.create();
        String jsonRes = openrtbJson.newWriter().writeBidResponse(response.openRtb().build());
        ctx.httpResponse().printContent(jsonRes);
        
      } else {
        ctx.httpResponse().printContent(((StringBuilder) response.nativeResponse()).toString());
      }

      successResponseMeter().mark();
    } catch (MapperException e) {
      logger.warn(e.toString());
      ctx.httpResponse().setStatusCode(HttpStatus.SC_NO_CONTENT);
    }
    catch ( Exception ex)
    {
        logger.warn(ex.toString());
    }
    
    finally {
      clearRequestId();
    }
  }
}
