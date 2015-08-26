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

package com.google.openbidder.http.message;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.api.client.util.escape.CharEscapers;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import com.google.openbidder.http.Cookie;
import com.google.openbidder.http.HttpRequest;
import com.google.openbidder.http.Protocol;
import com.google.openbidder.http.cookie.StandardCookie;
import com.google.openbidder.http.request.AbstractHttpRequest;
import com.google.openbidder.http.request.HttpRequestOrBuilder;
import com.google.openbidder.http.request.StandardHttpRequest;
import com.google.openbidder.http.util.HttpUtil;
import com.google.openbidder.util.testing.TestUtil;

import org.joda.time.Instant;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Tests for {@link HttpRequest}.
 */
public class HttpRequestTest {

  @Test
  public void testCommonMethods() {
    HttpRequest.Builder reqBuilder = StandardHttpRequest.newBuilder().setUri("http://a.io");
    TestUtil.testCommonMethods(reqBuilder);
    TestUtil.testCommonMethods(reqBuilder.build());
  }

  @Test
  public void testRequest() throws URISyntaxException, IOException {
    Cookie cookie = StandardCookie.create("name", "value");
    String plain = "+";
    String enc = CharEscapers.escapeUri(plain);
    String enc2 = CharEscapers.escapeUri(enc);
    Instant now = Instant.parse("2014-01-01T00:00:00+00");
    HttpRequest.Builder reqBuilder = StandardHttpRequest.newBuilder()
        .setAllParameter(ImmutableMultimap.of("p1", enc, "p2", enc2))
        .setUri("http://host:8080/path?p1=v1")
        .setUri(new URI("http://host:8080/path?p1=v1"))
        .setMethod("GET")
        .setProtocol(Protocol.HTTP_1_1)
        .addAllParameter(ImmutableMultimap.of("p1", enc))
        .setAllParameter(ImmutableMultimap.of("p1", enc, "p2", enc2))
        .addParameter("p2", enc2)
        .setParameter("p2", enc2)
        .setParameter("p2", ImmutableList.of(enc2))
        .removeParameter("p", "v")
        .addAllHeader(ImmutableMultimap.of("h1", "header1"))
        .setAllHeader(ImmutableMultimap.of("h1", "header1"))
        .setHeader("h2", "header2")
        .setHeader("h2", ImmutableList.of("header2"))
        .addHeader("h2", "header2")
        .addHeader("h2", ImmutableList.of("header2"))
        .removeHeader("none")
        .removeHeader("none", "v")
        .addAllCookie(ImmutableList.of(cookie))
        .setAllCookie(ImmutableList.of(cookie))
        .addCookie(cookie)
        .removeCookie("none")
        .printContent("ping")
        .addDateHeader("d1", now)
        .setDateHeader("d1", now)
        .removeDateHeader("none", now)
        .addIntHeader("i1", 1)
        .setIntHeader("i1", 1)
        .removeIntHeader("none", 1)
        .setMediaType(MediaType.ANY_TEXT_TYPE) // no charset
        .setMediaType(MediaType.JSON_UTF_8); // has charset
    HttpRequest req = HttpUtil.built(HttpUtil.built(reqBuilder));

    assertHttpRequestOrBuilder(req);
    assertHttpRequestOrBuilder(reqBuilder);

    assertNotNull(HttpUtil.builder(HttpUtil.builder(req)));
    assertEquals("ping", HttpUtil.readContentString(req));
    assertNull(reqBuilder.build().contentReader().readLine());
    assertEquals(4, req.getContentLength());
    assertNull(req.getParameter("none"));
    assertNull(req.getParameterDecoded("none"));
    assertNull(req.getParameterDecoded2("none"));
    assertTrue(req.getParameters("none").isEmpty());
    assertEquals(enc2, req.getParameter("p2"));
    assertEquals(enc, req.getParameterDecoded("p2"));
    assertEquals(plain, req.getParameterDecoded2("p2"));
    assertEquals(enc, req.getParameter("p1"));
    assertEquals(plain, req.getParameterDecoded("p1"));
    assertEquals(" ", req.getParameterDecoded2("p1"));
    assertEquals(ImmutableSet.of("p1", "p2"), req.getParameterNames());
    assertEquals(1, req.getCookies(cookie.getName()).size());
    assertEquals("value", req.getCookie(cookie.getName()).getValue());
    assertFalse(req.isSecure());
    assertEquals("header1", req.getHeader("h1"));
    assertEquals(1, req.getIntHeader("i1"));
    assertEquals(now, req.getDateHeader("d1"));
    assertEquals(
        ImmutableSet.of("h1", "h2", "i1", "d1", HttpHeaders.CONTENT_TYPE),
        req.getHeaderNames());
    assertEquals(ImmutableList.of("header1"), req.getHeaders("h1"));

    reqBuilder.clearCookie();
    assertTrue(reqBuilder.getCookies().isEmpty());
    reqBuilder.clearHeader();
    assertTrue(reqBuilder.getHeaders().isEmpty());
    reqBuilder.clearParameter();
    assertTrue(reqBuilder.getParameters().isEmpty());
    reqBuilder.contentWriter().print("ping");
    assertEquals("ping", reqBuilder.build().contentReader().readLine());
    reqBuilder.content().write("ping".getBytes(Charsets.UTF_8));
    assertEquals("ping", HttpUtil.readContentString(reqBuilder.build()));
    reqBuilder = StandardHttpRequest.newBuilder().setUri("http://a.io");
    assertNull(reqBuilder.build().getServerName());
    assertEquals("a.io", reqBuilder.setHeader("Host", "a.io").build().getServerName());
    assertEquals("a.io", reqBuilder.setHeader("Host", "a.io:999").build().getServerName());
    assertEquals(999, reqBuilder.setHeader("Host", "a.io:999").build().getServerPort());
    assertEquals(80, reqBuilder.setUri("http://a.io").setHeader("Host", "a.io")
        .build().getServerPort());
    assertEquals(443, reqBuilder.setUri("https://a.io").setHeader("Host", "a.io")
        .build().getServerPort());
  }

