/*
 * Copyright 2012 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.openbidder.metrics.reporter;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Similar to {@link ScheduledReporter}, reports are scheduled at a fixed rate.
 */
public abstract class FixedRateAbstractPollingReporter implements Runnable {

  private final MetricRegistry metricRegistry;
  private final ScheduledExecutorService executor;

  protected FixedRateAbstractPollingReporter(
      MetricRegistry metricRegistry,
      ScheduledExecutorService executor) {
    this.metricRegistry = metricRegistry;
    this.executor = executor;
  }

  public MetricRegistry getMetricRegistry() {
    return metricRegistry;
  }

  /**
   * Starts the reporter.
   */
  public void start(long period, TimeUnit unit) {
    executor.scheduleAtFixedRate(this, period, period, unit);
  }

  /**
   * Shuts down the reporter, waiting for full termination.
   */
  public void shutdownAndWait(long timeout, TimeUnit unit) throws InterruptedException {
    shutdown();
    executor.awaitTermination(timeout, unit);
  }

  public void shutdown() {
    executor.shutdown();
  }
}
