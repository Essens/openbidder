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

package com.google.openbidder.api.testing.match;

import com.google.openbidder.api.match.MatchRequest;
import com.google.openbidder.http.HttpRequest;
import com.google.openbidder.http.cookie.CookieOrBuilder;
import com.google.openbidder.http.request.StandardHttpRequest;
import com.google.openbidder.http.util.HttpUtil;

/**
 * Extends {@link com.google.openbidder.api.match.MatchRequest.Builder}
 * with additional features and defaults for unit testing.
 */
public class TestMatchRequestBuilder extends MatchRequest.Builder {
  private static final HttpRequest DEFAULT_REQUEST = StandardHttpRequest.newBuilder()
      .setMethod("GET")
      .setUri("http://localhost")
      .build();

  protected TestMatchRequestBuilder() {
    setHttpRequest(DEFAULT_REQUEST);
  }

  public static TestMatchRequestBuilder create() {
    return new TestMatchRequestBuilder();
  }

  public TestMatchRequestBuilder addParameters(String... params) {
    HttpRequest.Builder httpRequest = HttpUtil.builder(getHttpRequest());
    setHttpRequest(httpRequest.addAllParameter(HttpUtil.toMultimap(params)));
    return this;
  }

  public TestMatchRequestBuilder addCookie(CookieOrBuilder cookie) {
    HttpRequest.Builder httpRequest = HttpUtil.builder(getHttpRequest());
    setHttpRequest(httpRequest.addCookie(HttpUtil.built(cookie)));
    return this;
  }
}
