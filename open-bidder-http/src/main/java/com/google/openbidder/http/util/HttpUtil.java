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

package com.google.openbidder.http.util;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.CharStreams;
import com.google.openbidder.http.Cookie;
import com.google.openbidder.http.HttpMessage;
import com.google.openbidder.http.HttpRequest;
import com.google.openbidder.http.HttpResponse;
import com.google.openbidder.http.cookie.CookieOrBuilder;
import com.google.openbidder.http.request.HttpRequestOrBuilder;
import com.google.openbidder.http.response.HttpResponseOrBuilder;

import org.joda.time.Instant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * HTTP related common utilities.
 */
public final class HttpUtil {
  public static final DateTimeFormatter HTTP_DATE_FORMAT = // RFC 1123
      DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss z");

  private HttpUtil() {
  }

  /**
   * @return A {@link URI} from the String URI
   * @throws IllegalArgumentException if a {@link URISyntaxException} is thrown
   */
  public static URI buildUri(String uri) {
    try {
      return new URI(uri);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("Invalid URI: " + uri, e);
    }
  }

  /**
   * @return {@link Instant} parsed from RFC 1123 compliant date
   */
  public static Instant parseHttpDate(String dateText) {
    return HTTP_DATE_FORMAT.parseDateTime(dateText).toInstant();
  }

  /**
   * @return Text for the {@link Instant} represented in RFC 1123 date format
   */
  public static String formatHttpDate(Instant instant) {
    return HTTP_DATE_FORMAT.print(instant);
  }

  /**
   * @return Path elements concatenated together adding separator slashes as required
   */
  public static String concatPaths(String... paths) {
    StringBuilder buf = new StringBuilder();
    for (String path : paths) {
      if (buf.length() == 0 || buf.charAt(buf.length() - 1) != '/') {
        buf.append('/');
      }
      if (path.length() > 1 && path.charAt(0) == '/') {
        buf.append(path, 1, path.length());
      } else if (path.length() > 0) {
        buf.append(path);
      }
    }
    int len = buf.length();
    if (len == 0 || len == 1 && buf.charAt(0) == '/') {
      return "/";
    } else {
      if (buf.charAt(len - 1) == '/') {
        buf.deleteCharAt(len - 1);
      }
    }
    return buf.toString();
  }

  public static Multimap<String, String> toMultimap(String... params) {
    checkArgument(params.length % 2 == 0);
    ImmutableMultimap.Builder<String, String> mmap = ImmutableMultimap.builder();
    for (int i = 0; i < params.length; i += 2) {
      mmap.put(params[i], params[i + 1]);
    }
    return mmap.build();
  }

  public static HttpRequest built(HttpRequestOrBuilder rob) {
    return rob instanceof HttpRequest.Builder
        ? ((HttpRequest.Builder) rob).build()
        : (HttpRequest) rob;
  }

  public static HttpRequest.Builder builder(HttpRequestOrBuilder rob) {
    return rob instanceof HttpRequest
        ? ((HttpRequest) rob).toBuilder()
        : (HttpRequest.Builder) rob;
  }

  public static HttpResponse built(HttpResponseOrBuilder rob) {
    return rob instanceof HttpResponse.Builder
        ? ((HttpResponse.Builder) rob).build()
        : (HttpResponse) rob;
  }

  public static HttpResponse.Builder builder(HttpResponseOrBuilder rob) {
    return rob instanceof HttpResponse
        ? ((HttpResponse) rob).toBuilder()
        : (HttpResponse.Builder) rob;
  }

  public static Cookie built(CookieOrBuilder cookieOrBuilder) {
    return cookieOrBuilder instanceof Cookie.Builder
        ? ((Cookie.Builder) cookieOrBuilder).build()
        : (Cookie) cookieOrBuilder;
  }

  public static Cookie.Builder builder(CookieOrBuilder cob) {
    return cob instanceof Cookie
        ? ((Cookie) cob).toBuilder()
        : (Cookie.Builder) cob;
  }

  public static String readContentString(HttpMessage msg) {
    try {
      return CharStreams.toString(new InputStreamReader(msg.content(), Charsets.UTF_8));
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }
}
