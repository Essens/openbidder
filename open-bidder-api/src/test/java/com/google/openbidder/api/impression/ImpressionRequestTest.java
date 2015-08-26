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

package com.google.openbidder.api.impression;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.openbidder.api.platform.NoExchange;
import com.google.openbidder.api.testing.impression.TestImpressionRequestBuilder;
import com.google.openbidder.http.request.StandardHttpRequest;
import com.google.openbidder.util.testing.TestUtil;

import org.junit.Test;

/**
 * Tests for {@link ImpressionRequest}.
 */
public class ImpressionRequestTest {
  private static final String PRICE_NAME = "price";

  @Test
  public void testCommonMethods() {
    ImpressionRequest req = TestImpressionRequestBuilder.create().build();
    TestUtil.testCommonMethods(req);
    assertEquals("price", req.getPriceName());
  }

  @Test
  public void testBuilder() {
    ImpressionRequest.Builder req = ImpressionRequest.newBuilder()
        .setExchange(NoExchange.INSTANCE)
        .setHttpRequest(StandardHttpRequest.newBuilder().setUri("http://a.com").build())
        .setPriceName("cost");
    TestUtil.testCommonMethods(req);
    assertEquals("cost", req.getPriceName());
    assertNotNull(req.build().toBuilder().build());
  }

  public void testGetPrice_OK() {
    ImpressionRequest req = TestImpressionRequestBuilder.create()
        .setHttpRequest(StandardHttpRequest.newBuilder()
            .setUri("http://localhost")
            .setParameter(PRICE_NAME, "100")
            .setParameter("myprice", "55"))
        .build();
    assertTrue(req.hasPrice());
    assertEquals(PRICE_NAME, req.getPriceName());
    assertEquals(PRICE_NAME, req.toBuilder().getPriceName());
    assertEquals(100.0, req.getPriceValue(), 1e-9);
    req.getPriceValue(); // cached now
  }

  public void testGetPriceNamed_OK() {
    ImpressionRequest req = TestImpressionRequestBuilder.create()
        .setPriceName("myprice")
        .setHttpRequest(StandardHttpRequest.newBuilder()
            .setUri("http://localhost")
            .setParameter(PRICE_NAME, "55")
            .setParameter("myprice", "100"))
        .build();
    assertEquals(100.0, req.getPriceValue("myprice"), 1e-9);
  }

  @Test(expected = IllegalStateException.class)
  public void testGetPrice_missing() {
    TestImpressionRequestBuilder.create().build().getPriceValue();
  }

  @Test(expected = IllegalStateException.class)
  public void testGetPriceNamed_missing() {
    TestImpressionRequestBuilder.create().build().getPriceValue(PRICE_NAME);
  }
}
