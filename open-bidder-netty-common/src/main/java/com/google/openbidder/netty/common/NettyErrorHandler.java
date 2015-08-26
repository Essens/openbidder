/*
 * Copyright 2014 Google Inc. All Rights Reserved.
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

package com.google.openbidder.netty.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.IOException;

/**
 * Handles errors.
 */
@Sharable
public class NettyErrorHandler extends ChannelInboundHandlerAdapter {
  private static final Logger logger = LoggerFactory.getLogger(NettyErrorHandler.class);
  public static final NettyErrorHandler INSTANCE = new NettyErrorHandler();

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    if (cause instanceof IOException) {
      // Common: Too many open files; Connection reset by peer
      // Single-line dump even if the logger is set to DEBUG.
      logger.warn("Unhandled exception: {}", cause.toString());
    } else {
      if (logger.isDebugEnabled()) {
        logger.debug("Unhandled exception", cause);
      } else {
        logger.warn("Unhandled exception: {}", cause.toString());
      }
    }
  }
}
