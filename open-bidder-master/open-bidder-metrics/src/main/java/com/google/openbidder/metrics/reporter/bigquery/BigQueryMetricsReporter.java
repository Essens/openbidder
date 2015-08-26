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

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.bigquery.Bigquery;
import com.google.api.services.bigquery.model.Dataset;
import com.google.api.services.bigquery.model.DatasetReference;
import com.google.api.services.bigquery.model.Job;
import com.google.api.services.bigquery.model.JobConfiguration;
import com.google.api.services.bigquery.model.JobConfigurationLoad;
import com.google.api.services.bigquery.model.JobStatus;
import com.google.api.services.bigquery.model.TableReference;
import com.google.api.services.bigquery.model.TableSchema;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.net.MediaType;
import com.google.openbidder.googlecompute.InstanceMetadata;
import com.google.openbidder.metrics.reporter.BidderMetricReportBuilder;
import com.google.openbidder.metrics.reporter.BidderMetricReportRow;
import com.google.openbidder.metrics.reporter.FixedRateAbstractPollingReporter;
import com.google.openbidder.util.Clock;

import com.codahale.metrics.MetricRegistry;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A metrics reporter that logs metrics to Google BigQuery.
 */
public class BigQueryMetricsReporter extends FixedRateAbstractPollingReporter {

  private static final Logger logger = LoggerFactory.getLogger(BigQueryMetricsReporter.class);

  private static final int STATUS_INTERVAL_MS = 30000;
  private static final int STATUS_CHECKS = 4;

  private static final String SCHEMA_RESOURCE = "/bigquery-bidder-schema.json";
  private static final String TABLE_TEMPLATE = "bidder_metrics_%s";

  private final Clock clock;
  private final String apiProjectId;
  private final Bigquery bigquery;
  private final JsonFactory jsonFactory;
  private final String datasetName;
  private final BidderMetricReportBuilder rowBuilder;

  private TableSchema tableSchema;

  public BigQueryMetricsReporter(
      MetricRegistry metricRegistry,
      ScheduledExecutorService executor,
      Bigquery bigquery,
      JsonFactory jsonFactory,
      Clock clock,
      InstanceMetadata metadata,
      String apiProjectId,
      String datasetName) {
    super(metricRegistry, executor);

    this.clock = clock;
    this.apiProjectId = apiProjectId;
    this.jsonFactory = jsonFactory;
    this.bigquery = bigquery;
    this.datasetName = datasetName;
    this.rowBuilder = new BidderMetricReportBuilder(
        clock, metadata.metadata("hostname"),
        InstanceMetadata.resourceShortName(metadata.metadata("zone")));
  }

  @Override
  public void start(long period, TimeUnit unit) {
    super.start(period, unit);

    try {
      this.tableSchema = parseSchema();
    } catch (IOException e) {
      throw new IllegalStateException("Error reading table schema file: " + SCHEMA_RESOURCE);
    }

    // Ensure the dataset exists or create it if not
    try {
      try {
        bigquery.datasets().get(apiProjectId, datasetName).execute();
        logger.info("Initialized for existing dataset: {}", datasetName);
      } catch (HttpResponseException e) {
        if (e.getStatusCode() == HttpStatusCodes.STATUS_CODE_NOT_FOUND) {
          logger.info("Dataset does not exist, creating: {}", datasetName);
          bigquery.datasets().insert(apiProjectId, new Dataset()
              .setDescription(String.format("Created on %s", clock.now().toDateTime().toString()))
              .setDatasetReference(new DatasetReference()
                .setProjectId(apiProjectId)
                .setDatasetId(datasetName))).execute();
        } else {
          throw e;
        }
      }
    } catch (IOException e) {
      throw new IllegalStateException(
          "Error creating/inspecting BigQuery dataset: " + datasetName, e);
    }
  }

  /**
   * An iteration of the reporter.
   */
  @Override
  public void run() {
    String tableName = tableName(clock.now().toDateTime(DateTimeZone.UTC));
    Job job = makeJob(tableName);

    if (logger.isDebugEnabled()) {
      logger.debug("Building metric report {}:{}", datasetName, tableName);
    }
    List<BidderMetricReportRow> metrics = rowBuilder.build(this.getMetricRegistry());
    if (logger.isDebugEnabled()) {
      logger.debug("Writing {} metric rows to {}:{}",
          metrics.size(), datasetName, tableName);
    }

    try {
      ByteArrayContent content = new ByteArrayContent(MediaType.OCTET_STREAM.toString(),
          serializeRows(metrics).getBytes(Charsets.UTF_8));
      if (logger.isDebugEnabled()) {
        logger.debug("Creating BigQuery insertion job");
      }
      Job runningJob = bigquery.jobs().insert(apiProjectId, job, content).execute();
      if (logger.isDebugEnabled()) {
        logger.debug("Insertion job {} running", runningJob.getJobReference().getJobId());
      }

      // Wait some time so we can log if an error occurs.
      boolean done = false;

      for (int numChecks = 0; numChecks < STATUS_CHECKS && !Thread.currentThread().isInterrupted();
          numChecks++) {
        Job jobStatus = bigquery.jobs().get(
            apiProjectId, runningJob.getJobReference().getJobId()).execute();
        JobStatus status = jobStatus.getStatus();

        if ("DONE".equals(status.getState())) {
          if (status.getErrors() != null && !status.getErrors().isEmpty()) {
            logger.error("Error loading metric report {}:{} - {}",
                datasetName, tableName, status.getErrors().get(0).getMessage());
          } else {
            logger.info("Success loading metric report {}:{}", datasetName, tableName);
          }

          done = true;
          break;
        }

        try {
          Thread.sleep(STATUS_INTERVAL_MS);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }

      if (!done) { // Either exceeded STATUS_CHECKS, or got interrupted
        logger.warn("Job is still running after {}s.  Not checking status any longer for {}:{}",
            (STATUS_CHECKS * STATUS_INTERVAL_MS) / 1000, datasetName, tableName);
      }
    } catch (IOException e) {
      logger.warn("Error creating or executing metric load job", e);
    }
  }

  @VisibleForTesting
  protected TableSchema parseSchema() throws IOException {
    return jsonFactory.fromInputStream(
        BigQueryMetricsReporter.class.getResourceAsStream(SCHEMA_RESOURCE),
        TableSchema.class);
  }

  private String serializeRows(List<BidderMetricReportRow> rows) {
    return Joiner.on("\n").join(
        Iterables.transform(rows, new Function<BidderMetricReportRow, String>() {
          @Override
          public String apply(BidderMetricReportRow row) {
            try {
              return jsonFactory.toString(row);
            } catch (IOException e) {
              throw new IllegalStateException(
                  "Error serializing bidder metric report row: " + row, e);
            }
          }})
       );
  }

  private Job makeJob(String tableName) {
    JobConfigurationLoad jobConfiguration = new JobConfigurationLoad();
    jobConfiguration.setWriteDisposition("WRITE_APPEND");
    jobConfiguration.setCreateDisposition("CREATE_IF_NEEDED");
    jobConfiguration.setDestinationTable(new TableReference()
      .setDatasetId(datasetName)
      .setProjectId(apiProjectId)
      .setTableId(tableName));
    jobConfiguration.setSchema(tableSchema);
    jobConfiguration.setSourceFormat("NEWLINE_DELIMITED_JSON");

    Job job = new Job();
    job.setConfiguration(new JobConfiguration().setLoad(jobConfiguration));

    return job;
  }

  private String tableName(DateTime dateTime) {
    return String.format(TABLE_TEMPLATE, dateTime.toString("YYYY_MM"));
  }
}
