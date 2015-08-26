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

import com.google.common.base.Preconditions;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.HttpHeaders.Values;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.nio.charset.Charset;

/**
 * Echo HTTP payloads back to the sender.
 */
public class EchoServerHandler extends SimpleChannelInboundHandler<HttpObject> {
  private final Charset charset;

  public EchoServerHandler(Charset charset) {
    this.charset = Preconditions.checkNotNull(charset);
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
    if (msg instanceof FullHttpRequest) {
      FullHttpRequest httpRequest = (FullHttpRequest) msg;
      if (HttpHeaders.is100ContinueExpected(httpRequest)) {
        ctx.write(new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE));
      }
      boolean keepAlive = HttpHeaders.isKeepAlive(httpRequest);
      FullHttpResponse httpResponse = new DefaultFullHttpResponse(
          HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
      httpResponse.content().writeBytes("Received: ".getBytes(charset))
          .writeBytes(httpRequest.content());
      httpResponse.headers().add(Names.CONTENT_TYPE, "text/plain; charset=" + charset.displayName());
      httpResponse.headers().add(Names.CONTENT_LENGTH, httpResponse.content().readableBytes());
      if (keepAlive) {
        httpResponse.headers().add(Names.CONNECTION, Values.KEEP_ALIVE);
        ctx.write(httpResponse);
      } else {
        ctx.write(httpResponse).addListener(ChannelFutureListener.CLOSE);
      }
      ctx.flush();
    }
  }
}
