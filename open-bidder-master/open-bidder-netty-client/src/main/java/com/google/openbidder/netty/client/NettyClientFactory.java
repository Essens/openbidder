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

package com.google.openbidder.netty.client;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AbstractIdleService;

import com.beust.jcommander.internal.Nullable;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;

import java.net.URI;

/**
 * Baseline Netty client as a {@link com.google.common.util.concurrent.Service}.
 * @param <M> Message type
 */
public class NettyClientFactory<M> extends AbstractIdleService {

  private final EventLoopGroup clientGroup;
  private final Class<? extends Channel> clientChannel;
  private final ChannelInitializer<SocketChannel> channelInitializer;
  @Nullable private final URI defaultUri;
  private final Bootstrap bootstrap = new Bootstrap();
  private final int connectionTimeout;

  public NettyClientFactory(
      EventLoopGroup clientGroup,
      Class<? extends Channel> clientChannel,
      ChannelInitializer<SocketChannel> channelInitializer,
      int connectionTimeout) {

    this(clientGroup, clientChannel, channelInitializer, connectionTimeout, /* default URL */ null);
  }

  public NettyClientFactory(
      EventLoopGroup clientGroup,
      Class<? extends Channel> clientChannel,
      ChannelInitializer<SocketChannel> channelInitializer,
      int connectionTimeout,
      URI defaultUri) {

    this.clientGroup = Preconditions.checkNotNull(clientGroup);
    this.clientChannel = Preconditions.checkNotNull(clientChannel);
    this.channelInitializer = Preconditions.checkNotNull(channelInitializer);
    this.connectionTimeout = connectionTimeout;
    this.defaultUri = defaultUri;
  }

  protected NettyClient<M> makeClient(ChannelFuture connectFuture) throws Exception {
    return new NettyClient<>(connectFuture.sync().channel());
  }

  public NettyClient<M> connect() throws Exception {
    Preconditions.checkNotNull(defaultUri, "Default URL must be set for calling connect()");
    return connect(defaultUri);
  }

  public NettyClient<M> connect(URI uri) throws Exception {
    return makeClient(bootstrap.validate().connect(uri.getHost(), uri.getPort()));
  }

  @Override
  protected void startUp() throws Exception {
    bootstrap.group(clientGroup)
        .channel(clientChannel)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout)
        .handler(channelInitializer);
  }

  @Override
  protected void shutDown() throws Exception {
    clientGroup.shutdownGracefully();
  }
}
