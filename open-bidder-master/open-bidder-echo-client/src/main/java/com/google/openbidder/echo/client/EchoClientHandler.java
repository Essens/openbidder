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
import com.google.common.base.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.HttpHeaders.Values;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Wrap a String as an HTTP request.
 */
public class EchoClientHandler extends MessageToMessageEncoder<String> {

  private static final Logger logger = LoggerFactory.getLogger(EchoClientHandler.class);

  private final URI uri;
  private final Charset charset;

  public EchoClientHandler(URI uri, Charset charset) {
    this.uri = Preconditions.checkNotNull(uri);
    this.charset = Preconditions.checkNotNull(charset);
  }

  @Override
  protected void encode(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {
    FullHttpRequest httpRequest = new DefaultFullHttpRequest(
        HttpVersion.HTTP_1_1,
        HttpMethod.POST,
        getUri(uri),
        ctx.alloc().buffer().writeBytes(msg.getBytes(charset)));
    HttpHeaders headers = httpRequest.headers();
    headers.add(Names.HOST, uri.getHost());
    headers.add(Names.CONNECTION, Values.CLOSE);
    headers.set(Names.ACCEPT_ENCODING, Values.GZIP + ',' + Values.DEFLATE);
    headers.set(Names.ACCEPT_CHARSET, "utf-8");
    headers.set(Names.USER_AGENT, "Netty Echo client");
    headers.set(Names.ACCEPT, "text/plain");
    headers.set(Names.CONTENT_LENGTH, httpRequest.content().readableBytes());
    logger.info("Request: {}", httpRequest);
    out.add(httpRequest);
    ctx.flush();
  }

  private static String getUri(URI uri) {
    String path = uri.getPath();
    return Strings.isNullOrEmpty(path) ? "/" : path;
  }
}
