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

package com.google.openbidder.netty.common;

import com.google.openbidder.http.Cookie;
import com.google.openbidder.http.HttpMessage;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.ServerCookieEncoder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public final class NettyUtil {

  private NettyUtil() {
  }

  public static void getNettyMessage(
      HttpMessage httpMessage, HttpHeaders msgHeaders, ByteBuf msgContent) throws IOException {
    // headers
    for (Map.Entry<String, String> header : httpMessage.getHeaders().entries()) {
      msgHeaders.add(header.getKey(), header.getValue());
    }

    // cookies
    for (Cookie cookie : httpMessage.getCookies().values()) {
      msgHeaders.add(
          Names.COOKIE, ServerCookieEncoder.encode(NettyCookie.getNettyCookie(cookie)));
    }

    // content
    InputStream is = httpMessage.content();
    while (is.available() != 0) {
      msgContent.writeBytes(is, is.available());
    }
    if (!msgHeaders.contains(Names.CONTENT_LENGTH)) {
      msgHeaders.add(Names.CONTENT_LENGTH, msgContent.readableBytes());
    }
  }
}
