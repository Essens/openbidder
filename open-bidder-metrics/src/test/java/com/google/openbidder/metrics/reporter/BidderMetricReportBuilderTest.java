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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.openbidder.util.Clock;
import com.google.openbidder.util.testing.FakeClock;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.UniformReservoir;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Tests for {@link BidderMetricReportBuilder}.
 */
public class BidderMetricReportBuilderTest {

  private static final Clock clock = new FakeClock();
  private BidderMetricReportBuilder builder;

  @Before
  public void setUp() {
    builder = new BidderMetricReportBuilder(clock, "test-hostname", "test-region");
  }

  @Test
  public void testMeterMapping() {
    MetricRegistry metricRegistry = new MetricRegistry();
    long startTime = clock.now().getMillis();
    Meter meter = metricRegistry.register(
        MetricRegistry.name(BidderMetricReportBuilder.class, "test-meter"), new Meter());
    Long numEvents = 3L;
    for (int i = 0; i < numEvents; i++) {
      meter.mark();
    }

    List<BidderMetricReportRow> rows = builder.build(metricRegistry);
    assertEquals(1, rows.size());
    BidderMetricReportRow row = rows.get(0);
    assertEquals("test-hostname", row.getBidderName());
    assertEquals("test-region", row.getRegion());
    assertTrue(row.getTimestamp() >= startTime);
    assertEquals("com.google.openbidder.metrics.reporter.BidderMetricReportBuilder.test-meter",
        row.getMetricName());
    assertEquals("meter", row.getMetricType());
    assertEquals(numEvents, row.getMeterCount());
    assertNotNull(row.getMeter1Minute());
    assertNotNull(row.getMeter5Minutes());
    assertNotNull(row.getMeter15Minutes());
    assertNotNull(row.getMeterMean());

    assertNull(row.getHistogramCount());
    assertNull(row.getHistogramMax());
  }

  @Test
  public void testHistogramMapping() {
    MetricRegistry metricRegistry = new MetricRegistry();
    long startTime = clock.now().getMillis();
    Histogram histogram = metricRegistry.register(
        MetricRegistry.name(BidderMetricReportBuilder.class, "test-histogram"),
        new Histogram(new UniformReservoir()));
    Long numEvents = 3L;
    for (int i = 0; i < numEvents; i++) {
      histogram.update(i);
    }

    List<BidderMetricReportRow> rows = builder.build(metricRegistry);
    assertEquals(1, rows.size());
    BidderMetricReportRow row = rows.get(0);
    assertEquals("test-hostname", row.getBidderName());
    assertEquals("test-region", row.getRegion());
    assertTrue(row.getTimestamp() >= startTime);
    assertEquals("com.google.openbidder.metrics.reporter.BidderMetricReportBuilder.test-histogram",
        row.getMetricName());
    assertEquals("histogram", row.getMetricType());
    assertEquals(numEvents, row.getHistogramCount());
    assertTrue(row.getHistogramMin() >= 0);
    assertTrue(row.getHistogramMedian() >= 0);
    assertTrue(row.getHistogramMax() >= 0);
    assertTrue(row.getHistogramStdDev() >= 0);
    assertTrue(row.getHistogramMedian() >= 0);
    assertTrue(row.getHistogram75thPercentile() >= 0);
    assertTrue(row.getHistogram95thPercentile() >= 0);
    assertTrue(row.getHistogram98thPercentile() >= 0);
    assertTrue(row.getHistogram99thPercentile() >= 0);
    assertTrue(row.getHistogram999thPercentile() >= 0);

    assertNull(row.getMeterCount());
    assertNull(row.getMeter1Minute());
  }

  @Test
  public void testTimerMapping() throws Exception {
    MetricRegistry metricRegistry = new MetricRegistry();
    long startTime = clock.now().getMillis();
    Timer timer = metricRegistry.register(
        MetricRegistry.name(BidderMetricReportBuilder.class, "test-timer"), new Timer());
    Long numEvents = 3L;
    for (int i = 0; i < numEvents; i++) {
      timer.update(i, TimeUnit.SECONDS);
    }

    List<BidderMetricReportRow> rows = builder.build(metricRegistry);
    assertEquals(1, rows.size());
    BidderMetricReportRow row = rows.get(0);
    assertEquals("test-hostname", row.getBidderName());
    assertEquals("test-region", row.getRegion());
    assertEquals("com.google.openbidder.metrics.reporter.BidderMetricReportBuilder.test-timer",
        row.getMetricName());
    assertTrue(row.getTimestamp() >= startTime);
    assertEquals(Long.valueOf(3), row.getTimerRateCount());
    assertTrue(row.getTimerDurationMin() >= 0);
    assertTrue(row.getTimerDurationMax() >= 0);
    assertTrue(row.getTimerDurationMean() >= 0);
    assertTrue(row.getTimerDurationStdDev() >= 0);
    assertTrue(row.getTimerDurationMedian() >= 0);
    assertTrue(row.getTimerDuration75thPercentile() >= 0);
    assertTrue(row.getTimerDuration95thPercentile() >= 0);
    assertTrue(row.getTimerDuration98thPercentile() >= 0);
    assertTrue(row.getTimerDuration99thPercentile() >= 0);
    assertTrue(row.getTimerDuration999thPercentile() >= 0);
    assertTrue(row.getTimerRate1Minute() >= 0);
    assertTrue(row.getTimerRate5Minutes() >= 0);
    assertTrue(row.getTimerRate15Minutes() >= 0);
    assertTrue(row.getTimerRateMean() >= 0);

    assertNull(row.getMeterCount());
    assertNull(row.getMeter1Minute());
    assertNull(row.getHistogramCount());
    assertNull(row.getHistogram95thPercentile());
  }

  @Test
  public void testCounter() throws Exception {
    MetricRegistry metricRegistry = new MetricRegistry();
    long startTime = clock.now().getMillis();
    Counter counter = metricRegistry.register(
        MetricRegistry.name(BidderMetricReportBuilder.class, "test-counter"), new Counter());
    Long count = 5L;
    counter.inc(count);

    List<BidderMetricReportRow> rows = builder.build(metricRegistry);
    assertEquals(1, rows.size());
    BidderMetricReportRow row = rows.get(0);
    assertEquals("test-hostname", row.getBidderName());
    assertEquals("test-region", row.getRegion());
    assertTrue(row.getTimestamp() >= startTime);
    assertEquals(count, row.getCounterCount());
    assertEquals("counter", row.getMetricType());
    assertEquals("com.google.openbidder.metrics.reporter.BidderMetricReportBuilder.test-counter",
        row.getMetricName());

    assertNull(row.getMeterCount());
    assertNull(row.getMeter1Minute());
    assertNull(row.getHistogramCount());
    assertNull(row.getHistogram95thPercentile());
  }
}
