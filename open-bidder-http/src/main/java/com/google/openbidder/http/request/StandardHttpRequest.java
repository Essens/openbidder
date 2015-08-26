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

package com.google.openbidder.http.request;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.openbidder.http.Cookie;
import com.google.openbidder.http.HttpRequest;
import com.google.openbidder.http.Protocol;
import com.google.openbidder.http.message.ContentHolder;

import org.apache.http.client.utils.URIBuilder;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

import javax.annotation.Nullable;

/**
 * Standard {@link HttpRequest} implementation.
 */
public class StandardHttpRequest extends AbstractHttpRequest {
  private final Protocol protocol;
  private final String method;
  private final URI uri;
  private final InetSocketAddress remoteAddress;
  private final ImmutableMultimap<String, String> headers;
  private final ImmutableMultimap<String, String> parameters;
  private final ImmutableMultimap<String, Cookie> cookies;

  protected StandardHttpRequest(
      Protocol protocol,
      String method,
      URI uri,
      InetSocketAddress remoteAddress,
      Multimap<String, String> headers,
      Multimap<String, String> parameters,
      Multimap<String, Cookie> cookies,
      @Nullable ContentHolder contentHolder) {

    this.protocol = protocol;
    this.method = method.trim().toUpperCase();
    URIBuilder uriBuilder = new URIBuilder(uri);
    this.remoteAddress = remoteAddress;
    this.parameters = ImmutableMultimap.copyOf(parameters);
    uriBuilder.removeQuery();
    try {
      this.uri = uriBuilder.build();
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("Invalid URI: " + uri);
    }
    this.headers = ImmutableMultimap.copyOf(headers);
    this.cookies = ImmutableMultimap.copyOf(cookies);
    setContentHolder(contentHolder);
  }

  @Override
  public final Protocol getProtocol() {
    return protocol;
  }

  @Override
  public final ImmutableMultimap<String, String> getParameters() {
    return parameters;
  }

  @Override
  public final String getMethod() {
    return method;
  }

  @Override
  public final URI getUri() {
    return uri;
  }

  @Override
  public InetSocketAddress getRemoteAddress() {
    return remoteAddress;
  }

  @Override
  public final ImmutableMultimap<String, String> getHeaders() {
    return headers;
  }

  @Override
  public final ImmutableMultimap<String, Cookie> getCookies() {
    return cookies;
  }

  @Override
  public HttpRequest.Builder toBuilder() {
    return new StandardHttpRequestBuilder(
        protocol,
        method,
        uri,
        remoteAddress,
        headers,
        parameters,
        cookies);
  }

  public static HttpRequest.Builder newBuilder() {
    return new StandardHttpRequestBuilder();
  }
}
