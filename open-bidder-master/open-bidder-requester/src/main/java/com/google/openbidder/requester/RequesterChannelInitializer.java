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

import com.google.common.base.Preconditions;
import com.google.openbidder.config.client.ClientLogging;
import com.google.openbidder.config.client.Uri;
import com.google.openbidder.config.server.MaxContentLength;
import com.google.openbidder.netty.client.NettyHttpClientCodec;
import com.google.openbidder.netty.common.NettyErrorHandler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.logging.LoggingHandler;

import java.net.URI;

import javax.inject.Inject;

/**
 * Request channel initialization.
 */
public class RequesterChannelInitializer extends ChannelInitializer<SocketChannel> {
  private static final LoggingHandler loggingHandler = new LoggingHandler();
  private final int maxContentLength;
  private final boolean clientLogging;
  private final BidSender bidSender;

  @Inject
  public RequesterChannelInitializer(
      @Uri URI uri,
      @MaxContentLength int maxContentLength,
      @ClientLogging boolean clientLogging) {

    Preconditions.checkNotNull(uri);
    Preconditions.checkArgument("http".equals(uri.getScheme()),
        "Invalid protocol '%s', must be HTTP", uri.getScheme());
    this.bidSender = new BidSender(uri);
    this.maxContentLength = maxContentLength;
    this.clientLogging = clientLogging;
  }

  @Override
  protected void initChannel(SocketChannel ch) {
    ChannelPipeline pipeline = ch.pipeline();
    if (clientLogging) {
      pipeline.addLast("logging", loggingHandler);
    }
    pipeline.addLast("http", new HttpClientCodec());
    pipeline.addLast("aggregate", new HttpObjectAggregator(maxContentLength));
    pipeline.addLast("obHttp", NettyHttpClientCodec.INSTANCE);
    pipeline.addLast("sender", bidSender);
    pipeline.addLast("receiver", BidReceiver.INSTANCE);
    pipeline.addLast("error", NettyErrorHandler.INSTANCE);
  }
}
