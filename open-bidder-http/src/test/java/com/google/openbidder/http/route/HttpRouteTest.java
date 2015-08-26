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

package com.google.openbidder.http.route;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import com.google.openbidder.config.http.Feature;
import com.google.openbidder.http.receiver.CounterReceiver;
import com.google.openbidder.util.testing.TestUtil;

import org.junit.Test;

/**
 * Tests for {@link HttpRoute}.
 */
public class HttpRouteTest {

  @Test
  public void testRoute() {
    CounterReceiver receiver = new CounterReceiver();
    HttpRoute route1 = HttpRoute.get("route", "/foo", receiver, Feature.OTHER);
    HttpRoute route2 = HttpRoute.get("route", "/foo", receiver, Feature.OTHER);
    HttpRoute route3 = HttpRoute.get("route", "/foo", receiver, Feature.ADMIN);
    TestUtil.testCommonMethods(route1, route2, route3);

    assertEquals("route", route1.getName());
    assertEquals(HttpRoute.GET, route1.getMethods());
    assertEquals("/foo", route1.getPathMatcher().getPathSpec());
    assertEquals("/foo", route1.getPathSpec());
    assertEquals(ImmutableSet.of(Feature.OTHER), route1.getRequiredFeatures());
    assertTrue(route1.hasRequiredFeatures(ImmutableSet.of(Feature.OTHER)));
    assertFalse(route1.hasRequiredFeatures(ImmutableSet.of(Feature.ADMIN)));
    assertSame(receiver, route1.getHttpReceiver());

    assertEquals(HttpRoute.POST, HttpRoute.post("r", "/", receiver, Feature.OTHER).getMethods());
    assertEquals("HttpRoute", HttpRoute.get(null, "/", receiver, Feature.OTHER).getName());
  }
}
