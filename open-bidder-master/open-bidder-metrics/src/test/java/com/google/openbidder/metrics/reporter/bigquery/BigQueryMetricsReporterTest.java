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

package com.google.openbidder.metrics.reporter.bigquery;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.bigquery.Bigquery;
import com.google.api.services.bigquery.model.TableSchema;
import com.google.openbidder.googlecompute.InstanceMetadata;
import com.google.openbidder.util.testing.FakeClock;

import com.codahale.metrics.MetricRegistry;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.concurrent.Executors;

/**
 * Tests for {@link BigQueryMetricsReporter}.
 */
public class BigQueryMetricsReporterTest {

  private static final String PROJECT_ID = "project_id";

  private Bigquery bigquery;
  private BigQueryMetricsReporter reporter;

  @Before
  public void setUp() {
    bigquery = Mockito.mock(Bigquery.class);
    InstanceMetadata metadata = Mockito.mock(InstanceMetadata.class);
    when(metadata.metadata("hostname")).thenReturn("localhost");
    when(metadata.metadata("zone")).thenReturn("us-central");
    reporter = new BigQueryMetricsReporter(
        new MetricRegistry(),
        Executors.newScheduledThreadPool(1),
        bigquery,
        new JacksonFactory(),
        new FakeClock(),
        metadata,
        PROJECT_ID,
        "uuid");
  }

  @Test
  public void testLoadingSchema() throws IOException {
    TableSchema schema = reporter.parseSchema();
    assertNotNull(schema.getFields());
    assertTrue(schema.getFields().size() > 0);
  }
}
