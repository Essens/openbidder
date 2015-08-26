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

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.openbidder.http.Cookie;
import com.google.openbidder.http.HttpResponse;
import com.google.openbidder.http.message.ContentHolder;

import java.net.URI;

import javax.annotation.Nullable;

/**
 * Standard {@link HttpResponse} implementation.
 */
public class StandardHttpResponse extends AbstractHttpResponse {
  private final int statusCode;
  private final ImmutableMultimap<String, String> headers;
  private final ImmutableMultimap<String, Cookie> cookies;
  private final ImmutableMultimap<String, String> redirectParameters;
  private final @Nullable URI redirectUri;

  protected StandardHttpResponse(
      int statusCode,
      Multimap<String, String> headers,
      Multimap<String, Cookie> cookies,
      @Nullable URI redirectUri,
      Multimap<String, String> redirectParameters,
      @Nullable ContentHolder contentHolder) {

    this.statusCode = statusCode;
    this.headers = ImmutableMultimap.copyOf(headers);
    this.cookies = ImmutableMultimap.copyOf(cookies);
    this.redirectParameters = ImmutableMultimap.copyOf(redirectParameters);
    this.redirectUri = redirectUri;
    setContentHolder(contentHolder);
    validate();
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
    return redirectUri;
  }

  @Override
  public final ImmutableMultimap<String, String> getRedirectParameters() {
    return redirectParameters;
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
  public HttpResponse.Builder toBuilder() {
    return new StandardHttpResponseBuilder(
        statusCode,
        headers,
        cookies,
        redirectUri);
  }

  public static HttpResponse.Builder newBuilder() {
    return new StandardHttpResponseBuilder();
  }
}
