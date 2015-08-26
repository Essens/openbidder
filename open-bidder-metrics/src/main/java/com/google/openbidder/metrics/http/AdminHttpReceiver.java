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

import com.google.common.collect.ImmutableMap;
import com.google.common.net.MediaType;
import com.google.openbidder.config.template.AdminTemplate;
import com.google.openbidder.http.HttpReceiver;
import com.google.openbidder.http.HttpReceiverContext;
import com.google.openbidder.http.template.Template;
import com.google.openbidder.metrics.config.HealthCheckPath;
import com.google.openbidder.metrics.config.MetricsPath;
import com.google.openbidder.metrics.config.PingPath;
import com.google.openbidder.metrics.config.ThreadsPath;

import javax.inject.Inject;

/**
 * Simple HTML page with links to the different metrics pages.
 */
public class AdminHttpReceiver implements HttpReceiver {
  private static final String PARAM_METRICS_PATH = "metricsPath";
  private static final String PARAM_PING_PATH = "pingPath";
  private static final String PARAM_THREADS_PATH = "threadsPath";
  private static final String PARAM_HEALTH_CHECK_PATH = "healthCheckPath";

  private final String content;

  @Inject
  public AdminHttpReceiver(
      @AdminTemplate Template adminTemplate,
      @MetricsPath String metricsPath,
      @PingPath String pingPath,
      @ThreadsPath String threadsPath,
      @HealthCheckPath String healthCheckPath) {

    content = adminTemplate.process(ImmutableMap.<String, Object>of(
        PARAM_METRICS_PATH, metricsPath,
        PARAM_PING_PATH, pingPath,
        PARAM_THREADS_PATH, threadsPath,
        PARAM_HEALTH_CHECK_PATH, healthCheckPath
    ));
  }

  @Override
  public void receive(HttpReceiverContext ctx) {
    ctx.httpResponse()
        .setStatusOk()
        .setMediaType(MediaType.HTML_UTF_8)
        .setHeader("Cache-Control", "must-revalidate,no-cache,no-store")
        .printContent(content);
  }
}
