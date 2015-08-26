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

package com.google.openbidder.http.response;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.openbidder.http.Cookie;
import com.google.openbidder.http.HttpResponse;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map.Entry;

import javax.annotation.Nullable;

/**
 * Standard HTTP response builder.
 */
public class StandardHttpResponseBuilder extends AbstractHttpResponseBuilder {
  private int statusCode;
  private final Multimap<String, String> headers = HashMultimap.create();
  private final Multimap<String, Cookie> cookies = HashMultimap.create();
  private final Multimap<String, String> redirectParameters = HashMultimap.create();
  private URIBuilder redirectUri;

  protected StandardHttpResponseBuilder() {
    this.statusCode = HttpStatus.SC_OK;
  }

  protected StandardHttpResponseBuilder(
      int statusCode,
      Multimap<String, String> headers,
      Multimap<String, Cookie> cookies,
      @Nullable URI redirectUri) {

    this.statusCode = statusCode;
    this.headers.putAll(headers);
    this.cookies.putAll(cookies);
    if (redirectUri != null) {
      setRedirectUri(redirectUri);
    }
  }

  @Override
  protected final Multimap<String, String> headers() {
    return headers;
  }

  @Override
  public final Multimap<String, String> getHeaders() {
    return Multimaps.unmodifiableMultimap(headers);
  }

  @Override
  public final Multimap<String, Cookie> getCookies() {
    return Multimaps.unmodifiableMultimap(cookies);
  }

  @Override
  public final StandardHttpResponseBuilder addCookie(Cookie cookie) {
    cookies.put(cookie.getName(), cookie);
    return this;
  }

  @Override
  public final StandardHttpResponseBuilder removeCookie(String name) {
    cookies.removeAll(name);
    return this;
  }

  @Override
  public final StandardHttpResponseBuilder clearCookie() {
    cookies.clear();
    return this;
  }

  @Override
  public final StandardHttpResponseBuilder setStatusCode(int statusCode) {
    this.statusCode = statusCode;
    return this;
  }

  @Override
  public final StandardHttpResponseBuilder setRedirectUri(URI uri) {
    redirectUri = new URIBuilder(uri);
    redirectParameters.clear();
    for (NameValuePair param : redirectUri.getQueryParams()) {
      redirectParameters.put(param.getName(), Strings.nullToEmpty(param.getValue()));
    }
    redirectUri.removeQuery();
    return this;
  }

  @Override
  public final StandardHttpResponseBuilder clearRedirectUri() {
    redirectUri = null;
    return this;
  }

  @Override
  public final StandardHttpResponseBuilder setRedirectHost(String hostname) {
    uriBuilder().setHost(hostname);
    return this;
  }

  @Override
  public final StandardHttpResponseBuilder setRedirectPort(int port) {
    uriBuilder().setPort(port);
    return this;
  }

  @Override
  public final StandardHttpResponseBuilder setRedirectPath(String path) {
    uriBuilder().setPath(path);
    return this;
  }

  @Override
  public final StandardHttpResponseBuilder setRedirectFragment(String fragment) {
    uriBuilder().setFragment(fragment);
    return this;
  }

  @Override
  public final int getStatusCode() {
    return statusCode;
  }

  @Override
  public final boolean hasRedirectUri() {
    return redirectUri != null;
  }

  @Nullable
  @Override
  public final URI getRedirectUri() {
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
  public final String getRedirectHost() {
    return redirectUri == null ? null : redirectUri.getHost();
  }

  @Nullable
  @Override
  public final Integer getRedirectPort() {
    return redirectUri == null ? null : redirectUri.getPort();
  }

  @Nullable
  @Override
  public final String getRedirectPath() {
    return redirectUri == null ? null : redirectUri.getPath();
  }

  @Nullable
  @Override
  public final String getRedirectFragment() {
    return redirectUri == null ? null : redirectUri.getFragment();
  }

  @Override
  public final Multimap<String, String> getRedirectParameters() {
    return Multimaps.unmodifiableMultimap(redirectParameters);
  }

  @Override
  protected final Multimap<String, String> redirectParameters() {
    return redirectParameters;
  }

  @Override
  public HttpResponse build() {
    return new StandardHttpResponse(
        statusCode,
        headers,
        cookies,
        getRedirectUri(),
        redirectParameters,
        closeContentHolder());
  }

  private URIBuilder uriBuilder() {
    if (redirectUri == null) {
      redirectUri = new URIBuilder();
    }
    return redirectUri;
  }
}
