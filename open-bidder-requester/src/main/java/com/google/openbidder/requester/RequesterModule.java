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

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;

import com.beust.jcommander.Parameters;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * Requester client bindings.
 */
@Parameters(separators = "=")
public class RequesterModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(new TypeLiteral<ChannelInitializer<SocketChannel>>() {})
        .to(RequesterChannelInitializer.class)
        .in(Scopes.SINGLETON);
    bind(RequesterClientFactory.class).in(Scopes.SINGLETON);
  }
}
