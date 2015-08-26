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

import com.google.openbidder.api.testing.click.TestClickResponseBuilder;
import com.google.openbidder.util.testing.TestUtil;

import org.junit.Test;

/**
 * Tests for {@link ClickResponse}.
 */
public class ClickResponseTest {

  @Test
  public void test() {
    ClickResponse resp1 = TestClickResponseBuilder.create().build();
    ClickResponse.Builder resp1Builder = resp1.toBuilder();
    TestUtil.testCommonMethods(resp1);
    TestUtil.testCommonMethods(resp1Builder);
  }
}
