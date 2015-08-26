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

package com.google.openbidder.http.receiver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import com.google.openbidder.http.HttpReceiverContext;
import com.google.openbidder.http.HttpRequest;
import com.google.openbidder.http.HttpResponse;
import com.google.openbidder.http.request.StandardHttpRequest;
import com.google.openbidder.http.response.StandardHttpResponse;

import org.junit.Test;

/**
 * Unit tests for this package.
 */
public class HttpReceiverTest {

  @Test
  public void testContext() {
    HttpRequest request = StandardHttpRequest.newBuilder().setUri("a.io").build();
    HttpResponse.Builder response = StandardHttpResponse.newBuilder();
    HttpReceiverContext context = new DefaultHttpReceiverContext(request, response);
    assertSame(request, context.httpRequest());
    assertSame(response, context.httpResponse());
    assertTrue(context.attributes().isEmpty());
    assertNotNull(context.toString());
  }

  @Test
  public void testOptions() {
    DefaultHttpReceiverContext context = createContext("OPTIONS");
    context.attributes().put("allowedMethods", ImmutableSet.of("GET"));
    new OptionsHttpReceiver().receive(context);
    assertEquals(ImmutableSet.of("GET"), context.httpResponse().getHeaders().get("Allow"));
  }

  protected DefaultHttpReceiverContext createContext(String method) {
    return new DefaultHttpReceiverContext(
        StandardHttpRequest.newBuilder().setUri("a.io").setMethod(method).build(),
        StandardHttpResponse.newBuilder());
  }
}
