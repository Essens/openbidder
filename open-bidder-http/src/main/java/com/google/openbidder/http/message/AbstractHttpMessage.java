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

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.openbidder.http.Cookie;
import com.google.openbidder.http.HttpMessage;
import com.google.openbidder.http.util.HttpUtil;

import org.joda.time.Instant;

import java.io.BufferedReader;
import java.io.InputStream;

import javax.annotation.Nullable;

/**
 * Base implementation of {@link HttpMessage}.
 */
public abstract class AbstractHttpMessage
extends AbstractHttpMessageOrBuilder implements HttpMessage {

  @Nullable
  @Override
  public final String getHeader(String name) {
    return Iterables.getFirst(getHeaders(name), /* default value  */ null);
  }

  @Override
  public final int getIntHeader(String name) {
    return Integer.parseInt(getHeader(name));
  }

  @Override
  public final Instant getDateHeader(String name) {
    return HttpUtil.parseHttpDate(getHeader(name));
  }

  @Override
  public final ImmutableCollection<String> getHeaders(String name) {
    return getHeaders().get(name);
  }

  @Override
  public final ImmutableSet<String> getHeaderNames() {
    return getHeaders().keySet();
  }

  @Override
  public final ImmutableCollection<Cookie> getCookies(String name) {
    return getCookies().get(name);
  }

  @Override
  public @Nullable Cookie getCookie(String name) {
    ImmutableCollection<Cookie> cookies = getCookies().get(name);
    return cookies.isEmpty() ? null : Iterables.getOnlyElement(cookies);
  }

  @Override
  public final InputStream content() {
    return contentHolder().inputIn();
  }

  @Override
  public final BufferedReader contentReader() {
    return contentHolder().inputReader();
  }

  @Override
  public final int getContentLength() {
    return contentHolder().getLength();
  }
}