  protected void assertHttpRequestOrBuilder(HttpRequestOrBuilder rob) {
    assertNotNull(rob.toString());
    assertEquals(1, rob.getCookies().size());
    assertEquals(5, rob.getHeaders().size());
    assertEquals(MediaType.JSON_UTF_8, rob.getMediaType());
    assertEquals("GET", rob.getMethod());
    assertEquals(2, rob.getParameters().size());
    assertEquals(Protocol.HTTP_1_1, rob.getProtocol());
    assertEquals("http://host:8080/path", rob.getUri().toString());
  }

  @Test
  public void testAbstractHttpRequest() {
    HttpRequest req = new AbstractHttpRequestTester();
    assertNotNull(req.toBuilder().build());
  }

  public void test_multiValued_ok() {
    HttpRequest req = newRequest(ImmutableMultimap.of("k", "v1", "k", "v2"));
    assertEquals(ImmutableList.of("v1", "v2"), req.getParameters("k"));
    assertEquals(1, req.getParameters().size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_multiValued_bad() {
    HttpRequest req = newRequest(ImmutableMultimap.of("k", "v1", "k", "v2"));
    req.getParameter("k");
  }

  private static HttpRequest newRequest(Multimap<String, String> parameters) {
    return StandardHttpRequest.newBuilder()
        .setUri("http://example.com")
        .setAllParameter(parameters)
        .build();
  }

  static class AbstractHttpRequestTester extends AbstractHttpRequest {
    @Override public ImmutableMultimap<String, String> getParameters() {
      return ImmutableMultimap.of();
    }
    @Override public ImmutableMultimap<String, String> getHeaders() {
      return ImmutableMultimap.of();
    }
    @Override public ImmutableMultimap<String, Cookie> getCookies() {
      return ImmutableMultimap.of();
    }
    @Override public Protocol getProtocol() { return Protocol.HTTP_1_1; }
    @Override public String getMethod() { return "GET"; }
    @Override public URI getUri() { return HttpUtil.buildUri("http://example.com"); }
    @Override public InetSocketAddress getRemoteAddress() {
      return InetSocketAddress.createUnresolved("localhost", 8080);
    }
  }
}
