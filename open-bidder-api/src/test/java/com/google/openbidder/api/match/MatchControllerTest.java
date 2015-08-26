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

import com.google.common.util.concurrent.Service.State;
import com.google.openbidder.api.testing.match.CountingMatchInterceptor;
import com.google.openbidder.api.testing.match.MatchTestUtil;
import com.google.openbidder.api.testing.match.TestMatchRequestBuilder;
import com.google.openbidder.api.testing.match.TestMatchResponseBuilder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link MatchController}.
 */
public class MatchControllerTest {
  private MatchController controller;
  private CountingMatchInterceptor countingInterceptor;

  @Before
  public void setUp() {
    countingInterceptor = new CountingMatchInterceptor();
    controller = MatchTestUtil.newMatchController(countingInterceptor);
  }

  @After
  public void tearDown() {
    controller.stopAsync().awaitTerminated();
    assertEquals(State.TERMINATED, controller.state());
    controller.stopAsync().awaitTerminated();
    assertEquals(State.TERMINATED, controller.state());
    assertEquals(1, countingInterceptor.preDestroyCount);
  }

  @Test
  public void testImpression() {
    MatchRequest request = TestMatchRequestBuilder.create().build();
    MatchResponse response = TestMatchResponseBuilder.create().build();

    controller.onRequest(request, response);
    assertEquals(1, countingInterceptor.postConstructCount);
    assertEquals(1, countingInterceptor.invokeCount);
  }
}
