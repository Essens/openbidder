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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.openbidder.config.server.BidderAdminPort;
import com.google.openbidder.config.server.BidderListenPort;
import com.google.openbidder.config.server.BindHost;
import com.google.openbidder.netty.server.config.BossGroup;
import com.google.openbidder.netty.server.config.ServerConnectionChannel;
import com.google.openbidder.netty.server.config.WorkerGroup;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.socket.SocketChannel;

import java.net.InetSocketAddress;
import java.util.Map;

import javax.inject.Inject;

/**
 * Baseline Netty server as a {@link com.google.common.util.concurrent.Service}.
 */
public class NettyServer extends AbstractIdleService {

  private final EventLoopGroup bossGroup;
  private final EventLoopGroup workerGroup;
  private final Class<? extends ServerChannel> serverChannel;
  private final ChannelInitializer<SocketChannel> channelInitializer;
  private final ImmutableMap<ChannelOption<?>, Object> channelOptions;
  private final String host;
  private final int listenPort;
  private Channel channel;
  private final int adminPort;
  private Channel adminChannel;

  @Inject
  public NettyServer(
      @BossGroup EventLoopGroup bossGroup,
      @WorkerGroup EventLoopGroup workerGroup,
      @ServerConnectionChannel Class<? extends ServerChannel> serverChannel,
      ChannelInitializer<SocketChannel> channelInitializer,
      Map<ChannelOption<?>, Object> channelOptions,
      @BindHost String host,
      @BidderListenPort int listenPort,
      @BidderAdminPort int adminPort) {

    this.bossGroup = checkNotNull(bossGroup);
    this.workerGroup = checkNotNull(workerGroup);
    this.serverChannel = checkNotNull(serverChannel);
    this.channelInitializer = checkNotNull(channelInitializer);
    this.channelOptions = ImmutableMap.copyOf(channelOptions);
    this.host = checkNotNull(host);
    this.listenPort = listenPort;
    this.adminPort = adminPort;
  }

  @Override
  protected void startUp() throws Exception {
    ServerBootstrap bootstrap = new ServerBootstrap()
        .group(bossGroup, workerGroup)
        .channel(serverChannel)
        .childHandler(channelInitializer)
        .childOption(ChannelOption.TCP_NODELAY, true);

    for (Map.Entry<ChannelOption<?>, Object> channelOption : channelOptions.entrySet()) {
      @SuppressWarnings("unchecked")
      ChannelOption<Object> key = (ChannelOption<Object>) channelOption.getKey();
      bootstrap.childOption(key, channelOption.getValue());
    }

    channel = bootstrap.bind(Strings.isNullOrEmpty(host)
        ? new InetSocketAddress(listenPort)
        : new InetSocketAddress(host, listenPort)).sync().channel();
    adminChannel = bootstrap.bind(Strings.isNullOrEmpty(host)
        ? new InetSocketAddress(adminPort)
        : new InetSocketAddress(host, adminPort)).sync().channel();
  }

  @Override
  protected synchronized void shutDown() throws Exception {
    bossGroup.shutdownGracefully();
    workerGroup.shutdownGracefully();

    if (channel != null) {
      channel.closeFuture().sync();
      channel = null;
    }

    if (adminChannel != null) {
      adminChannel.closeFuture().sync();
      adminChannel = null;
    }
  }
}
