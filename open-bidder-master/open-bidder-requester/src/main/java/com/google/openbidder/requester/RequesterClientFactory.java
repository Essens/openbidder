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

import com.google.openbidder.config.client.ConnectionTimeout;
import com.google.openbidder.config.client.Uri;
import com.google.openbidder.netty.client.NettyClientFactory;
import com.google.openbidder.netty.client.config.ClientConnectionChannel;
import com.google.openbidder.netty.client.config.ClientGroup;
import com.google.protos.adx.NetworkBid.BidRequest;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;

import java.net.URI;

import javax.inject.Inject;

/**
 * Client to send DoubleClick bid request protobufs over HTTP.
 */
public class RequesterClientFactory extends NettyClientFactory<BidRequest> {

  @Inject
  public RequesterClientFactory(
      @ClientGroup EventLoopGroup clientGroup,
      @ClientConnectionChannel Class<? extends Channel> clientChannel,
      ChannelInitializer<SocketChannel> channelInitializer,
      @ConnectionTimeout int connectionTimeout,
      @Uri URI uri) {

    super(clientGroup, clientChannel, channelInitializer, connectionTimeout, uri);
  }
}
