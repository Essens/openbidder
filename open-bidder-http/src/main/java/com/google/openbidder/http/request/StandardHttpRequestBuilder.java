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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.openbidder.http.Cookie;
import com.google.openbidder.http.HttpRequest;
import com.google.openbidder.http.Protocol;
import com.google.openbidder.http.util.HttpUtil;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;

import java.net.InetSocketAddress;
import java.net.URI;

/**
 * Standard HTTP request builder.
 */
public class StandardHttpRequestBuilder extends AbstractHttpRequestBuilder {
  private static final String DEFAULT_METHOD = "GET";

  private Protocol protocol;
  private String method;
  private URI uri;
  private InetSocketAddress remoteAddress;
  private final Multimap<String, String> headers = HashMultimap.create();
  private final Multimap<String, Cookie> cookies = HashMultimap.create();
  private final Multimap<String, String> parameters = HashMultimap.create();

  protected StandardHttpRequestBuilder() {
    this.protocol = Protocol.HTTP_1_1;
    this.method = DEFAULT_METHOD;
  }

  protected StandardHttpRequestBuilder(
      Protocol protocol,
      String method,
      URI uri,
      InetSocketAddress remoteAddress,
      Multimap<String, String> headers,
      Multimap<String, String> parameters,
      Multimap<String, Cookie> cookies) {

    this.protocol = protocol;
    this.method = method;
    this.uri = uri;
    this.remoteAddress = remoteAddress;
    this.headers.putAll(headers);
    this.cookies.putAll(cookies);
    this.parameters.putAll(parameters);
  }

  @Override
  public final Multimap<String, String> getHeaders() {
    return Multimaps.unmodifiableMultimap(headers);
  }

  @Override
  protected final Multimap<String, String> headers() {
    return headers;
  }

  @Override
  public final Multimap<String, Cookie> getCookies() {
    return Multimaps.unmodifiableMultimap(cookies);
  }

  @Override
  public final StandardHttpRequestBuilder addCookie(Cookie cookie) {
    cookies.put(cookie.getName(), cookie);
    return this;
  }

  @Override
  public final StandardHttpRequestBuilder removeCookie(String name) {
    cookies.removeAll(name);
    return this;
  }

  @Override
  public final StandardHttpRequestBuilder clearCookie() {
    cookies.clear();
    return this;
  }

  @Override
  public final Protocol getProtocol() {
    return protocol;
  }

  @Override
  public final StandardHttpRequestBuilder addParameter(String name, String value) {
    parameters.put(name, checkNotNull(value));
    return this;
  }

  @Override
  public final StandardHttpRequestBuilder removeParameter(String name, String value) {
    parameters.remove(name, value);
    return this;
  }

  @Override
  public final StandardHttpRequestBuilder removeParameter(String name) {
    parameters.removeAll(name);
    return this;
  }

  @Override
  public final StandardHttpRequestBuilder clearParameter() {
    parameters.clear();
    return this;
  }

  @Override
  public final StandardHttpRequestBuilder setProtocol(Protocol protocol) {
    this.protocol = checkNotNull(protocol);
    return this;
  }

  @Override
  public final StandardHttpRequestBuilder setMethod(String method) {
    this.method = checkNotNull(method);
    return this;
  }

  @Override
  public final StandardHttpRequestBuilder setUri(URI uri) {
    URIBuilder uriBuilder = new URIBuilder(uri);
    Multimap<String, String> parameters = HashMultimap.create();
    for (NameValuePair param : uriBuilder.getQueryParams()) {
      parameters.put(param.getName(), Strings.nullToEmpty(param.getValue()));
    }
    uriBuilder.removeQuery();
    this.uri = HttpUtil.buildUri(uriBuilder.toString());
    setAllParameter(parameters);
    return this;
  }

  @Override
  public InetSocketAddress getRemoteAddress() {
    return remoteAddress;
  }

  @Override
  public StandardHttpRequestBuilder setRemoteAddress(InetSocketAddress remoteAddress) {
    this.remoteAddress = remoteAddress;
    return this;
  }

  @Override
  public final Multimap<String, String> getParameters() {
    return Multimaps.unmodifiableMultimap(parameters);
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
  public HttpRequest build() {
    return new StandardHttpRequest(
        protocol,
        method,
        uri,
        remoteAddress,
        headers,
        parameters,
        cookies,
        closeContentHolder());
  }
}
