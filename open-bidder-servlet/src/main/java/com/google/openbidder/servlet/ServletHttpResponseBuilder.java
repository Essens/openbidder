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

import com.google.openbidder.http.Cookie;
import com.google.openbidder.http.HttpResponse;
import com.google.openbidder.http.message.ContentHolder;
import com.google.openbidder.http.response.StandardHttpResponseBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

/**
 * {@link com.google.openbidder.http.HttpResponse.Builder} adapter for the Servlet-compatible
 * {@link HttpServletResponse}.
 */
public final class ServletHttpResponseBuilder extends StandardHttpResponseBuilder {
  private static final Logger logger = LoggerFactory.getLogger(ServletHttpResponseBuilder.class);

  private final HttpServletResponse servlet;

  public ServletHttpResponseBuilder(HttpServletResponse servlet) {
    this.servlet = servlet;
  }

  public final HttpServletResponse servlet() {
    return servlet;
  }

  @Override
  protected ContentHolder newContentHolder() {
    return new ServletContentHolder(servlet, ContentHolder.State.OUTPUT);
  }

  @Override
  public HttpResponse build() {
    HttpResponse httpResponse = super.build();

    // status
    servlet.setStatus(httpResponse.getStatusCode());

    // redirect
    if (httpResponse.isRedirect()) {
      try {
        URI redirectUri = httpResponse.getRedirectUri();
        if (redirectUri == null) {
          logger.warn("No redirect URI for {}", httpResponse);
        } else {
          servlet.sendRedirect(redirectUri.toString());
          return httpResponse;
        }
      } catch (IOException e) {
        logger.warn("Error sending redirect: {}", e.toString());
        return httpResponse;
      }
    }

    // error
    if (httpResponse.isError()) {
      try {
        servlet.sendError(httpResponse.getStatusCode());
      } catch (IOException e) {
        logger.warn("Error sending error: {}", e.toString());
      }
      return httpResponse;
    }

    // OK response

    // headers
    for (Entry<String, String> entry : httpResponse.getHeaders().entries()) {
      servlet.setHeader(entry.getKey(), entry.getValue());
    }

    // cookies
    for (Map.Entry<String, Cookie> entry : httpResponse.getCookies().entries()) {
      Cookie cookie = entry.getValue();
      if (cookie.getName().equals(entry.getKey())) {
        javax.servlet.http.Cookie httpCookie =
            new javax.servlet.http.Cookie(cookie.getName(), cookie.getValue());
        if (cookie.getDomain() != null) {
          httpCookie.setDomain(cookie.getDomain());
        }
        httpCookie.setPath(cookie.getPath());
        httpCookie.setSecure(cookie.isSecure());
        httpCookie.setMaxAge(cookie.getMaxAge());
        servlet.addCookie(httpCookie);
      } else {
        logger.warn("Invalid Cookie entry: {}", entry);
      }
    }

    return httpResponse;
  }
}
