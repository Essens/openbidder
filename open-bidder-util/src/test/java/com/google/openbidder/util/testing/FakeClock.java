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

package com.google.openbidder.util.testing;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.openbidder.util.Clock;

import org.joda.time.Duration;
import org.joda.time.Instant;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Mock implementation of {@link Clock} that allows the test to explicitly set the time.
 * The time can either be frozen or progress in a predictable way.
 */
@javax.annotation.ParametersAreNonnullByDefault
public class FakeClock implements Clock {
  private Instant now;
  private Duration stepPerCall;
  private final List<Listener> listeners = new CopyOnWriteArrayList<>();

  public FakeClock() {
    this.now = Instant.now();
    this.stepPerCall = Duration.millis(1);
  }

  public FakeClock(Instant now, Duration stepPerCall) {
    this.now = now;
    this.stepPerCall = stepPerCall;
  }

  public void addListener(Listener listener) {
    listeners.add(listener);
  }

  public void removeListener(Listener listener) {
    listeners.remove(listener);
  }

  @Override
  public Instant now() {
    tick();
    return now;
  }

  @Override
  public long nanoTime() {
    return now().getMillis() * 1000000L;
  }

  public void tick() {
    Instant ret = checkNotNull(now);
    now = ret.plus(stepPerCall);

    for (Listener listener : listeners) {
      listener.tick();
    }
  }

  public Instant lastNow() {
    return checkNotNull(now);
  }

  public void setNow(Instant now) {
    this.now = now;
  }

  public void setNow(long now) {
    this.now = new Instant(now);
  }

  public final Duration getStepPerCall() {
    return stepPerCall;
  }

  public final void setStepPerCall(Duration stepPerCall) {
    this.stepPerCall = stepPerCall;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("now", now)
        .add("stepperCall", stepPerCall)
        .toString();
  }

  /**
   * Allows listening to each clock tick.
   */
  public static interface Listener {

    /**
     * Ticking away the moments that make up a dull day...
     * <br>this will be invoked every time the clock ticks.
     */
    void tick();
  }
}
