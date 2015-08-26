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

import io.netty.handler.codec.http.DefaultCookie;

/**
 * Netty-optimized {@link Cookie} implementation.
 */
public class NettyCookie extends AbstractCookie {
  private final io.netty.handler.codec.http.Cookie self;

  public NettyCookie(io.netty.handler.codec.http.Cookie self) {
    this.self = self;
  }

  public NettyCookie (Cookie cookie) {
    this(getNettyCookie(cookie));
  }

  public io.netty.handler.codec.http.Cookie self() {
    return self;
  }

  public static io.netty.handler.codec.http.Cookie getNettyCookie(Cookie cookie) {
    if (cookie instanceof NettyCookie) {
      return ((NettyCookie) cookie).self;
    } else {
      DefaultCookie self = new DefaultCookie(cookie.getName(), cookie.getValue());
      self.setDomain(cookie.getDomain());
      self.setPath(cookie.getPath());
      self.setSecure(cookie.isSecure());
      self.setComment(cookie.getComment());
      self.setMaxAge(cookie.getMaxAge());
      self.setVersion(cookie.getVersion());
      return self;
    }
  }

  @Override
  public String getName() {
    return self.getName();
  }

  @Override
  public String getValue() {
    return self.getValue();
  }

  @Override
  public String getDomain() {
    return self.getDomain();
  }

  @Override
  public String getPath() {
    return self.getPath();
  }

  @Override
  public boolean isSecure() {
    return self.isSecure();
  }

  @Override
  public String getComment() {
    return self.getComment();
  }

  @Override
  public int getMaxAge() {
    return (int) self.getMaxAge();
  }

  @Override
  public int getVersion() {
    return self.getVersion();
  }
}
