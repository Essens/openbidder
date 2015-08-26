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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMultimap;
import com.google.openbidder.http.Cookie;
import com.google.openbidder.http.HttpRequest;
import com.google.openbidder.http.Protocol;
import com.google.openbidder.http.message.ContentHolder;
import com.google.openbidder.http.request.AbstractHttpRequest;
import com.google.openbidder.http.util.HttpUtil;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;

import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.http.CookieDecoder;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Netty-optimized {@link com.google.openbidder.http.HttpRequest} implementation.
 */
public final class NettyHttpRequest extends AbstractHttpRequest {
  private final FullHttpRequest netty;
  private Protocol protocol;
  private URI uri;
  private ImmutableMultimap<String, String> headers;
  private ImmutableMultimap<String, String> parameters;
  private ImmutableMultimap<String, Cookie> cookies;

  public NettyHttpRequest(FullHttpRequest netty) {
    this.netty = netty;
    netty.content().retain();
  }

  public NettyHttpRequest(HttpRequest httpRequest, ByteBufAllocator allocator) {
    this.netty = getNettyRequest(httpRequest, allocator);
  }

  public FullHttpRequest netty() {
    return netty;
  }

  public static FullHttpRequest getNettyRequest(
      HttpRequest httpRequest, ByteBufAllocator allocator) {
    try {
      InputStream content = httpRequest.content();
      // URI
      URIBuilder uriBuilder = new URIBuilder(httpRequest.getUri());
      for (Entry<String, String> param : httpRequest.getParameters().entries()) {
        uriBuilder.addParameter(param.getKey(), param.getValue());
      }
      String uri;
      try {
        uri = uriBuilder.build().toString();
      } catch (URISyntaxException e) {
        throw new IllegalArgumentException("Invalid URI for " + uriBuilder, e);
      }

      // method
      DefaultFullHttpRequest self = new DefaultFullHttpRequest(
          HttpVersion.valueOf(httpRequest.getProtocol().toString()),
          HttpMethod.valueOf(httpRequest.getMethod()),
          uri,
          allocator.buffer(content.available()));

      NettyUtil.getNettyMessage(httpRequest, self.headers(), self.content());
      return self;
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public ImmutableMultimap<String, String> getParameters() {
    getUri();
    return parameters;
  }

  @Override
  public ImmutableMultimap<String, String> getHeaders() {
    if (headers == null) {
      if (netty.headers().isEmpty()) {
        headers = ImmutableMultimap.of();
      } else {
        ImmutableMultimap.Builder<String, String> builder = ImmutableMultimap.builder();
        for (Entry<String, String> header : netty.headers()) {
          if (!Names.COOKIE.equals(header.getKey())) {
            builder.put(header.getKey(), header.getValue());
          }
        }
        headers = builder.build();
      }
    }
    return headers;
  }

  @Override
  public ImmutableMultimap<String, Cookie> getCookies() {
    if (cookies == null) {
      String cookieHeader = netty.headers().get(Names.COOKIE);
      if (Strings.isNullOrEmpty(cookieHeader)) {
        cookies = ImmutableMultimap.of();
      } else {
        ImmutableMultimap.Builder<String, Cookie> builder = ImmutableMultimap.builder();
        Set<io.netty.handler.codec.http.Cookie> nettyCookies = CookieDecoder.decode(cookieHeader);
        for (io.netty.handler.codec.http.Cookie nettyCookie : nettyCookies) {
          NettyCookie cookie = new NettyCookie(nettyCookie);
          builder.put(cookie.getName(), cookie);
        }
        cookies = builder.build();
      }
    }
    return cookies;
  }

  @Override
  public Protocol getProtocol() {
    if (protocol == null) {
      this.protocol = Protocol.decode(netty.getProtocolVersion().text());
    }
    return protocol;
  }

  @Override
  public String getMethod() {
    return netty.getMethod().name();
  }

  @Override
  public URI getUri() {
    if (uri == null) {
      try {
        URIBuilder uriBuilder = new URIBuilder(netty.getUri());
        if (uriBuilder.getQueryParams().isEmpty()) {
          parameters = ImmutableMultimap.of();
        } else {
          ImmutableMultimap.Builder<String, String> paramBuilder = ImmutableMultimap.builder();
          for (NameValuePair param : uriBuilder.getQueryParams()) {
            paramBuilder.put(param.getName(), Strings.nullToEmpty(param.getValue()));
          }
          uriBuilder.removeQuery();
          parameters = paramBuilder.build();
        }
        uri = HttpUtil.buildUri(uriBuilder.toString());
      } catch (URISyntaxException e) {
        throw new IllegalStateException("Netty request has invalid URI: " + netty.getUri(), e);
      }
    }
    return uri;
  }

  @Override
  protected ContentHolder newContentHolder() {
    return new NettyContentHolder(netty, ContentHolder.State.INPUT);
  }
}
