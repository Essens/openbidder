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

package com.google.openbidder.http;

import com.google.openbidder.http.cookie.CookieOrBuilder;

import javax.annotation.Nullable;

/**
 * A cookie.
 */
public interface Cookie extends CookieOrBuilder {
  /**
   * @return Builder initialized with properties from this cookie.
   * The builder may not produce the same cookie implementation
   */
  Builder toBuilder();

  /**
   * Builds a {@link Cookie}.
   */
  interface Builder extends CookieOrBuilder {
    Builder setName(String name);
    Builder setValue(String value);
    Builder setDomain(@Nullable String domain);
    Builder setPath(@Nullable String path);
    Builder setSecure(boolean secure);
    Builder setMaxAge(int maxAge);

    Cookie build();
  }
}
