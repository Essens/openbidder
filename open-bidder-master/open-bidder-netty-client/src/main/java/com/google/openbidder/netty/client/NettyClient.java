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

import io.netty.channel.Channel;

/**
 * Generic Netty client. Intended to be extended.
 * @param <M> Message type
 */
public class NettyClient<M> {

  private final Channel channel;

  public NettyClient(Channel channel) {
    this.channel = Preconditions.checkNotNull(channel);
  }

  public void send(M message) {
    channel.write(message);
    channel.flush();
  }

  public boolean isConnected() {
    return channel.isRegistered();
  }

  public void close() {
    if (channel.isOpen()) {
      channel.close();
    }
  }
}
