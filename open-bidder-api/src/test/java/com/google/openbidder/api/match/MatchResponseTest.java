/*
 * Copyright 2012 Google Inc. All Rights Reserved.
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

package com.google.openbidder.api.match;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import com.google.openbidder.api.platform.NoExchange;
import com.google.openbidder.api.testing.match.TestMatchResponseBuilder;
import com.google.openbidder.http.response.StandardHttpResponse;
import com.google.openbidder.http.util.HttpUtil;
import com.google.openbidder.util.testing.TestUtil;

import org.junit.Test;

import java.net.URI;

/**
 * Tests for {@link MatchResponse}.
 */
public class MatchResponseTest {

  @Test
  public void testCommonMethods() {
    MatchResponse resp = TestMatchResponseBuilder.create()
        .setRedirectUri("http://example.com/foo").build();
    TestUtil.testCommonMethods(resp);
    assertEquals("example.com", resp.getHostName());
    assertEquals("/foo", resp.getPath());
  }

  @Test
  public void testBuilder() {
    MatchResponse.Builder resp = MatchResponse.newBuilder()
        .setExchange(NoExchange.INSTANCE)
        .setHttpResponse(StandardHttpResponse.newBuilder());
    TestUtil.testCommonMethods(resp);
    assertNotNull(resp.build().toBuilder().build());
  }

  @Test
  public void test() {
    MatchResponse resp = TestMatchResponseBuilder.create().build();
    resp.setRedirectUri("http://pleasegothere");
    URI uri = HttpUtil.buildUri("http://pleasegothere");
    assertEquals(uri, resp.getRedirectUri());
    resp.setRedirectUri(uri);
    assertEquals(uri, resp.getRedirectUri());
    resp.putRedirectParameter("a", "b");
    assertEquals(ImmutableSet.of("b"), ImmutableSet.copyOf(resp.getRedirectParameters("a")));
    resp.removeRedirectParameter("a", "b");
    assertTrue(resp.getRedirectParameters("a").isEmpty());
  }
}
