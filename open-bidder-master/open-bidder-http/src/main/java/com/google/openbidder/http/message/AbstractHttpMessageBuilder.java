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
import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import com.google.openbidder.http.Cookie;
import com.google.openbidder.http.HttpMessage;
import com.google.openbidder.http.util.HttpUtil;

import org.joda.time.Instant;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map.Entry;

/**
 * Base implementation of {@link com.google.openbidder.http.HttpMessage.Builder}.
 */
public abstract class AbstractHttpMessageBuilder<B extends HttpMessage.Builder<B>>
extends AbstractHttpMessageOrBuilder implements HttpMessage.Builder<B> {

  @SuppressWarnings("unchecked")
  protected final B self() {
    return (B) this;
  }

  @Override
  public final B addIntHeader(String name, int value) {
    return addHeader(name, String.valueOf(value));
  }

  @Override
  public final B addDateHeader(String name, Instant value) {
    return addHeader(name, HttpUtil.formatHttpDate(value));
  }

  @Override
  public final B addHeader(String name, Iterable<String> values) {
    for (String value : values) {
      addHeader(name, value);
    }
    return self();
  }

  @Override
  public final B setHeader(String name, String value) {
    return removeHeader(name).addHeader(name, value);
  }

  @Override
  public final B setIntHeader(String name, int value) {
    return setHeader(name, String.valueOf(value));
  }

  @Override
  public final B setDateHeader(String name, Instant value) {
    return setHeader(name, HttpUtil.formatHttpDate(value));
  }

  @Override
  public final B setHeader(String name, Iterable<String> values) {
    return removeHeader(name).addHeader(name, values);
  }

  @Override
  public final B addAllHeader(Multimap<String, String> headers) {
    for (Entry<String, String> header : headers.entries()) {
      addHeader(header.getKey(), header.getValue());
    }
    return self();
  }

  @Override
  public final B setAllHeader(Multimap<String, String> headers) {
    return clearHeader().addAllHeader(headers);
  }

  @Override
  public final B removeIntHeader(String name, int value) {
    return removeHeader(name, String.valueOf(value));
  }

  @Override
  public final B removeDateHeader(String name, Instant value) {
    return removeHeader(name, HttpUtil.formatHttpDate(value));
  }

  @Override
  public final B addHeader(String name, String value) {
    headers().put(name, value);
    return self();
  }

  @Override
  public final B removeHeader(String name, String value) {
    headers().remove(name, value);
    return self();
  }

  @Override
  public final B removeHeader(String name) {
    headers().removeAll(name);
    return self();
  }

  @Override
  public final B clearHeader() {
    headers().clear();
    return self();
  }

  protected abstract Multimap<String, String> headers();

  @Override
  public final B setMediaType(MediaType mediaType) {
    return setHeader(HttpHeaders.CONTENT_TYPE, mediaType.toString());
  }

  @Override
  public final B addAllCookie(Iterable<Cookie> cookies) {
    for (Cookie cookie : cookies) {
      addCookie(cookie);
    }
    return self();
  }

  @Override
  public final B setAllCookie(Iterable<Cookie> cookies) {
    return clearCookie().addAllCookie(cookies);
  }

  @Override
  public final B printContent(String s) {
    contentWriter().print(s);
    return self();
  }

  @Override
  public final OutputStream content() {
    return contentHolder().outputOut();
  }

  @Override
  public final PrintWriter contentWriter() {
    return contentHolder().outputWriter();
  }
}
