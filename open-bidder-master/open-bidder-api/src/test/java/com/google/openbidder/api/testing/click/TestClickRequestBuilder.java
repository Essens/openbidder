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

package com.google.openbidder.api.testing.click;

import com.google.openbidder.api.click.ClickRequest;
import com.google.openbidder.http.HttpRequest;
import com.google.openbidder.http.request.StandardHttpRequest;
import com.google.openbidder.http.util.HttpUtil;

/**
 * Extends {@link com.google.openbidder.api.click.ClickRequest.Builder}
 * with additional methods for unit testing on generic exchanges.
 */
public class TestClickRequestBuilder extends ClickRequest.Builder {
  private static final HttpRequest DEFAULT_REQUEST = StandardHttpRequest.newBuilder()
      .setMethod("GET")
      .setUri("http://localhost")
      .build();

  protected TestClickRequestBuilder() {
    setHttpRequest(DEFAULT_REQUEST);
    setExchange(defaultExchange());
  }

  public static TestClickRequestBuilder create() {
    return new TestClickRequestBuilder();
  }

  public TestClickRequestBuilder addParameters(String... params) {
    HttpRequest.Builder httpRequest = HttpUtil.builder(getHttpRequest());
    setHttpRequest(httpRequest.addAllParameter(HttpUtil.toMultimap(params)));
    return this;
  }
}
