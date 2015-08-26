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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.util.concurrent.AbstractIdleService;
import com.google.openbidder.config.client.ConnectionTimeout;
import com.google.openbidder.netty.client.config.ClientConnectionChannel;
import com.google.openbidder.netty.client.config.ClientGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;

import java.net.InetSocketAddress;
import java.net.URI;

import javax.inject.Inject;

/**
 * Requester client.
 */
public class RequesterClient extends AbstractIdleService {

  private static final Logger logger = LoggerFactory.getLogger(RequesterClient.class);

  private final EventLoopGroup bossGroup;
  private final Class<? extends Channel> socketChannel;
  private final ChannelInitializer<Channel> channelInitializer;
  private final URI uri;
  private final int connectionTimeout;

  @Inject
  public RequesterClient(
      @ClientGroup EventLoopGroup clientGroup,
      @ClientConnectionChannel Class<? extends Channel> socketChannel,
      ChannelInitializer<Channel> channelInitializer,
      @BidderUrl URI uri,
      @ConnectionTimeout int connectionTimeout) {

    this.bossGroup = checkNotNull(clientGroup);
    this.socketChannel = checkNotNull(socketChannel);
    this.channelInitializer = checkNotNull(channelInitializer);
    this.uri = checkNotNull(uri);
    this.connectionTimeout = connectionTimeout;
  }

  @Override
  protected void startUp() throws Exception {
    Bootstrap bootstrap = new Bootstrap();
    bootstrap.group(bossGroup)
        .channel(socketChannel)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout)
        .handler(channelInitializer);
    final InetSocketAddress address = new InetSocketAddress(uri.getHost(), uri.getPort());
    logger.info("Connecting to {}", address);
    ChannelFuture channelFuture = bootstrap.connect(address);
    channelFuture.addListener(new ChannelFutureListener() {
      @Override public void operationComplete(ChannelFuture future) throws Exception {
        if (future.isSuccess()) {
          logger.info("Successfully connected to {}", address);
        }
      }});
    channelFuture.sync().channel().closeFuture().sync();
  }

  @Override
  protected void shutDown() throws Exception {
    bossGroup.shutdownGracefully();
  }
}
