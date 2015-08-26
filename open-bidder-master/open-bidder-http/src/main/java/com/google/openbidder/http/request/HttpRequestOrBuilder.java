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

package com.google.openbidder.http.request;

import com.google.common.collect.Multimap;
import com.google.openbidder.http.Protocol;
import com.google.openbidder.http.message.HttpMessageOrBuilder;

import java.net.URI;

/**
 * Operations shared between {@link com.google.openbidder.http.HttpRequest}
 * and {@link com.google.openbidder.http.HttpRequest.Builder}.
 */
public interface HttpRequestOrBuilder extends HttpMessageOrBuilder {
  /**
   * @return HTTP protocol version
   */
  Protocol getProtocol();

  /**
   * @return All parameters with values as Strings
   */
  Multimap<String, String> getParameters();

  /**
   * @return HTTP method
   */
  String getMethod();

  /**
   * @return The full request URI
   * @throws IllegalArgumentException If the URI cannot be constructed / would be invalid
   */
  URI getUri();
}
