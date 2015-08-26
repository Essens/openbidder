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

import com.google.openbidder.http.Cookie;
import com.google.openbidder.util.testing.TestUtil;

import org.junit.Test;

/**
 * Tests for {@link ServletCookie}.
 */
public class ServletCookieTest {

  @Test
  public void testCommonMethods() {
    Cookie cookie1 = new ServletCookie(newServletCookie(1));
    Cookie cookie2 = new ServletCookie(newServletCookie(1));
    Cookie cookie3 = new ServletCookie(newServletCookie(3));

    TestUtil.testCommonMethods(cookie1, cookie2, cookie3);

    assertEquals("name", cookie1.getName());
    assertEquals("value", cookie1.getValue());
    assertEquals("domain", cookie1.getDomain());
    assertEquals("path", cookie1.getPath());
    assertEquals(false, cookie1.isSecure());
    assertEquals(1, cookie1.getMaxAge());
    assertNotNull(cookie1.toBuilder().build());
  }

  private static javax.servlet.http.Cookie newServletCookie(int maxAge) {
    javax.servlet.http.Cookie servletCookie = new javax.servlet.http.Cookie("name", "value");
    servletCookie.setDomain("domain");
    servletCookie.setPath("path");
    servletCookie.setSecure(false);
    servletCookie.setMaxAge(maxAge);
    servletCookie.setVersion(1);
    return servletCookie;
  }
}
