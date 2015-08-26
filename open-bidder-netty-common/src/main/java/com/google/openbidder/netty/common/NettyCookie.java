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

package com.google.openbidder.netty.common;

import com.google.openbidder.http.Cookie;
import com.google.openbidder.http.cookie.AbstractCookie;

import io.netty.handler.codec.http.cookie.DefaultCookie;


/**
 * Netty-optimized {@link Cookie} implementation.
 */
public class NettyCookie extends AbstractCookie {
  private final io.netty.handler.codec.http.cookie.Cookie self;

  public NettyCookie(io.netty.handler.codec.http.cookie.Cookie self) {
    this.self = self;
  }

  public NettyCookie (Cookie cookie) {
    this(getNettyCookie(cookie));
  }

  public io.netty.handler.codec.http.cookie.Cookie self() {
    return self;
  }

  public static io.netty.handler.codec.http.cookie.Cookie getNettyCookie(Cookie cookie) {
    if (cookie instanceof NettyCookie) {
      return ((NettyCookie) cookie).self;
    } else {
      DefaultCookie self = new DefaultCookie(cookie.getName(), cookie.getValue());
      self.setDomain(cookie.getDomain());
      self.setPath(cookie.getPath());
      self.setSecure(cookie.isSecure());
      self.setMaxAge(cookie.getMaxAge());
      return self;
    }
  }

  @Override
  public String getName() {
    return self.name();
  }

  @Override
  public String getValue() {
    return self.value();
  }

  @Override
  public String getDomain() {
    return self.domain();
  }

  @Override
  public String getPath() {
    return self.path();
  }

  @Override
  public boolean isSecure() {
    return self.isSecure();
  }

  @Override
  public int getMaxAge() {
    return (int) self.maxAge();
  }
}
