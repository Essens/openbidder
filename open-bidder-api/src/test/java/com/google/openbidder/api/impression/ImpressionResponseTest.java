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

import static org.junit.Assert.assertNotNull;

import com.google.openbidder.api.platform.NoExchange;
import com.google.openbidder.api.testing.impression.TestImpressionResponseBuilder;
import com.google.openbidder.http.response.StandardHttpResponse;
import com.google.openbidder.util.testing.TestUtil;

import org.junit.Test;

/**
 * Tests for {@link ImpressionResponse}.
 */
public class ImpressionResponseTest {

  @Test
  public void test() {
    ImpressionResponse resp = TestImpressionResponseBuilder.create().build();
    TestUtil.testCommonMethods(resp);
  }

  @Test
  public void testBuilder() {
    ImpressionResponse.Builder resp = ImpressionResponse.newBuilder()
        .setExchange(NoExchange.INSTANCE)
        .setHttpResponse(StandardHttpResponse.newBuilder());
    TestUtil.testCommonMethods(resp);
    assertNotNull(resp.build().toBuilder().build());
  }
}
