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

package com.google.openbidder.util;

import static org.junit.Assert.assertFalse;

import com.google.openbidder.util.Clock;
import com.google.openbidder.util.SystemClock;

import org.joda.time.Instant;
import org.junit.Test;

/**
 * Tests for {@link Clock}.
 */
public class ClockTest {

  @Test
  public void testSystemClock() {
    Clock clock = new SystemClock();
    Instant now1 = Instant.now();
    Instant now2 = clock.now();
    assertFalse(now2.isBefore(now1));
    long nano1 = System.nanoTime();
    long nano2 = clock.nanoTime();
    assertFalse(nano2 < nano1);
  }
}
