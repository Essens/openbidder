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

package com.google.openbidder.impression;

import com.google.openbidder.api.impression.ImpressionController;
import com.google.openbidder.api.testing.impression.ImpressionTestUtil;
import com.google.openbidder.api.testing.impression.TestImpressionRequestBuilder;
import com.google.openbidder.api.testing.impression.TestImpressionResponseBuilder;
import com.google.openbidder.impression.interceptor.SimpleImpressionInterceptor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link SimpleImpressionInterceptor}.
 */
public class SimpleImpressionInterceptorTest {
  private ImpressionController controller;

  @Before
  public void setUp() {
    controller = ImpressionTestUtil.newImpressionController(new SimpleImpressionInterceptor());
  }

  @After
  public void tearDown() {
    if (controller != null) {
      controller.stopAsync().awaitTerminated();
    }
  }

  @Test
  public void testInterceptor() {
    controller.onRequest(
        TestImpressionRequestBuilder.create().build(),
        TestImpressionResponseBuilder.create().build());
    controller.onRequest(
        TestImpressionRequestBuilder.create().setPrice(1.2).build(),
        TestImpressionResponseBuilder.create().build());
  }
}
