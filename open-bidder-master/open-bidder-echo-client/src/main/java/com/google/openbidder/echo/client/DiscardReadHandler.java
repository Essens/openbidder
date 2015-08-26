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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;

/**
 * Send and receive text messages over a socket.
 */
public class DiscardReadHandler extends ChannelDuplexHandler {

  private static final Logger logger = LoggerFactory.getLogger(DiscardReadHandler.class);

  private final Charset charset;

  public DiscardReadHandler(Charset charset) {
    this.charset = Preconditions.checkNotNull(charset);
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (msg instanceof HttpResponse) {
      HttpResponse httpResponse = (HttpResponse) msg;
      if (logger.isDebugEnabled()) {
        logger.debug("Received response status {}: {}", httpResponse.getStatus(), httpResponse);
      }
    }
    if (msg instanceof HttpContent) {
      ByteBuf content = ((HttpContent) msg).content();
      String message = content.toString(charset);
      if (logger.isDebugEnabled()) {
        logger.debug("Response payload: {}", message);
      }
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    logger.warn("Exception caught", cause);
  }
}
