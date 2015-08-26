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

package com.google.openbidder.api.impression;

import static org.junit.Assert.assertEquals;

import com.google.common.util.concurrent.Service.State;
import com.google.openbidder.api.testing.impression.CountingImpressionInterceptor;
import com.google.openbidder.api.testing.impression.ImpressionTestUtil;
import com.google.openbidder.api.testing.impression.TestImpressionRequestBuilder;
import com.google.openbidder.api.testing.impression.TestImpressionResponseBuilder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link ImpressionController}.
 */
public class ImpressionControllerTest {
  private ImpressionController controller;
  private CountingImpressionInterceptor countingInterceptor;

  @Before
  public void setUp() {
    countingInterceptor = new CountingImpressionInterceptor();
    controller = ImpressionTestUtil.newImpressionController(countingInterceptor);
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
    controller.onRequest(
        TestImpressionRequestBuilder.create().build(),
        TestImpressionResponseBuilder.create().build());
    assertEquals(1, countingInterceptor.postConstructCount);
    assertEquals(1, countingInterceptor.invokeCount);
  }
}
