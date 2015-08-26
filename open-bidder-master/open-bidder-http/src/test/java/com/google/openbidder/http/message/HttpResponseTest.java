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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import com.google.openbidder.http.Cookie;
import com.google.openbidder.http.HttpResponse;
import com.google.openbidder.http.cookie.StandardCookie;
import com.google.openbidder.http.response.AbstractHttpResponse;
import com.google.openbidder.http.response.HttpResponseOrBuilder;
import com.google.openbidder.http.response.StandardHttpResponse;
import com.google.openbidder.http.util.HttpUtil;
import com.google.openbidder.util.testing.TestUtil;

import org.joda.time.Instant;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Tests for {@link HttpResponse}.
 */
public class HttpResponseTest {

  @Test
  public void testCommonMethods() {
    HttpResponse.Builder respBuilder = StandardHttpResponse.newBuilder();
    TestUtil.testCommonMethods(respBuilder);
    TestUtil.testCommonMethods(respBuilder.build());
  }

  @Test
  public void testResponse() throws URISyntaxException, IOException {
    Cookie cookie1 = StandardCookie.create("name1", "value1");
    Cookie cookie2 = StandardCookie.create("name2", "value2");
    Instant now = Instant.parse("2014-01-01T00:00:00+00");
    HttpResponse.Builder respBuilder = StandardHttpResponse.newBuilder()
        .setRedirectUri("http://host:8080/path?p1=v1")
        .setRedirectUri(new URI("http://host:8080/path?p1=v1"))
        .setRedirectHost("host")
        .setRedirectPort(8080)
        .setRedirectPath("/path")
        .setRedirectParameter("p1", "v1")
        .removeRedirectParameter("none")
        .removeRedirectParameter("none", "v")
        .setRedirectFragment("fragment")
        .setStatusOk()
        .setStatusCode(301)
        .setAllCookie(ImmutableList.of(cookie1))
        .addAllCookie(ImmutableList.of(cookie1))
        .addCookie(cookie2)
        .removeCookie("none")
        .addAllHeader(ImmutableMultimap.of("h1", "header1"))
        .setAllHeader(ImmutableMultimap.of("h1", "header1"))
        .addHeader("h2", "header2")
        .setHeader("h2", "header2")
        .removeHeader("none")
        .removeHeader("none", "v")
        .printContent("pong")
        .addDateHeader("d1", now)
        .setDateHeader("d1", now)
        .addIntHeader("i1", 1)
        .setIntHeader("i1", 1)
        .setMediaType(MediaType.ANY_TEXT_TYPE) // no charset
        .setMediaType(MediaType.JSON_UTF_8); // has charset
    HttpResponse resp = HttpUtil.built(HttpUtil.built(respBuilder));

    assertResponseOrBuilder(resp);
    assertResponseOrBuilder(respBuilder);

    assertNotNull(HttpUtil.builder(HttpUtil.builder(resp)));
    assertEquals("pong", HttpUtil.readContentString(resp));
    assertNull(respBuilder.build().contentReader().readLine());
    assertEquals(4, resp.getContentLength());
    assertEquals(1, resp.getCookies(cookie1.getName()).size());
    assertEquals("value1", resp.getCookie(cookie1.getName()).getValue());
    assertEquals("header1", resp.getHeader("h1"));
    assertEquals(1, resp.getIntHeader("i1"));
    assertEquals(now, resp.getDateHeader("d1"));
    assertEquals(
        ImmutableSet.of("h1", "h2", "i1", "d1", HttpHeaders.CONTENT_TYPE),
        resp.getHeaderNames());
    assertEquals(ImmutableList.of("header1"), resp.getHeaders("h1"));
    assertTrue(!resp.isOk());
    assertTrue(resp.isRedirect());
    assertTrue(resp.isValidRedirect());
    assertTrue(!resp.isClientError());
    assertTrue(!resp.isServerError());
    assertTrue(!resp.isError());
    assertEquals("host", resp.getRedirectHost());
    assertEquals((Integer) 8080, resp.getRedirectPort());
    assertEquals("/path", resp.getRedirectPath());
    assertEquals("fragment", resp.getRedirectFragment());

    respBuilder.clearCookie();
    assertTrue(respBuilder.getCookies().isEmpty());
    respBuilder.clearHeader();
    assertTrue(respBuilder.getHeaders().isEmpty());
    respBuilder.clearRedirectUri();
    assertNull(respBuilder.getRedirectUri());
    respBuilder.clearRedirectParameter();
    assertTrue(respBuilder.getRedirectParameters().isEmpty());
    respBuilder.contentWriter().print("pong");
    assertEquals("pong", respBuilder.build().contentReader().readLine());
    respBuilder.content().write("pong".getBytes(Charsets.UTF_8));
    assertEquals("pong", HttpUtil.readContentString(respBuilder.build()));
  }

  private void assertResponseOrBuilder(HttpResponseOrBuilder rob) {
    assertNotNull(rob.toString());
    assertEquals(2, rob.getCookies().size());
    assertEquals(5, rob.getHeaders().size());
    assertEquals(MediaType.JSON_UTF_8, rob.getMediaType());
    assertEquals(301, rob.getStatusCode());
    assertEquals("http://host:8080/path?p1=v1#fragment", String.valueOf(rob.getRedirectUri()));
    assertEquals("host", rob.getRedirectHost());
    assertEquals((Integer) 8080, rob.getRedirectPort());
    assertEquals("/path", rob.getRedirectPath());
    assertEquals("v1", rob.getRedirectParameter("p1"));
    assertEquals(ImmutableSet.of("p1"), rob.getRedirectParameterNames());
    assertEquals(ImmutableSet.of("v1"), ImmutableSet.copyOf(rob.getRedirectParameters("p1")));
    assertEquals("fragment", rob.getRedirectFragment());
    assertTrue(rob.hasRedirectUri());
    assertTrue(rob.containsRedirectParameter("p1"));
  }

  @Test
  public void testAbstractHttpResponse() {
    HttpResponse resp = new AbstractHttpResponseTester();
    assertNotNull(resp.toBuilder().build());
  }

  static class AbstractHttpResponseTester extends AbstractHttpResponse {
    @Override public ImmutableMultimap<String, String> getRedirectParameters() {
      return ImmutableMultimap.of();
    }
    @Override public ImmutableMultimap<String, String> getHeaders() {
      return ImmutableMultimap.of();
    }
    @Override public ImmutableMultimap<String, Cookie> getCookies() {
      return ImmutableMultimap.of();
    }
    @Override public int getStatusCode() { return 301; }
    @Override public boolean hasRedirectUri() { return false; }
    @Override public URI getRedirectUri() {
      return HttpUtil.buildUri("http://host:8080/path?p1=v1");
    }
  }
}
