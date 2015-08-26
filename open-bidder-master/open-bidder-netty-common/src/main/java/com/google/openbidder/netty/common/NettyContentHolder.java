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

import com.google.common.base.Charsets;
import com.google.common.net.MediaType;
import com.google.openbidder.http.message.ContentHolder;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Specialized {@link ContentHolder} for Netty.
 */
public class NettyContentHolder extends ContentHolder {

  public NettyContentHolder(FullHttpRequest self, State state) {
    super(self, self.content().readableBytes(), state);
  }

  public NettyContentHolder(FullHttpResponse self, State state) {
    super(self, -1, state);
  }

  @Override
  public Charset getCharset() {
    FullHttpMessage self = (FullHttpMessage) self();
    String contentType = self.headers().get(HttpHeaders.Names.CONTENT_TYPE);
    return contentType == null ? Charsets.UTF_8 : MediaType.parse(contentType).charset().orNull();
  }

  @Override
  protected InputStream toInputStream() {
    return new ByteBufInputStream(((FullHttpMessage) self()).content());
  }

  @Override
  protected OutputStream toOutputStream() {
    return new ByteBufOutputStream(((FullHttpMessage) self()).content());
  }

  @Override
  protected InputStream inputFromOutput(OutputStream os) {
    return toInputStream();
  }
}
