/*
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless respuired by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.openbidder.netty.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.common.net.MediaType;
import com.google.openbidder.http.HttpResponse;
import com.google.openbidder.http.cookie.StandardCookie;
import com.google.openbidder.http.response.StandardHttpResponse;
import com.google.openbidder.http.util.HttpUtil;
import com.google.openbidder.util.testing.TestUtil;

import org.apache.http.HttpStatus;
import org.junit.Test;

import io.netty.buffer.PooledByteBufAllocator;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.io.IOException;

/**
 * Tests for {@link NettyHttpResponse}.
 */
public class NettyHttpResponseTest {

  @Test
  public void testResponseRedirect() throws IOException {
    NettyHttpResponse resp = new NettyHttpResponse(
    StandardHttpResponse.newBuilder()
        .setRedirectUri("http://a.io:999?p1=v1")
        .addRedirectParameter("p2", "v2")
        .setStatusCode(HttpStatus.SC_MOVED_TEMPORARILY)
        .build(),
        new PooledByteBufAllocator());
    TestUtil.testCommonMethods(resp);
    assertEquals(
        HttpUtil.buildUri("http://a.io:999?p1=v1&p2=v2").toString().length(),
        resp.getRedirectUri().toString().length());
    assertTrue(resp.getRedirectParameters().isEmpty());
    assertTrue(resp.hasRedirectUri());
    assertEquals(1, resp.getHeaders().size());
    assertTrue(resp.getCookies().isEmpty());
    assertNull(resp.getMediaType());
    assertNotNull(resp.netty());
    assertNull(resp.contentReader().readLine());
    assertTrue(resp.netty().content().release());
  }

  @Test
  public void testResponseRegular() throws IOException {
    NettyHttpResponse resp = new NettyHttpResponse(NettyHttpResponse.getNettyResponse(
    StandardHttpResponse.newBuilder()
        .addHeader("h1", "header1")
        .addCookie(StandardCookie.create("name", "value"))
        .setMediaType(MediaType.JSON_UTF_8)
        .printContent("pong")
        .build(),
        new PooledByteBufAllocator()));
    TestUtil.testCommonMethods(resp);
    assertNull(resp.getRedirectUri());
    assertTrue(resp.getRedirectParameters().isEmpty());
    assertFalse(resp.hasRedirectUri());
    assertEquals("header1", resp.getHeader("h1"));
    assertEquals("value", resp.getCookie("name").getValue());
    assertSame(MediaType.JSON_UTF_8, resp.getMediaType());
    assertEquals("pong", resp.contentReader().readLine());
    assertNotNull(resp.netty());
    assertTrue(resp.netty().content().release(2));
  }

  @Test
  public void testResponseEmpty() throws IOException {
    NettyHttpResponse resp = new NettyHttpResponse(
    StandardHttpResponse.newBuilder().build(), new PooledByteBufAllocator());
    TestUtil.testCommonMethods(resp);
    assertEquals(1, resp.getHeaders().size());
    assertTrue(resp.getRedirectParameters().isEmpty());
    assertTrue(resp.getCookies().isEmpty());
    assertNull(resp.getMediaType());
    assertNotNull(resp.netty());
    assertNull(resp.contentReader().readLine());
    assertTrue(resp.netty().content().release());
  }

  @Test
  public void testResponseBuilderRedirect() {
    HttpResponse.Builder respBuilder = new NettyHttpResponseBuilder(new PooledByteBufAllocator())
        .setRedirectUri("http://a.io:999?p1=v1")
        .addRedirectParameter("p2", "v2")
        .setStatusCode(HttpStatus.SC_MOVED_TEMPORARILY);
    TestUtil.testCommonMethods(respBuilder);
    assertEquals(
        HttpUtil.buildUri("http://a.io:999?p1=v1&p2=v2").toString().length(),
        respBuilder.getRedirectUri().toString().length());
    assertEquals("v1", respBuilder.getRedirectParameter("p1"));
    assertEquals("v2", respBuilder.getRedirectParameter("p2"));
    assertTrue(respBuilder.hasRedirectUri());
    assertTrue(respBuilder.getHeaders().isEmpty());
    assertTrue(respBuilder.getCookies().isEmpty());
    assertNull(respBuilder.getMediaType());
    assertNotNull(((NettyHttpResponseBuilder) respBuilder).netty());
    NettyHttpResponse resp = (NettyHttpResponse) respBuilder.build();
    assertTrue(resp.netty().content().release());
  }

  @Test
  public void testResponseBuilderRegular() {
    HttpResponse.Builder respBuilder = new NettyHttpResponseBuilder(new PooledByteBufAllocator())
        .addHeader("h1", "header1")
        .addCookie(StandardCookie.create("name", "value"))
        .setMediaType(MediaType.JSON_UTF_8)
        .printContent("pong");
    TestUtil.testCommonMethods(respBuilder);
    assertNull(respBuilder.getRedirectUri());
    assertTrue(respBuilder.getRedirectParameters().isEmpty());
    assertFalse(respBuilder.hasRedirectUri());
    assertEquals(2, respBuilder.getHeaders().size());
    assertEquals(1, respBuilder.getCookies().size());
    assertSame(MediaType.JSON_UTF_8, respBuilder.getMediaType());
    assertNotNull("pong", respBuilder.contentWriter());
    assertNotNull(((NettyHttpResponseBuilder) respBuilder).netty());
    NettyHttpResponse resp = (NettyHttpResponse) respBuilder.build();
    assertTrue(resp.netty().content().release());
  }

  @Test
  public void testResponseBuilderEmpty() throws IOException {
    HttpResponse.Builder respBuilder = new NettyHttpResponseBuilder(new DefaultFullHttpResponse(
        HttpVersion.HTTP_1_1,
        HttpResponseStatus.OK,
        new PooledByteBufAllocator().buffer()));
    TestUtil.testCommonMethods(respBuilder);
    assertTrue(respBuilder.getHeaders().isEmpty());
    assertTrue(respBuilder.getRedirectParameters().isEmpty());
    assertTrue(respBuilder.getCookies().isEmpty());
    assertNull(respBuilder.getMediaType());
    assertNotNull(((NettyHttpResponseBuilder) respBuilder).netty());
    NettyHttpResponse resp = (NettyHttpResponse) respBuilder.build();
    assertNull(resp.contentReader().readLine());
    assertTrue(resp.netty().content().release(2));
  }
}
