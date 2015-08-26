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
import com.google.common.collect.Multimap;
import com.google.openbidder.http.Cookie;
import com.google.openbidder.http.HttpResponse;
import com.google.openbidder.http.message.ContentHolder;
import com.google.openbidder.http.response.AbstractHttpResponse;

import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * Netty-optimized {@link com.google.openbidder.http.HttpResponse} implementation.
 */
public final class NettyHttpResponse extends AbstractHttpResponse {
  private final FullHttpResponse netty;
  private ImmutableMultimap<String, String> headers;
  private ImmutableMultimap<String, Cookie> cookies;
  private @Nullable URI redirectUri;

  public NettyHttpResponse(FullHttpResponse netty) {
    this.netty = netty;
    validate();
    this.netty.content().retain();
  }

  public NettyHttpResponse(HttpResponse httpResponse, ByteBufAllocator allocator) {
    this.netty = getNettyResponse(httpResponse, allocator);
    validate();
  }

  NettyHttpResponse(
      FullHttpResponse netty,
      Multimap<String, String> headers,
      Multimap<String, Cookie> cookies,
      URI redirectUri,
      @Nullable ContentHolder contentHolder) {
    // Don't retain the ByteBuf, it's handed off from a builder that didn't release it
    this.netty = netty;
    this.headers = ImmutableMultimap.copyOf(headers);
    this.cookies = ImmutableMultimap.copyOf(cookies);
    this.redirectUri = redirectUri;
    setContentHolder(contentHolder);
    validate();
  }

  public FullHttpResponse netty() {
    return netty;
  }

  public static FullHttpResponse getNettyResponse(
      HttpResponse httpResponse, ByteBufAllocator allocator) {
    try {
      // status
      DefaultFullHttpResponse self = new DefaultFullHttpResponse(
          HttpVersion.HTTP_1_1,
          HttpResponseStatus.valueOf(httpResponse.getStatusCode()),
          allocator.buffer());

      // redirect
      if (httpResponse.isValidRedirect()) {
        self.headers().add(HttpHeaders.Names.LOCATION, httpResponse.getRedirectUri());
      } else {
        NettyUtil.getNettyMessage(httpResponse, self.headers(), self.content());
      }

      return self;
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public ImmutableMultimap<String, String> getRedirectParameters() {
    return ImmutableMultimap.of();
  }

  @Override
  public ImmutableMultimap<String, String> getHeaders() {
    if (headers == null) {
      if (netty.headers().isEmpty()) {
        headers = ImmutableMultimap.of();
      } else {
        ImmutableMultimap.Builder<String, String> builder = ImmutableMultimap.builder();
        for (Entry<String, String> header : netty.headers()) {
          if (!HttpHeaders.Names.COOKIE.equals(header.getKey())) {
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
      List<String> cookieHeaders = netty.headers().getAll(HttpHeaders.Names.COOKIE);
      if (cookieHeaders.isEmpty()) {
        cookies = ImmutableMultimap.of();
      } else {
        ImmutableMultimap.Builder<String, Cookie> builder = ImmutableMultimap.builder();
        for (String cookie : cookieHeaders) {
          Set<io.netty.handler.codec.http.cookie.Cookie> nettyCookies =
              ServerCookieDecoder.LAX.decode(cookie);
          for (io.netty.handler.codec.http.cookie.Cookie nettyCookie : nettyCookies) {
            builder.put(nettyCookie.name(), new NettyCookie(nettyCookie));
          }
        }
        cookies = builder.build();
      }
    }
    return cookies;
  }

  @Override
  public int getStatusCode() {
    return netty.getStatus().code();
  }

  @Override
  public boolean hasRedirectUri() {
    return redirectUri != null
        || !Strings.isNullOrEmpty(netty.headers().get(HttpHeaders.Names.LOCATION));
  }

  @Override
  public @Nullable URI getRedirectUri() {
    if (redirectUri == null) {
      String locationHeader = netty.headers().get(HttpHeaders.Names.LOCATION);
      if (Strings.isNullOrEmpty(locationHeader)) {
        return null;
      } else {
        try {
          redirectUri = new URI(locationHeader);
        } catch (URISyntaxException e) {
          throw new IllegalStateException("Netty response has invalid URI: " + locationHeader, e);
        }
      }
    }
    return redirectUri;
  }

  @Override
  protected ContentHolder newContentHolder() {
    return new NettyContentHolder(netty, ContentHolder.State.INPUT);
  }
}
