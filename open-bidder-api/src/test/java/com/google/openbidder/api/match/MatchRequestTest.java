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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.google.openbidder.api.platform.NoExchange;
import com.google.openbidder.api.testing.match.TestMatchRequestBuilder;
import com.google.openbidder.http.request.StandardHttpRequest;
import com.google.openbidder.util.testing.TestUtil;

import org.junit.Test;

/**
 * Tests for {@link MatchRequest}.
 */
public class MatchRequestTest {

  @Test
  public void testCommonMethods() {
    MatchRequest req = TestMatchRequestBuilder.create().build();
    TestUtil.testCommonMethods(req);
    assertNull(req.getUserId());
    assertNull(req.getUserId());
  }

  @Test
  public void testBuilder() {
    MatchRequest.Builder req = MatchRequest.newBuilder()
        .setExchange(NoExchange.INSTANCE)
        .setHttpRequest(StandardHttpRequest.newBuilder().setUri("http://a.com").build());
    TestUtil.testCommonMethods(req);
    assertNotNull(req.build().toBuilder().build());
  }
}
