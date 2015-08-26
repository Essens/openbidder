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

package com.google.openbidder.api.click;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.openbidder.api.platform.NoExchange;
import com.google.openbidder.api.testing.click.TestClickResponseBuilder;
import com.google.openbidder.http.response.StandardHttpResponse;
import com.google.openbidder.http.util.HttpUtil;
import com.google.openbidder.util.testing.TestUtil;

import org.junit.Test;

/**
 * Tests for {@link ClickResponse}.
 */
public class ClickResponseTest {

  @Test
  public void test() {
    ClickResponse resp = TestClickResponseBuilder.create().build();
    TestUtil.testCommonMethods(resp);
    resp.setRedirectLocation("http://a.com");
    assertEquals("http://a.com", resp.getRedirectLocation().toString());
    resp.setRedirectLocation(HttpUtil.buildUri("http://a.com"));
    assertEquals("http://a.com", resp.getRedirectLocation().toString());
  }

  @Test
  public void testBuilder() {
    ClickResponse.Builder resp = ClickResponse.newBuilder()
        .setExchange(NoExchange.INSTANCE)
        .setHttpResponse(StandardHttpResponse.newBuilder());
    TestUtil.testCommonMethods(resp);
    assertNotNull(resp.build().toBuilder().build());
  }
}
