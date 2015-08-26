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

package com.google.openbidder.http.cookie;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.openbidder.http.Cookie;

import javax.annotation.Nullable;

/**
 * Standard {@link Cookie} implementation.
 */
public class StandardCookie extends AbstractCookie {
  private final String name;
  private final String value;
  private final @Nullable String domain;
  private final @Nullable String path;
  private final boolean secure;
  private final int maxAge;

  /**
   * Creates the Cookie, allowing to set both mandatory and optional attributes.
   */
  protected StandardCookie(String name, String value,
      @Nullable String domain, @Nullable String path, boolean secure, int maxAge) {
    this.name = checkNotNull(name);
    this.value = checkNotNull(value);
    this.domain = domain;
    this.path = path;
    this.secure = secure;
    this.maxAge = maxAge;
  }

  /**
   * Returns a builder that will produce a Cookie.
   */
  public static Cookie.Builder newBuilder() {
    return new StandardCookieBuilder();
  }

  public static Cookie create(String name, String value) {
    return new StandardCookie(name, value, null, null, false, 0);
  }

  @Override
  public Cookie.Builder toBuilder() {
    return new StandardCookieBuilder(
        getName(),
        getValue(),
        getDomain(),
        getPath(),
        isSecure(),
        getMaxAge());
  }

  @Override
  public final String getName() {
    return name;
  }

  @Override
  public final String getValue() {
    return value;
  }

  @Override
  public final @Nullable String getDomain() {
    return domain;
  }

  @Override
  public final @Nullable String getPath() {
    return path;
  }

  @Override
  public final boolean isSecure() {
    return secure;
  }

  @Override
  public final int getMaxAge() {
    return maxAge;
  }
}
