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

package com.google.openbidder.requester;

import com.google.openbidder.http.HttpResponse;
import com.google.protos.adx.NetworkBid.BidResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class BidReceiver extends SimpleChannelInboundHandler<HttpResponse> {
  private static final Logger logger = LoggerFactory.getLogger(BidReceiver.class);
  public static final BidReceiver INSTANCE = new BidReceiver();

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, HttpResponse msg) throws Exception {
    if (msg.isOk()) {
      BidResponse bidResponse = BidResponse.parseFrom(msg.content());
      if (logger.isDebugEnabled()) {
        logger.debug("Received {}", bidResponse);
      }
      RequesterTool.responsesReceived.incrementAndGet();
    }
  }
}
