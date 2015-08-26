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
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.openbidder.http.Cookie;
import com.google.openbidder.http.HttpStatusType;
import com.google.openbidder.http.message.ContentHolder;
import com.google.openbidder.http.response.AbstractHttpResponseBuilder;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;

import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.ServerCookieEncoder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

/**
 * Netty-optimized {@link com.google.openbidder.http.HttpResponse} implementation.
 */
public final class NettyHttpResponseBuilder extends AbstractHttpResponseBuilder {
  private int statusCode;
  private final Multimap<String, String> headers = HashMultimap.create();
  private final Multimap<String, Cookie> cookies = HashMultimap.create();
  private final Multimap<String, String> redirectParameters = HashMultimap.create();
  private URIBuilder redirectUri;
  private final FullHttpResponse netty;

  public NettyHttpResponseBuilder(FullHttpResponse netty) {
    this.netty = netty;
    this.netty.content().retain();
  }

  public NettyHttpResponseBuilder(ByteBufAllocator allocator) {
    this.netty = new DefaultFullHttpResponse(
        HttpVersion.HTTP_1_1,
        HttpResponseStatus.OK,
        allocator.buffer());
  }

  public FullHttpResponse netty() {
    return netty;
  }

  @Override
  protected ContentHolder newContentHolder() {
    return new NettyContentHolder(netty, ContentHolder.State.OUTPUT);
  }

  @Override
  public NettyHttpResponseBuilder setStatusCode(int statusCode) {
    this.statusCode = statusCode;
    return this;
  }

  @Override
  public NettyHttpResponseBuilder setRedirectUri(URI uri) {
    redirectUri = new URIBuilder(uri);
    redirectParameters.clear();
    for (NameValuePair param : redirectUri.getQueryParams()) {
      redirectParameters.put(param.getName(), Strings.nullToEmpty(param.getValue()));
    }
    redirectUri.removeQuery();
    return this;
  }

  @Override
  public NettyHttpResponseBuilder clearRedirectUri() {
    redirectUri = null;
    return this;
  }

  @Override
  public NettyHttpResponseBuilder setRedirectHost(String hostname) {
    uriBuilder().setHost(hostname);
    return this;
  }

  @Override
  public NettyHttpResponseBuilder setRedirectPort(int port) {
    uriBuilder().setPort(port);
    return this;
  }

  @Override
  public NettyHttpResponseBuilder setRedirectPath(String path) {
    uriBuilder().setPath(path);
    return this;
  }

  @Override
  public NettyHttpResponseBuilder setRedirectFragment(String fragment) {
    uriBuilder().setFragment(fragment);
    return this;
  }

  @Override
  protected Multimap<String, String> redirectParameters() {
    return redirectParameters;
  }

  @Override
  public NettyHttpResponseBuilder addCookie(Cookie cookie) {
    cookies.put(cookie.getName(), cookie);
    return this;
  }

  @Override
  public NettyHttpResponseBuilder removeCookie(String name) {
    cookies.removeAll(name);
    return this;
  }

  @Override
  public NettyHttpResponseBuilder clearCookie() {
    cookies.clear();
    return this;
  }

  @Override
  public int getStatusCode() {
    return statusCode;
  }

  @Override
  public boolean hasRedirectUri() {
    return redirectUri != null;
  }

  @Nullable
  @Override
  public URI getRedirectUri() {
    if (redirectUri == null) {
      return null;
    }
    try {
      for (Entry<String, String> param : redirectParameters.entries()) {
        redirectUri.addParameter(param.getKey(), param.getValue());
      }
      String uri = redirectUri.toString();
      if (Strings.isNullOrEmpty(uri)) {
        return null;
      }
      return new URI(uri);
    } catch (URISyntaxException e) {
      return null;
    } finally {
      redirectUri.removeQuery();
    }
  }

  @Nullable
  @Override
  public String getRedirectHost() {
    return redirectUri == null ? null : redirectUri.getHost();
  }

  @Nullable
  @Override
  public Integer getRedirectPort() {
    return redirectUri == null ? null : redirectUri.getPort();
  }

  @Nullable
  @Override
  public String getRedirectPath() {
    return redirectUri == null ? null : redirectUri.getPath();
  }

  @Nullable
  @Override
  public String getRedirectFragment() {
    return redirectUri == null ? null : redirectUri.getFragment();
  }

  @Override
  public Multimap<String, String> getRedirectParameters() {
    return Multimaps.unmodifiableMultimap(redirectParameters);
  }

  @Override
  public Multimap<String, String> getHeaders() {
    return Multimaps.unmodifiableMultimap(headers);
  }

  @Override
  protected Multimap<String, String> headers() {
    return headers;
  }

  @Override
  public Multimap<String, Cookie> getCookies() {
    return Multimaps.unmodifiableMultimap(cookies);
  }

  @Override
  public NettyHttpResponse build() {
    ContentHolder contentHolder = closeContentHolder();

    // status
    netty.setStatus(HttpResponseStatus.valueOf(statusCode));

    // redirect
    URI redirectUri = getRedirectUri();
    if (HttpStatusType.REDIRECT.contains(getStatusCode()) && redirectUri != null) {
      netty.headers().add(Names.LOCATION, redirectUri);
    } else {
      // headers
      for (Map.Entry<String, String> header : getHeaders().entries()) {
        netty.headers().add(header.getKey(), header.getValue());
      }
      if (!netty.headers().contains(Names.CONTENT_LENGTH)) {
        netty.headers().add(Names.CONTENT_LENGTH, netty.content().readableBytes());
      }

      // cookies
      for (Cookie cookie : getCookies().values()) {
        netty.headers().add(
            Names.COOKIE, ServerCookieEncoder.encode(NettyCookie.getNettyCookie(cookie)));
      }
    }

    // Don't release the ByteBuf; hand it off to a constructor that doesn't retain it
    return new NettyHttpResponse(netty, headers, cookies, redirectUri, contentHolder);
  }

  private URIBuilder uriBuilder() {
    if (redirectUri == null) {
      redirectUri = new URIBuilder();
    }
    return redirectUri;
  }
}
