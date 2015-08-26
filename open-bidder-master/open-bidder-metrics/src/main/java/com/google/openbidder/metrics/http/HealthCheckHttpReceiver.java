/*
 * Copyright 2013 Google Inc. All Rights Reserved.
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

package com.google.openbidder.metrics.http;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.net.MediaType;
import com.google.openbidder.http.HttpReceiver;
import com.google.openbidder.http.HttpReceiverContext;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;

import org.apache.http.HttpStatus;

import java.io.PrintWriter;
import java.util.Map;

import javax.inject.Inject;

/**
 * Processes health check request.
 */
public class HealthCheckHttpReceiver implements HttpReceiver {
  private final HealthCheckRegistry healthCheckRegistry;

  @Inject
  public HealthCheckHttpReceiver(HealthCheckRegistry healthCheckRegistry) {
    this.healthCheckRegistry = checkNotNull(healthCheckRegistry);
  }

  @Override
  public void receive(HttpReceiverContext ctx) {
    Map<String, HealthCheck.Result> results = healthCheckRegistry.runHealthChecks();
    ctx.httpResponse()
        .setStatusOk()
        .setMediaType(MediaType.PLAIN_TEXT_UTF_8)
        .setHeader("Cache-Control", "must-revalidate,no-cache,no-store");

    PrintWriter writer = ctx.httpResponse().contentWriter();

    for (Map.Entry<String, HealthCheck.Result> entry : results.entrySet()) {
      HealthCheck.Result result = entry.getValue();

      if (result.isHealthy()) {
        if (result.getMessage() != null) {
          writer.format("* %s: OK\n  %s\n", entry.getKey(), result.getMessage());
        } else {
          writer.format("* %s: OK\n", entry.getKey());
        }
      } else {
        ctx.httpResponse().setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);

        if (result.getMessage() != null) {
          writer.format("! %s: ERROR\n!  %s\n", entry.getKey(), result.getMessage());
        }

        Throwable error = result.getError();
        if (error != null) {
          writer.println();
          error.printStackTrace(writer);
          writer.println();
        }
      }
    }

    writer.flush();
  }
}
