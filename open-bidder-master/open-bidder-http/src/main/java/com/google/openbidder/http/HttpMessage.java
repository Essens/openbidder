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

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.net.MediaType;
import com.google.openbidder.http.message.HttpMessageOrBuilder;

import org.joda.time.Instant;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * HTTP message. Implementations will be immutable, with the exception of the content
 * property which is a "read-once" stream (see {@link #content()}).
 */
public interface HttpMessage extends HttpMessageOrBuilder {

  /**
   * @return First header value by name or {@code null} if it doesn't exist
   */
  @Nullable String getHeader(String name);

  /**
   * @return Int value of a header
   * @throws NumberFormatException if the header doesn'exist or cannot be converted to int
   */
  int getIntHeader(String name);

  /**
   * @return Date value of a header
   * @throws NullPointerException if the header doesn't exist
   * @throws IllegalArgumentException if the header cannot be parsed as a date
   */
  Instant getDateHeader(String name);

  /**
   * @return All values for a given header
   */
  ImmutableCollection<String> getHeaders(String name);

  /**
   * @return Header names
   */
  Set<String> getHeaderNames();

  /**
   * @return All {@link Cookie}s with the given name
   */
  ImmutableCollection<Cookie> getCookies(String name);

  /**
   * @return The single {@link Cookie} with the given name
   */
  @Nullable Cookie getCookie(String name);

  /**
   * @return Content length in bytes (Content-Length header)
   */
  int getContentLength();

  /**
   * @return HTTP payload data as an {@link InputStream}.
   * <p>
   * You can read the same data via {@link #contentReader()}, but only one of these can be invoked.
   */
  InputStream content();

  /**
   * @return HTTP payload data as a {@link BufferedReader}, configured for the message's
   * character encoding (part of {@link #getMediaType()}.
   * <p>
   * You can read the same data via {@link #contentReader()}, but only one of these can be invoked.
   */
  BufferedReader contentReader();

  /**
   * @return Builder initialized with properties from this message (except the content).
   * The builder may not produce the same message implementation
   */
  Builder<?> toBuilder();

  // Overrides for annotations
  @Override public abstract ImmutableMultimap<String, String> getHeaders();
  @Override public abstract ImmutableMultimap<String, Cookie> getCookies();

  /**
   * Builds a {@link HttpMessage}.
   */
  public interface Builder<B extends Builder<B>> extends HttpMessageOrBuilder {

    B addHeader(String name, String value);
    B addHeader(String name, Iterable<String> values);
    B setHeader(String name, String value);
    B setHeader(String name, Iterable<String> values);
    B addAllHeader(Multimap<String, String> headers);
    B setAllHeader(Multimap<String, String> headers);
    B removeHeader(String name, String value);
    B clearHeader();
    B addIntHeader(String name, int value);
    B setIntHeader(String name, int value);
    B addDateHeader(String name, Instant value);
    B setDateHeader(String name, Instant value);
    B removeIntHeader(String name, int value);
    B removeDateHeader(String name, Instant value);
    B removeHeader(String name);

    /**
     * Sets the message's content type (Content-Type header).
     * <p>
     * This includes the character encoding; if absent, UTF-8 will be used as default encoding.
     */
    B setMediaType(MediaType mediaType);

    /**
     * Adds text payload as a {@link String}; has same effect as {@code contentWriter().print()}
     * (will use the same {@link PrintWriter} object, subject to the same buffering).
     * This method is convenient because it enables fluent call chains (good for tests),
     * but {@link PrintWriter} provides a much richer API for writing text data.
     * <p>
     * You can use this method interchangeably with all other content-setting methods.
     */
    B printContent(String s);
    /**
     * Returns an {@link OutputStream} that allows adding payload data.
     * <p>
     * You cannot use this method interchangeably with {@link #contentWriter()}.
     */
    OutputStream content();
    /**
     * Returns an {@link PrintWriter}, configured for the payload's character encoding
     * (set with {@link #setMediaType(MediaType)}), that allows adding payload data.
     * <p>
     * You cannot use this method interchangeably with {@link #content()}.
     */
    PrintWriter contentWriter();

    B addCookie(Cookie cookie);
    B addAllCookie(Iterable<Cookie> cookie);
    B setAllCookie(Iterable<Cookie> cookies);
    B removeCookie(String name);
    B clearCookie();

    /**
     * Builds the message. After this is invoked, the builder instance can be reused, keeping
     * the same properties it had before with the exception of the content which is cleared.
     *
     * @return Built message
     */
    HttpMessage build();
  }
}
