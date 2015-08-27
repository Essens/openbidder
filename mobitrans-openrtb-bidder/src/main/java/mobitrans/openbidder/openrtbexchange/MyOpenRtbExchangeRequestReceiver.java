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

package mobitrans.openbidder.openrtbexchange;

import com.google.openbidder.api.bidding.BidController;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.interceptor.RequestReceiver;
import com.google.openbidder.http.HttpReceiverContext;
import com.google.openrtb.json.OpenRtbJsonFactory;
import com.google.openrtb.json.OpenRtbJsonReader;
import com.google.openrtb.json.OpenRtbJsonWriter;
import com.google.openrtb.snippet.OpenRtbSnippetProcessor;

import com.codahale.metrics.MetricRegistry;

import org.apache.http.HttpStatus;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton public class MyOpenRtbExchangeRequestReceiver
extends RequestReceiver<BidController> {
  private final OpenRtbSnippetProcessor snippetProcessor;
  private final OpenRtbJsonReader jsonReader;
  private final OpenRtbJsonWriter jsonWriter;

  @Inject public MyOpenRtbExchangeRequestReceiver(
      MetricRegistry metricsRegistry,
      BidController controller,
      OpenRtbSnippetProcessor snippetProcessor,
      OpenRtbJsonFactory openrtbJsonFactory) {
    super(MyOpenRtbExchange.INSTANCE, metricsRegistry, controller);
    this.snippetProcessor = snippetProcessor;
    this.jsonReader = openrtbJsonFactory.newReader();
    this.jsonWriter = openrtbJsonFactory.newWriter();
  }

  @Override public void receive(HttpReceiverContext ctx) {
    try {
      // Step 1. Map the HTTP request to an Open Bidder bid request
      BidRequest request = BidRequest.newBuilder()
          .setExchange(MyOpenRtbExchange.INSTANCE)
          .setHttpRequest(ctx.httpRequest())
          .setRequest(jsonReader.readBidRequest(ctx.httpRequest().content()))
          .build();
      setRequestId(request.openRtb().getId());

      // Step 2. Execute the Open Bidder interceptor stack on the bid request
      BidResponse response = BidResponse.newBuilder()
          .setExchange(MyOpenRtbExchange.INSTANCE)
          .setHttpResponse(ctx.httpResponse())
          .build();
      controller().onRequest(request, response);

      // Step 3. Map the Open Bidder BidResponse back to the exchange's format.
      snippetProcessor.process(request.openRtb(), response.openRtb());
      if (!response.openRtb().hasId()) {
        response.openRtb().setId(request.openRtb().getId());
      }
      ctx.httpResponse().printContent(jsonWriter.writeBidResponse(response.openRtb().build()));

      successResponseMeter().mark();
    } catch (IOException e) {
      logger.warn(e.toString());
      ctx.httpResponse().setStatusCode(HttpStatus.SC_NO_CONTENT);
    } finally {
      clearRequestId();
    }
  }
}
