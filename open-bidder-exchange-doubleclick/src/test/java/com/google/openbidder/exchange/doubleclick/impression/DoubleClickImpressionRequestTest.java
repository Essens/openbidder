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

package com.google.openbidder.exchange.doubleclick.impression;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.openbidder.config.impression.PriceName;
import com.google.openbidder.exchange.doubleclick.testing.DoubleClickTestUtil;
import com.google.openbidder.exchange.doubleclick.testing.TestImpressionRequestBuilder;
import com.google.openbidder.http.request.StandardHttpRequest;
import com.google.openbidder.util.testing.TestUtil;

import org.junit.Test;

/**
 * Tests for {@link DoubleClickImpressionRequest}.
 */
public class DoubleClickImpressionRequestTest {

  @Test
  public void testCommonMethods() {
    DoubleClickImpressionRequest req = TestImpressionRequestBuilder.create().build();
    TestUtil.testCommonMethods(req);
  }

  @Test
  public void testBuilder() {
    DoubleClickImpressionRequest.Builder req = DoubleClickImpressionRequest.newBuilder()
        .setHttpRequest(StandardHttpRequest.newBuilder().setUri("http://a.com").build())
        .setPriceCrypto(DoubleClickTestUtil.zeroPriceCrypto());
    TestUtil.testCommonMethods(req);
    assertSame(DoubleClickTestUtil.zeroPriceCrypto(), req.getPriceCrypto());
    assertNotNull(req.build().toBuilder().build());
  }

  @Test
  public void testGetPrice() {
    DoubleClickImpressionRequest req = TestImpressionRequestBuilder.create().setPrice(0.1).build();
    assertTrue(req.hasPrice());
    assertEquals(0.1, req.getPriceValue(), 1e-9);
    assertEquals(0.1, req.getPriceValue(), 1e-9); // cached
  }

  @Test
  public void testGetPriceNamed() {
    DoubleClickImpressionRequest req = TestImpressionRequestBuilder.create().setPrice(0.1).build();
    assertEquals(0.1, req.getPriceValue(PriceName.DEFAULT), 1e-9);
  }

  @Test(expected = IllegalStateException.class)
  public void testGetPrice_noPrice() {
    DoubleClickImpressionRequest req = TestImpressionRequestBuilder.create().build();
    assertFalse(req.hasPrice());
    req.getPriceValue();
  }

  @Test(expected = IllegalStateException.class)
  public void testGetPrice_noCrypto() {
    DoubleClickImpressionRequest req =
        TestImpressionRequestBuilder.create().setPrice(0.1).setPriceCrypto(null).build();
    assertTrue(req.hasPrice());
    req.getPriceValue();
  }
}
