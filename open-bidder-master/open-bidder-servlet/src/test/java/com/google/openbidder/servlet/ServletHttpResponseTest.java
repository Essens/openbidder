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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.common.net.MediaType;
import com.google.openbidder.http.HttpResponse;
import com.google.openbidder.http.cookie.StandardCookie;
import com.google.openbidder.servlet.testing.ServletTestUtils;
import com.google.openbidder.util.testing.TestUtil;

import org.apache.http.HttpStatus;
import org.junit.Test;

import java.io.IOException;

/**
 * Tests for {@link ServletHttpResponseBuilder}.
 */
public class ServletHttpResponseTest {

  @Test
  public void testResponseBuilderRedirect() throws IOException {
    HttpResponse.Builder respBuilder = new ServletHttpResponseBuilder(
        ServletTestUtils.newHttpServletResponse())
            .setRedirectUri("http://a.io:999?p1=v1")
            .addRedirectParameter("p2", "v2")
            .setStatusCode(HttpStatus.SC_MOVED_TEMPORARILY);
    TestUtil.testCommonMethods(respBuilder);
    assertEquals("v1", respBuilder.getRedirectParameter("p1"));
    assertTrue(respBuilder.hasRedirectUri());
    assertTrue(respBuilder.getHeaders().isEmpty());
    assertTrue(respBuilder.getCookies().isEmpty());
    assertNull(respBuilder.getMediaType());
    assertNotNull(((ServletHttpResponseBuilder) respBuilder).servlet());
    assertNotNull(respBuilder.build());
  }

  @Test
  public void testResponseBuilderRegular() throws IOException {
    HttpResponse.Builder respBuilder = new ServletHttpResponseBuilder(
        ServletTestUtils.newHttpServletResponse())
            .addHeader("h1", "header1")
            .addCookie(StandardCookie.create("name1", "value1"))
            .addCookie(StandardCookie.newBuilder()
                .setName("name2")
                .setValue("value2").setDomain("xyz.com")
                .build())
            .setMediaType(MediaType.JSON_UTF_8)
            .printContent("pong");
    TestUtil.testCommonMethods(respBuilder);
    assertTrue(respBuilder.getRedirectParameters().isEmpty());
    assertFalse(respBuilder.hasRedirectUri());
    assertEquals(2, respBuilder.getHeaders().size());
    assertEquals(2, respBuilder.getCookies().size());
    assertSame(MediaType.JSON_UTF_8, respBuilder.getMediaType());
    assertNotNull(respBuilder.build());
  }

  @Test
  public void testResponseBuilderEmpty() throws IOException {
    HttpResponse.Builder respBuilder = new ServletHttpResponseBuilder(
        ServletTestUtils.newHttpServletResponse());
    TestUtil.testCommonMethods(respBuilder);
    assertTrue(respBuilder.getRedirectParameters().isEmpty());
    assertFalse(respBuilder.hasRedirectUri());
    assertTrue(respBuilder.getHeaders().isEmpty());
    assertTrue(respBuilder.getCookies().isEmpty());
    assertNull(respBuilder.getMediaType());
    assertNotNull(respBuilder.build());
  }

  @Test
  public void testResponseBuilderError() throws IOException {
    HttpResponse.Builder respBuilder = new ServletHttpResponseBuilder(
        ServletTestUtils.newHttpServletResponse())
            .setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    TestUtil.testCommonMethods(respBuilder);
    assertNotNull(respBuilder.build());
  }

  @Test
  public void testResponseBuilderRedirectNoUri() throws IOException {
    HttpResponse.Builder respBuilder = new ServletHttpResponseBuilder(
        ServletTestUtils.newHttpServletResponse())
            .setStatusCode(HttpStatus.SC_MOVED_TEMPORARILY);
    TestUtil.testCommonMethods(respBuilder);
    assertNotNull(respBuilder.build());
  }
}
