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

package com.google.openbidder.exchange.doubleclick.match;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.openbidder.exchange.doubleclick.testing.TestMatchRequestBuilder;
import com.google.openbidder.http.request.StandardHttpRequest;
import com.google.openbidder.util.testing.TestUtil;

import org.junit.Test;

/**
 * Tests for {@link DoubleClickMatchRequest}.
 */
public class DoubleClickMatchRequestTest {
  static final String GOOGLE_GID = "dGhpcyBpcyBhbiBleGFtGxl";
  static final long VERSION = 112;
  static final String MATCH_REQUEST_PUSH =
      "http://ad.network.com/pixel?google_gid=dGhpcyBpcyBhbiBleGFtGxl&"
      + "google_cver=" + VERSION + "&google_push=push_data";
  static final String MATCH_REQUEST_COOKIE =
      "http://ad.network.com/pixel?google_gid=dGhpcyBpcyBhbiBleGFtGxl&"
      + "google_cver=" + VERSION + "&foo=bar";

  @Test
  public void testCommonMethods() {
    DoubleClickMatchRequest req1 = TestMatchRequestBuilder.create().build();
    DoubleClickMatchRequest.Builder req1Builder = req1.toBuilder();
    TestUtil.testCommonMethods(req1);
    TestUtil.testCommonMethods(req1Builder);
  }

  @Test
  public void constructor_push_parametersSet() {
    DoubleClickMatchRequest matchRequest = TestMatchRequestBuilder.create()
        .setHttpRequest(StandardHttpRequest.newBuilder().setUri(MATCH_REQUEST_PUSH))
        .build();
    assertEquals(GOOGLE_GID, matchRequest.getUserId());
    assertEquals(Long.valueOf(VERSION), matchRequest.getCookieVersion());
    assertEquals("push_data", matchRequest.getPushData());
    assertTrue(matchRequest.isPush());
  }

  public void constructor_nonPush_parametersSet() {
    DoubleClickMatchRequest matchRequest = TestMatchRequestBuilder.create()
        .setHttpRequest(StandardHttpRequest.newBuilder().setUri(MATCH_REQUEST_COOKIE))
        .build();
    assertEquals(GOOGLE_GID, matchRequest.getUserId());
    assertEquals(Long.valueOf(VERSION), matchRequest.getCookieVersion());
    assertNull(matchRequest.getPushData());
    assertEquals("bar", matchRequest.httpRequest().getParameter("foo"));
    assertNull(matchRequest.httpRequest().getParameter("foo2"));
    assertFalse(matchRequest.isPush());
  }
}
