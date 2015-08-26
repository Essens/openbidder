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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Charsets;
import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import com.google.openbidder.http.Protocol;
import com.google.openbidder.http.util.HttpUtil;
import com.google.openbidder.servlet.testing.ServletTestUtils;
import com.google.openbidder.util.testing.TestUtil;

import org.junit.Test;

import java.io.IOException;

import javax.servlet.http.Cookie;

/**
 * Tests for {@link ServletHttpRequest}.
 */
public class ServletHttpRequestTest {

  @Test
  public void testRequest() throws IOException {
    ServletHttpRequest req = new ServletHttpRequest(ServletTestUtils.newHttpRequest(
        "GET", "ping".getBytes(Charsets.UTF_8.name()), "http://a.io:999", "p1", "v1",
        HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString(), new Cookie("name", "value")));
    TestUtil.testCommonMethods(req);
    assertEquals(HttpUtil.buildUri("http://a.io:999"), req.getUri());
    assertSame(Protocol.HTTP_1_1, req.getProtocol());
    assertEquals(MediaType.JSON_UTF_8.toString(), req.getHeader(HttpHeaders.CONTENT_TYPE));
    assertEquals("GET", req.getMethod());
    assertEquals("v1", req.getParameter("p1"));
    assertEquals("value", req.getCookie("name").getValue());
    assertSame(MediaType.JSON_UTF_8, req.getMediaType());
    assertEquals("ping", req.contentReader().readLine());
    assertNotNull(req.servlet());
  }

  @Test
  public void testRequestEmpty() throws IOException {
    ServletHttpRequest req = new ServletHttpRequest(ServletTestUtils.newHttpRequest(
        "GET", new byte[0]));
    TestUtil.testCommonMethods(req);
    assertEquals(ServletTestUtils.DEFAULT_BID_URL, req.getUri());
    assertSame(Protocol.HTTP_1_1, req.getProtocol());
    assertEquals("GET", req.getMethod());
    assertEquals(1, req.getHeaders().size());
    assertTrue(req.getParameters().isEmpty());
    assertTrue(req.getCookies().isEmpty());
    assertNull(req.getMediaType());
    assertNull(req.contentReader().readLine());
  }
}
