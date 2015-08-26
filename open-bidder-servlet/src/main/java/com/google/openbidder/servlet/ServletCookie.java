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

package com.google.openbidder.servlet;

import com.google.openbidder.http.cookie.AbstractCookie;

public class ServletCookie extends AbstractCookie {
  protected final javax.servlet.http.Cookie self;

  public ServletCookie(javax.servlet.http.Cookie self) {
    this.self = self;
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
    return self.getSecure();
  }

  @Override
  public int getMaxAge() {
    return self.getMaxAge();
  }
}
