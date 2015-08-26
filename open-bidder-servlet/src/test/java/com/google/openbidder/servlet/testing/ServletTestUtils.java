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

package com.google.openbidder.servlet.testing;

import static java.util.Arrays.asList;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.net.HttpHeaders;
import com.google.openbidder.config.impression.PriceName;
import com.google.openbidder.http.Protocol;
import com.google.openbidder.http.util.HttpUtil;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Vector;

import javax.annotation.Nullable;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Utilities for servlet tests.
 */
@javax.annotation.ParametersAreNonnullByDefault
public class ServletTestUtils {
  public static final URI DEFAULT_BID_URL = HttpUtil.buildUri("http://rtb.example.com/bid");

  public static HttpServletRequest newHttpBidRequest(final byte[] exchangeSpecificBidRequest) {
    try {
      return newHttpRequest("POST", exchangeSpecificBidRequest);
    } catch (IOException e) {
      fail(e.getMessage());
      return null;
    }
  }

  public static HttpServletRequest newImpressionHttpRequest(String price) {
    try {
      HttpServletRequest httpRequest =  newHttpRequest(
          "GET",
          ("http://localhost/impression"
                + (price == null ? "" : "?price=" + price)).getBytes(Charsets.UTF_8));
      when(httpRequest.getParameter(PriceName.DEFAULT)).thenReturn(price);
      when(httpRequest.getParameterValues(PriceName.DEFAULT)).thenReturn(new String[]{price});
      return httpRequest;
    } catch (IOException e) {
      fail(e.getMessage());
      return null;
    }
  }

  public static HttpServletRequest newClickHttpRequest() {
    try {
      return newHttpRequest("GET", "http://localhost/click".getBytes(Charsets.UTF_8));
    } catch (IOException e) {
      fail(e.getMessage());
      return null;
    }
  }

  public static HttpServletRequest newHttpRequest(String method, @Nullable byte[] content)
      throws IOException {
    return newHttpRequest(method, content, DEFAULT_BID_URL.toString(), null, null,
        HttpHeaders.CONTENT_LENGTH, "0", null);
  }

  public static HttpServletRequest newHttpRequest(
      String method, @Nullable byte[] content, String requestUrl,
      @Nullable String paramName, @Nullable String paramValue,
      @Nullable String headerName, @Nullable String headerValue,
      @Nullable Cookie cookie) throws IOException {
    final byte[] bytes = content == null ? new byte[0] : content;
    TestServletInputStream is = new TestServletInputStream(bytes);
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    HttpServletRequest req =  mock(HttpServletRequest.class);
    // Simulate AdX's HTTP requests; except for gzip compression which is uninteresting
    when(req.getProtocol()).thenReturn(Protocol.HTTP_1_1.text());
    when(req.getMethod()).thenReturn(method);
    when(req.getContentLength()).thenReturn(bytes.length);
    when(req.getRemoteHost()).thenReturn("localhost");
    when(req.getCookies()).thenReturn(cookie == null ? null : new Cookie[]{ cookie });
    when(req.getRequestURL()).thenReturn(new StringBuffer(requestUrl));
    when(req.getParameterMap()).thenReturn(paramName == null
        ? ImmutableMap.<String, String[]>of()
        : ImmutableMap.<String, String[]>of(paramName, new String[]{ paramValue }));
    when(req.getHeaderNames()).thenReturn(headerName == null
        ? new Vector<String>().elements()
        : new Vector<>(asList(headerName)).elements());
    when(req.getHeaders(anyString())).thenReturn(headerName == null
        ? new Vector<String>().elements()
        : new Vector<>(asList(headerValue)).elements());
    when(req.getHeader(anyString())).thenReturn(headerName == null ? null : headerValue);
    when(req.getInputStream()).thenReturn(is);
    when(req.getReader()).thenReturn(reader);
    when(req.getCharacterEncoding()).thenReturn("UTF-8");
    return req;
  }

  public static HttpServletRequest newHttpRequestBadInput(String method, @Nullable byte[] content)
      throws IOException {
    HttpServletRequest httpRequest = newHttpRequest(method, content);
    when(httpRequest.getInputStream()).thenReturn(new TestServletInputStream(null));
    return httpRequest;
  }

  public static HttpServletResponse newHttpServletResponse() throws IOException {
    return HttpServletResponseTester.create().getHttpResponse();
  }

  static class TestServletInputStream extends ServletInputStream {
    private final ByteArrayInputStream is;
    TestServletInputStream(@Nullable byte[] bytes) {
      is = bytes == null ? null : new ByteArrayInputStream(bytes);
    }
    @Override public boolean isFinished() {
      return is != null && is.available() == 0;
    }
    @Override public boolean isReady() {
      return true;
    }
    @Override public void setReadListener(ReadListener readListener) {
    }
    @Override public int read() throws IOException {
      if (is == null) {
        throw new IOException("This input is secret. Go away!");
      }
      return is.read();
    }
  }
}
