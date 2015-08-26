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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.openbidder.http.HttpRequest;
import com.google.openbidder.http.receiver.DefaultHttpReceiverContext;
import com.google.openbidder.http.route.HttpRoute;
import com.google.openbidder.http.route.HttpRouter;
import com.google.openbidder.netty.common.NettyHttpRequest;
import com.google.openbidder.netty.common.NettyHttpResponse;
import com.google.openbidder.netty.common.NettyHttpResponseBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.util.ReferenceCountUtil;

/**
 * A Netty server handler that passes incoming {@link HttpRequest}s through a series of
 * {@link HttpRoute}s. If the router fails to handle the request an HTTP 404 (Not Found)
 * response is sent back to the client.
 */
@Sharable
public class NettyHttpRouter extends ChannelInboundHandlerAdapter {
  private static final Logger logger = LoggerFactory.getLogger(NettyHttpRouter.class);
  /**
   * RFC 2616, 9.4 makes clear that HEAD "MUST NOT return a message-body";
   * this means Content-Length shouldn't be expected.
   * But at least curl seems to expect either Content-Length or Connection: close.
   * Also, Jetty sets Content-Length for HEAD responses, so it's safer doing this.
   */
  private static final boolean HEAD_CONTENT_LENGTH = true;
  static final String PENDING = "PENDING";

  private final HttpRouter httpRouter;

  public NettyHttpRouter(HttpRouter httpRouter) {
    this.httpRouter = checkNotNull(httpRouter);
  }

  @SuppressWarnings("unused")
  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (msg instanceof HttpRequest) {
      HttpRequest httpRequest = (HttpRequest) msg;
      NettyHttpResponseBuilder httpRespBuilder = new NettyHttpResponseBuilder(ctx.alloc());

      try {
        httpRouter.receive(new DefaultHttpReceiverContext(httpRequest, httpRespBuilder));
      } finally {
        if (httpRequest instanceof NettyHttpRequest) {
          ((NettyHttpRequest) httpRequest).netty().content().release();
        }

        int status = httpRespBuilder.getStatusCode();
        if (!httpRespBuilder.getHeaders().containsKey(Names.CONTENT_LENGTH)
            && (!(status >= 100 && status <= 199) && status != 204 && status != 304)
            && (HEAD_CONTENT_LENGTH || !HttpMethod.HEAD.name().equals(httpRequest.getMethod()))) {
          httpRespBuilder.setHeader(Names.CONTENT_LENGTH, PENDING);
        }

        NettyHttpResponse httpResp = httpRespBuilder.build();
        ctx.write(httpResp);
        ctx.flush();
        ReferenceCountUtil.release(msg);
      }
    } else if (logger.isDebugEnabled()) {
      logger.debug("Received unexpected message type {}", msg);
    }
  }
}
