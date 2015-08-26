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

package com.google.openbidder.api.testing.impression;

import com.google.openbidder.api.impression.ImpressionRequest;
import com.google.openbidder.http.HttpRequest;
import com.google.openbidder.http.request.StandardHttpRequest;
import com.google.openbidder.http.util.HttpUtil;

/**
 * Extends {@link com.google.openbidder.api.impression.ImpressionRequest.Builder}
 * with additional methods for unit testing on generic exchanges.
 */
public class TestImpressionRequestBuilder extends ImpressionRequest.Builder {
  private static final HttpRequest DEFAULT_REQUEST = StandardHttpRequest.newBuilder()
      .setUri("http://localhost")
      .build();

  protected TestImpressionRequestBuilder() {
    setHttpRequest(DEFAULT_REQUEST);
    setPriceName("price"); // PriceName.DEFAULT
  }

  public static TestImpressionRequestBuilder create() {
    return new TestImpressionRequestBuilder();
  }

  public TestImpressionRequestBuilder setPrice(double price) {
    HttpRequest.Builder httpRequest = HttpUtil.builder(getHttpRequest());
    setHttpRequest(httpRequest.addParameter("price", Double.toString(price)));
    return this;
  }

  public TestImpressionRequestBuilder addParameters(String... params) {
    HttpRequest.Builder httpRequest = HttpUtil.builder(getHttpRequest());
    setHttpRequest(httpRequest.addAllParameter(HttpUtil.toMultimap(params)));
    return this;
  }
}
