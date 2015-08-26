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

package com.google.openbidder.api.interceptor;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.platform.NoExchange;
import com.google.openbidder.api.testing.bidding.TestBidRequestBuilder;
import com.google.openbidder.http.Cookie;
import com.google.openbidder.http.cookie.StandardCookie;
import com.google.openbidder.http.request.StandardHttpRequest;
import com.google.openbidder.util.testing.TestUtil;

import org.junit.Test;

import java.util.Arrays;

/**
 * Tests for {@link UserRequest} and {@link Cookie}.
 */
public class UserRequestTest {

  private static final String UNSECURE_URI = "http://example.com";

  @Test
  public void testCommonMethods() {
    Cookie cookie1 = StandardCookie.create("name", "value1");
    UserRequest req = unsecureRequest(ImmutableMultimap.of("k", "v"), cookie1);
    TestUtil.testCommonMethods(req);
    assertNotNull(req.httpRequest());
    assertSame(NoExchange.INSTANCE, req.getExchange());
  }

  private static BidRequest unsecureRequest(
      Multimap<String, String> parameters,
      Cookie... cookies) {

    return TestBidRequestBuilder.create().setHttpRequest(StandardHttpRequest.newBuilder()
            .setMethod("GET")
            .setUri(UNSECURE_URI)
            .setAllParameter(parameters)
            .setAllCookie(Arrays.asList(cookies)))
        .build();
  }
}
