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

import com.google.openbidder.http.HttpRequest;
import com.google.openbidder.netty.common.NettyHttpRequest;
import com.google.openbidder.netty.common.NettyHttpResponse;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.ReferenceCountUtil;

/**
 * A Netty client codec for encoding outbound {@link FullHttpRequest}s
 */
public class NettyHttpClientCodec extends ChannelDuplexHandler {
  public static final NettyHttpClientCodec INSTANCE = new NettyHttpClientCodec();

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    if (msg instanceof FullHttpResponse) {
      try {
        FullHttpResponse nettyResponse = (FullHttpResponse) msg;
        ctx.fireChannelRead(new NettyHttpResponse(nettyResponse));
      } finally {
        ReferenceCountUtil.release(msg, 2);
      }
    }
  }

  @Override
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
    FullHttpRequest nettyRequest;
    if (msg instanceof NettyHttpRequest) {
      nettyRequest = ((NettyHttpRequest) msg).netty();
    } else if (msg instanceof HttpRequest) {
      nettyRequest = NettyHttpRequest.getNettyRequest((HttpRequest) msg, ctx.alloc());
    } else if (msg instanceof FullHttpRequest) {
      nettyRequest = (FullHttpRequest) msg;
    } else {
      nettyRequest = null;
    }

    if (nettyRequest != null) {
      try {
        ctx.write(nettyRequest);
        ctx.flush();
      } finally {
        ReferenceCountUtil.release(msg);
      }
    }
  }
}
