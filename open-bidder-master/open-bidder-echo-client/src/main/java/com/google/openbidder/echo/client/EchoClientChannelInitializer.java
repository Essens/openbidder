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

package com.google.openbidder.echo.client;

import com.google.common.base.Preconditions;
import com.google.openbidder.config.client.Uri;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.net.URI;
import java.nio.charset.Charset;

import javax.inject.Inject;

/**
 * Set up a client to send chunked HTTP messages to a server.
 */
public class EchoClientChannelInitializer extends ChannelInitializer<SocketChannel> {
  private static final int MAX_CONTENT_LENGTH = 100000;

  private final URI uri;
  private final Charset charset;

  @Inject
  public EchoClientChannelInitializer(@Uri URI uri, Charset charset) {
    this.uri = Preconditions.checkNotNull(uri);
    this.charset = Preconditions.checkNotNull(charset);
  }

  @Override
  protected void initChannel(SocketChannel ch) {
    ChannelPipeline pipeline = ch.pipeline();
    pipeline.addLast("logging", new LoggingHandler(LogLevel.INFO));
    pipeline.addLast("http", new HttpClientCodec());
    pipeline.addLast("deflater", new HttpContentDecompressor());
    pipeline.addLast("aggregate", new HttpObjectAggregator(MAX_CONTENT_LENGTH));
    pipeline.addLast("discard", new DiscardReadHandler(charset));
    pipeline.addLast("handler", new EchoClientHandler(uri, charset));
  }
}
