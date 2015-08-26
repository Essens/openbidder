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

import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.Strings;
import com.google.api.services.bigquery.Bigquery;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.openbidder.config.googleapi.ApiProjectId;
import com.google.openbidder.googlecompute.InstanceMetadata;
import com.google.openbidder.metrics.reporter.DatasetName;
import com.google.openbidder.util.Clock;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.codahale.metrics.MetricRegistry;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Binds {@link Bigquery} client.
 */
@Parameters(separators = "=")
public class BigQueryMetricsReporterModule extends AbstractModule {
  @Parameter(names = "--metrics_reporter_dataset",
      description = "Name of dataset to store metrics")
  private String datasetName;

  @Override
  protected void configure() {
    if (!Strings.isNullOrEmpty(datasetName)) {
      bind(String.class).annotatedWith(DatasetName.class).toInstance(datasetName);
      bind(BigQueryMetricsReporter.class)
          .toProvider(BigQueryMetricsReporterProvider.class).in(Scopes.SINGLETON);
    }
  }

  public static class BigQueryMetricsReporterProvider implements Provider<BigQueryMetricsReporter> {
    private final MetricRegistry metricsRegistry;
    private final Bigquery bigquery;
    private final JsonFactory jsonFactory;
    private final Clock clock;
    private final InstanceMetadata metadata;
    private final @ApiProjectId String apiProjectId;
    private final @DatasetName String datasetName;

    @Inject
    public BigQueryMetricsReporterProvider(
        MetricRegistry metricsRegistry,
        Bigquery bigquery,
        JsonFactory jsonFactory,
        Clock clock,
        InstanceMetadata metadata,
        @ApiProjectId String apiProjectId,
        @DatasetName String datasetName) {
      this.metricsRegistry = metricsRegistry;
      this.bigquery = bigquery;
      this.jsonFactory = jsonFactory;
      this.clock = clock;
      this.metadata = metadata;
      this.apiProjectId = apiProjectId;
      this.datasetName = datasetName;
    }

    @Override
    public BigQueryMetricsReporter get() {
      BigQueryMetricsReporter reporter = new BigQueryMetricsReporter(
          metricsRegistry,
          Executors.newScheduledThreadPool(1),
          bigquery,
          jsonFactory,
          clock,
          metadata,
          apiProjectId,
          datasetName);
      reporter.start(300, TimeUnit.SECONDS);
      return reporter;
    }
  }
}
