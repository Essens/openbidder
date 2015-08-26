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

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.openbidder.config.system.AvailableProcessors;
import com.google.openbidder.netty.client.config.ClientConnectionChannel;
import com.google.openbidder.netty.client.config.ClientGroup;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import javax.inject.Singleton;

/**
 * Netty client configuration.
 */
@Parameters(separators = "=")
public class NettyClientModule extends AbstractModule {
  private static final Logger logger = LoggerFactory.getLogger(NettyClientModule.class);

  @Parameter(names = "--client_group_size",
      description = "Number of client group threads per processor")
  private int clientGroupSize = ClientGroup.DEFAULT;

  @Override
  protected void configure() {
    bind(new TypeLiteral<Class<? extends Channel>>() {})
        .annotatedWith(ClientConnectionChannel.class)
        .toInstance(NioSocketChannel.class);
  }

  @Provides
  @Singleton
  @ClientGroup
  public int provideWorkerGroupSize(@AvailableProcessors int availableProcessors) {
    return clientGroupSize * availableProcessors;
  }

  @Provides
  @Singleton
  @ClientGroup
  public EventLoopGroup provideClientGroup(@ClientGroup int clientGroupSize) {
    logger.info("Client group size: {}", clientGroupSize);
    return new NioEventLoopGroup(clientGroupSize);
  }
}
