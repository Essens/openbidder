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

package com.google.openbidder.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.openbidder.http.cookie.AbstractCookie;
import com.google.openbidder.http.cookie.CookieOrBuilder;
import com.google.openbidder.http.cookie.StandardCookie;
import com.google.openbidder.http.util.HttpUtil;
import com.google.openbidder.util.testing.TestUtil;

import org.junit.Test;

/**
 * Tests for {@link Cookie}.
 */
public class CookieTest {

  @Test
  public void testCommonMethods() {
    Cookie.Builder builder = StandardCookie.newBuilder()
        .setName("name")
        .setValue("value")
        .setDomain("domain")
        .setPath("path")
        .setSecure(false)
        .setMaxAge(1);
    Cookie cookie1 = HttpUtil.built(HttpUtil.built(builder));
    Cookie cookie2 = HttpUtil.builder(HttpUtil.builder(cookie1)).build();
    Cookie cookie3 = cookie1.toBuilder().setName("name2").build();

    TestUtil.testCommonMethods(cookie1, cookie2, cookie3);
    assertCookieOrBuilder(cookie1);
    assertCookieOrBuilder(builder);
  }

  void assertCookieOrBuilder(CookieOrBuilder cob) {
    cob.toString();
    assertEquals("name", cob.getName());
    assertEquals("value", cob.getValue());
    assertEquals("domain", cob.getDomain());
    assertEquals("path", cob.getPath());
    assertEquals(false, cob.isSecure());
    assertEquals(1, cob.getMaxAge());
  }

  @Test
  public void testAbstractCookie() {
    Cookie cookie = new AbstractCookieTester();
    assertNotNull(cookie.toBuilder().build());
  }

  static class AbstractCookieTester extends AbstractCookie {
    @Override public String getName() { return "name"; }
    @Override public String getValue() { return "value"; }
    @Override public String getDomain() { return "domain"; }
    @Override public String getPath() { return "path"; }
    @Override public boolean isSecure() { return false; }
    @Override public int getMaxAge() { return 1; }
  }
}
