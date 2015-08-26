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

package com.google.openbidder.netty.server;

import com.google.common.collect.ImmutableMap;
import com.google.openbidder.config.server.BidderAdminPort;
import com.google.openbidder.config.server.BidderListenPort;
import com.google.openbidder.config.server.MaxContentLength;
import com.google.openbidder.config.server.ServerLogging;
import com.google.openbidder.http.route.HttpRouter;
import com.google.openbidder.netty.common.NettyErrorHandler;
import com.google.openbidder.netty.server.config.UserGroup;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.EventExecutorGroup;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Sets up the server's channel pipeline.
 */
public class NettyChannelInitializer extends ChannelInitializer<SocketChannel> {
  private static final LoggingHandler loggingHandler = new LoggingHandler();
  private final ImmutableMap<Integer, NettyHttpRouter> httpRouters;
  private final int maxContentLength;
  private final boolean serverLogging;
  private final EventExecutorGroup userGroup;

  @Inject
  public NettyChannelInitializer(
      @BidderListenPort int listenPort,
      @BidderListenPort HttpRouter listenRouter,
      @BidderAdminPort int adminPort,
      @BidderAdminPort HttpRouter adminRouter,
      @UserGroup @Nullable EventExecutorGroup userGroup,
      @MaxContentLength int maxContentLength,
      @ServerLogging boolean serverLogging) {

    this.httpRouters = ImmutableMap.of(
        listenPort, new NettyHttpRouter(listenRouter),
        adminPort, new NettyHttpRouter(adminRouter));
    this.userGroup = userGroup;
    this.maxContentLength = maxContentLength;
    this.serverLogging = serverLogging;
  }

  @Override
  protected void initChannel(SocketChannel ch) {
    ChannelPipeline pipeline = ch.pipeline();
    if (serverLogging) {
      pipeline.addLast("logging", loggingHandler);
    }
    pipeline.addLast("http", new HttpServerCodec());
    pipeline.addLast("aggregate", new HttpObjectAggregator(maxContentLength));
    pipeline.addLast("obHttp", new NettyHttpServerCodec());
    int port = ch.localAddress() == null
        // Parent will be a ServerSocketChannel; no need to look further up
        ? ch.parent().localAddress().getPort()
        : ch.localAddress().getPort();
    if (userGroup == null) {
      pipeline.addLast("router", httpRouters.get(port));
    } else {
      pipeline.addLast(userGroup, "router", httpRouters.get(port));
    }
    pipeline.addLast("error", NettyErrorHandler.INSTANCE);
  }
}
