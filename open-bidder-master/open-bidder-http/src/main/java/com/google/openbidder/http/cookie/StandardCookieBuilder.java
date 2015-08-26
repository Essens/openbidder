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

import com.google.common.base.MoreObjects;
import com.google.openbidder.http.Cookie;

import javax.annotation.Nullable;

class StandardCookieBuilder implements Cookie.Builder {
  private String name;
  private String value;
  private @Nullable String domain;
  private @Nullable String path;
  private boolean secure;
  private @Nullable String comment;
  private int maxAge;
  private int version;

  protected StandardCookieBuilder() {
  }

  protected StandardCookieBuilder(
      String name, String value, @Nullable String domain, @Nullable String path,
      boolean secure, @Nullable String comment, int maxAge, int version) {
    this.name = name;
    this.value = value;
    this.domain = domain;
    this.path = path;
    this.secure = secure;
    this.comment = comment;
    this.maxAge = maxAge;
    this.version = version;
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
  public final @Nullable String getComment() {
    return comment;
  }

  @Override
  public final int getMaxAge() {
    return maxAge;
  }

  @Override
  public final int getVersion() {
    return version;
  }

  @Override
  public final StandardCookieBuilder setName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public final StandardCookieBuilder setValue(String value) {
    this.value = value;
    return this;
  }

  @Override
  public final StandardCookieBuilder setDomain(@Nullable String domain) {
    this.domain = domain;
    return this;
  }

  @Override
  public final StandardCookieBuilder setPath(@Nullable String path) {
    this.path = path;
    return this;
  }

  @Override
  public final StandardCookieBuilder setSecure(boolean secure) {
    this.secure = secure;
    return this;
  }

  @Override
  public final StandardCookieBuilder setComment(@Nullable String comment) {
    this.comment = comment;
    return this;
  }

  @Override
  public final StandardCookieBuilder setMaxAge(int maxAge) {
    this.maxAge = maxAge;
    return this;
  }

  @Override
  public final StandardCookieBuilder setVersion(int version) {
    this.version = version;
    return this;
  }

  @Override
  public StandardCookie build() {
    return new StandardCookie(name, value, domain, path, secure, comment, maxAge, version);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("name", name)
        .add("value", value)
        .add("domain", domain)
        .add("path", path)
        .add("secure", secure)
        .add("comment", comment)
        .add("maxAge", maxAge)
        .add("version", version)
        .toString();
  }
}