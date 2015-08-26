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

package com.google.openbidder.netty.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.common.net.MediaType;
import com.google.openbidder.http.Protocol;
import com.google.openbidder.http.cookie.StandardCookie;
import com.google.openbidder.http.request.StandardHttpRequest;
import com.google.openbidder.http.util.HttpUtil;
import com.google.openbidder.util.testing.TestUtil;

import org.junit.Test;

import io.netty.buffer.PooledByteBufAllocator;

import java.io.IOException;

/**
 * Tests for {@link NettyHttpRequest}.
 */
public class NettyHttpRequestTest {

  @Test
  public void testRequest() throws IOException {
    NettyHttpRequest req = new NettyHttpRequest(
    StandardHttpRequest.newBuilder()
        .setUri("http://a.io:999?p1=v1")
        .addParameter("p2", "v2")
        .setMethod("GET")
        .setProtocol(Protocol.HTTP_1_1)
        .addHeader("h1", "header1")
        .addCookie(StandardCookie.create("name", "value"))
        .setMediaType(MediaType.JSON_UTF_8)
        .printContent("ping")
        .build(),
        new PooledByteBufAllocator());
    TestUtil.testCommonMethods(req);
    assertEquals(HttpUtil.buildUri("http://a.io:999"), req.getUri());
    assertEquals("GET", req.getMethod());
    assertEquals("header1", req.getHeader("h1"));
    assertEquals("v1", req.getParameter("p1"));
    assertEquals("v2", req.getParameter("p2"));
    assertEquals("value", req.getCookie("name").getValue());
    assertSame(MediaType.JSON_UTF_8, req.getMediaType());
    assertEquals("ping", req.contentReader().readLine());
    assertNotNull(req.netty());
    assertTrue(req.netty().content().release());
  }

  @Test
  public void testRequestEmpty() throws IOException {
    NettyHttpRequest req = new NettyHttpRequest(NettyHttpRequest.getNettyRequest(
        StandardHttpRequest.newBuilder().setUri("http://a.io:999").build(),
        new PooledByteBufAllocator()));
    TestUtil.testCommonMethods(req);
    assertEquals(HttpUtil.buildUri("http://a.io:999"), req.getUri());
    assertEquals("GET", req.getMethod());
    assertEquals(1, req.getHeaders().size());
    assertTrue(req.getParameters().isEmpty());
    assertTrue(req.getCookies().isEmpty());
    assertNull(req.getMediaType());
    assertNull(req.contentReader().readLine());
    assertNotNull(req.netty());
    assertTrue(req.netty().content().release(2));
  }
}
