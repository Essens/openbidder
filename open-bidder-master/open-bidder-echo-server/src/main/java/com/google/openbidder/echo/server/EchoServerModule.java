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

package com.google.openbidder.echo.server;

import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.openbidder.config.server.WebserverRuntime;
import com.google.openbidder.netty.server.NettyServer;

import javax.inject.Singleton;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * HTTP echo server configuration.
 */
public class EchoServerModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(Service.class)
        .annotatedWith(WebserverRuntime.class)
        .to(NettyServer.class)
        .in(Scopes.SINGLETON);
  }

  @Provides
  @Singleton
  public ChannelInitializer<SocketChannel> provideChannelInitializer() {
    return new EchoServerChannelInitializer();
  }
}
