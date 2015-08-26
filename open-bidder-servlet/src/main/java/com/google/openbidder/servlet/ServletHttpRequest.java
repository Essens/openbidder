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

import com.google.common.collect.ImmutableMultimap;
import com.google.openbidder.http.Cookie;
import com.google.openbidder.http.HttpRequest;
import com.google.openbidder.http.Protocol;
import com.google.openbidder.http.message.ContentHolder;
import com.google.openbidder.http.request.AbstractHttpRequest;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * {@link HttpRequest} adapter for the Servlet-compatible {@link HttpServletRequest}.
 * This adapter makes lazy conversion possible: only properties read at least once will
 * be converted from the underlying Servlet request.
 */
public final class ServletHttpRequest extends AbstractHttpRequest {
  protected final HttpServletRequest servlet;
  private Protocol protocol;
  private URI uri;
  private ImmutableMultimap<String, String> headers;
  private ImmutableMultimap<String, String> parameters;
  private ImmutableMultimap<String, Cookie> cookies;

  public ServletHttpRequest(HttpServletRequest servlet) {
    this.servlet = servlet;
  }

  public HttpServletRequest servlet() {
    return servlet;
  }

  @Override
  public ImmutableMultimap<String, String> getParameters() {
    if (parameters == null) {
      Map<String, String[]> httpMap = servlet.getParameterMap();
      if (httpMap.isEmpty()) {
        parameters = ImmutableMultimap.of();
      } else {
        ImmutableMultimap.Builder<String, String> builder = ImmutableMultimap.builder();
        for (Map.Entry<String, String[]> entry : httpMap.entrySet()) {
          for (String value : entry.getValue()) {
            builder.put(entry.getKey(), value);
          }
        }
        parameters = builder.build();
      }
    }
    return parameters;
  }

  @Override
  public ImmutableMultimap<String, String> getHeaders() {
    if (headers == null) {
      Enumeration<String> names = servlet.getHeaderNames();
      if (names.hasMoreElements()) {
        ImmutableMultimap.Builder<String, String> builder = ImmutableMultimap.builder();
        while (names.hasMoreElements()) {
          String name = names.nextElement();
          for (Enumeration<String> value = servlet.getHeaders(name); value.hasMoreElements(); ) {
            builder.put(name, value.nextElement());
          }
        }
        headers = builder.build();
      } else {
        headers = ImmutableMultimap.of();
      }
    }
    return headers;
  }

  @Override
  public ImmutableMultimap<String, Cookie> getCookies() {
    if (cookies == null) {
      javax.servlet.http.Cookie[] httpCookies = servlet.getCookies();
      if (httpCookies == null || httpCookies.length == 0) {
        cookies = ImmutableMultimap.of();
      } else {
        ImmutableMultimap.Builder<String, Cookie> builder = ImmutableMultimap.builder();
        for (javax.servlet.http.Cookie httpCookie : httpCookies) {
          ServletCookie cookie = new ServletCookie(httpCookie);
          builder.put(cookie.getName(), cookie);
        }
        cookies = builder.build();
      }
    }
    return cookies;
  }

  @Override
  public Protocol getProtocol() {
    if (protocol == null) {
      this.protocol = Protocol.decode(servlet.getProtocol());
    }
    return protocol;
  }

  @Override
  public String getMethod() {
    return servlet.getMethod();
  }

  @Override
  public URI getUri() {
    if (uri == null) {
      try {
        StringBuffer sb = servlet.getRequestURL();
        String q = servlet.getQueryString();
        if (q != null) {
          sb.append('?').append(q);
        }
        uri = new URI(sb.toString());
      } catch (URISyntaxException e) {
        throw new IllegalStateException(
            "Servlet request has invalid URI: " + servlet.getRequestURL(), e);
      }
    }
    return uri;
  }

  @Override
  public InetSocketAddress getRemoteAddress() {
    return InetSocketAddress.createUnresolved(servlet.getRemoteHost(), servlet.getRemotePort());
  }

  @Override
  protected ContentHolder newContentHolder() {
    return new ServletContentHolder(servlet, ContentHolder.State.INPUT);
  }
}
