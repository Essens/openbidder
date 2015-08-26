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

package com.google.openbidder.http.message;

import com.google.common.collect.Multimap;
import com.google.common.net.MediaType;
import com.google.openbidder.http.Cookie;

import javax.annotation.Nullable;

/**
 * Operations shared between {@link com.google.openbidder.http.HttpMessage}
 * and {@link com.google.openbidder.http.HttpMessage.Builder}.
 */
public interface HttpMessageOrBuilder {

  /**
   * @return HTTP headers
   */
  Multimap<String, String> getHeaders();

  /**
   * @return {@link Cookie}s indexed by name
   */
  Multimap<String, Cookie> getCookies();

  // specific headers

  /**
   * Gets the message's content type (Content-Type header).
   * <p>
   * This includes the character encoding; if absent, UTF-8 will be used as default encoding.
   */
  @Nullable MediaType getMediaType();
}
