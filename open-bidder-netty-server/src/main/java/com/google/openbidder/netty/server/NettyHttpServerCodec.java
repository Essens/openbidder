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

import com.google.openbidder.http.HttpRequest;
import com.google.openbidder.http.HttpResponse;
import com.google.openbidder.netty.common.NettyHttpRequest;
import com.google.openbidder.netty.common.NettyHttpResponse;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.HttpHeaders.Values;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.ReferenceCountUtil;

import java.net.InetSocketAddress;

/**
 * A Netty codec for decoding incoming {@link FullHttpRequest}s into {@link HttpRequest}s and
 * outgoing {@link HttpResponse}s into {@link FullHttpResponse}.
 */
public class NettyHttpServerCodec extends ChannelDuplexHandler {
  private boolean keepAlive = true;

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (msg instanceof FullHttpRequest) {
      try {
        FullHttpRequest nettyRequest = (FullHttpRequest) msg;
        if (HttpHeaders.is100ContinueExpected(nettyRequest)) {
          ctx.write(new DefaultFullHttpResponse(
              nettyRequest.getProtocolVersion(), HttpResponseStatus.CONTINUE));
        }
        keepAlive = HttpHeaders.isKeepAlive(nettyRequest);
        ctx.fireChannelRead(new NettyHttpRequest(
            nettyRequest,
            // The SocketAddress below will always be a InetSocketAddress
            (InetSocketAddress)ctx.channel().remoteAddress()));
      } finally {
        ReferenceCountUtil.release(msg);
      }
    }
  }

  @Override
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
    FullHttpResponse nettyResponse;
    if (msg instanceof NettyHttpResponse) {
      nettyResponse = ((NettyHttpResponse) msg).netty();
    } else if (msg instanceof HttpResponse) {
      nettyResponse = NettyHttpResponse.getNettyResponse((HttpResponse) msg, ctx.alloc());
    } else if (msg instanceof FullHttpResponse) {
      nettyResponse = (FullHttpResponse) msg;
    } else {
      nettyResponse = null;
    }

    if (nettyResponse != null) {
      try {
        if (nettyResponse.headers().contains(
            Names.CONTENT_LENGTH, NettyHttpRouter.PENDING, false)) {
          nettyResponse.headers().set(
              Names.CONTENT_LENGTH, nettyResponse.content().readableBytes());
        }

        if (keepAlive) {
          nettyResponse.headers().set(Names.CONNECTION, Values.KEEP_ALIVE);
          ctx.write(nettyResponse, promise);
        } else {
          ctx.write(nettyResponse).addListener(ChannelFutureListener.CLOSE);
        }
        ctx.flush(); // This will release the response's ByteBuf!
      } finally {
        ReferenceCountUtil.release(msg);
      }
    }
  }
}
