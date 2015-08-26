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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import com.google.openbidder.config.http.Feature;
import com.google.openbidder.http.receiver.CounterReceiver;
import com.google.openbidder.http.receiver.DefaultHttpReceiverContext;
import com.google.openbidder.http.request.StandardHttpRequest;
import com.google.openbidder.http.response.StandardHttpResponse;

import org.junit.Test;

/**
 * Tests for {@link HttpRouter}.
 */
public class HttpRouterTest {

  @Test
  public void testRouter() {
    CounterReceiver receiver = new CounterReceiver();
    CounterReceiver optionsReceiver = new CounterReceiver();
    CounterReceiver methodNotAllowedReceiver = new CounterReceiver();
    CounterReceiver notFoundReceiver = new CounterReceiver();
    HttpRoute goodRoute = HttpRoute.get("route", "/foo", receiver, Feature.OTHER);
    HttpRoute badRoute = HttpRoute.get("route", "/bar", receiver, Feature.ADMIN);
    HttpRouter router = new HttpRouter(
        ImmutableSet.of(goodRoute, badRoute), ImmutableSet.of(Feature.OTHER),
        optionsReceiver, notFoundReceiver, methodNotAllowedReceiver);
    assertNotNull(router.toString());

    ImmutableSet<String> getAllowed = ImmutableSet.of("GET", "OPTIONS");
    assertEquals(getAllowed, router.getAllowedMethods("/foo"));
    assertTrue(router.getAllowedMethods("/bar").isEmpty());
    assertEquals(getAllowed, router.getAllowedMethods(PathMatcherType.buildMatcher("/foo")));
    assertTrue(router.getAllowedMethods(PathMatcherType.buildMatcher("/bar")).isEmpty());

    router.receive(new DefaultHttpReceiverContext(
        StandardHttpRequest.newBuilder().setUri("http://a.io/foo").build(),
        StandardHttpResponse.newBuilder()));
    assertEquals(1, receiver.counter);

    router.receive(new DefaultHttpReceiverContext(
        StandardHttpRequest.newBuilder().setUri("http://a.io/bar").build(),
        StandardHttpResponse.newBuilder()));
    assertEquals(1, notFoundReceiver.counter);

    router.receive(new DefaultHttpReceiverContext(
        StandardHttpRequest.newBuilder().setUri("http://a.io/foo").setMethod("OPTIONS").build(),
        StandardHttpResponse.newBuilder()));
    assertEquals(1, optionsReceiver.counter);

    router.receive(new DefaultHttpReceiverContext(
        StandardHttpRequest.newBuilder().setUri("http://a.io/foo").setMethod("POST").build(),
        StandardHttpResponse.newBuilder()));
    assertEquals(1, methodNotAllowedReceiver.counter);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDuplicateRoutes() {
    CounterReceiver receiver = new CounterReceiver();
    CounterReceiver optionsReceiver = new CounterReceiver();
    CounterReceiver methodNotAllowedReceiver = new CounterReceiver();
    CounterReceiver notFoundReceiver = new CounterReceiver();
    HttpRoute route1 = HttpRoute.get("route1", "/foo", receiver, Feature.OTHER);
    HttpRoute route2 = HttpRoute.get("route2", "/foo", receiver, Feature.OTHER);
    assertNotNull(new HttpRouter(
        ImmutableSet.of(route1, route2), ImmutableSet.of(Feature.OTHER),
        optionsReceiver, notFoundReceiver, methodNotAllowedReceiver));
  }
}
