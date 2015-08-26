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

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.json.MetricsModule;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

/**
 * Processes metrics request.
 */
public class MetricsHttpReceiver implements HttpReceiver {
  private final MetricRegistry registry;
  private final ObjectMapper mapperSamplesOff;
  private final ObjectMapper mapperSamplesOn;
  private final PrettyPrinter prettyPrinter;

  @Inject
  public MetricsHttpReceiver(
      MetricRegistry registry,
      JsonFactory factory) {

    this.registry = checkNotNull(registry);
    this.mapperSamplesOff = new ObjectMapper(factory)
        .registerModule(new MetricsModule(TimeUnit.SECONDS, TimeUnit.SECONDS, false));
    this.mapperSamplesOn = new ObjectMapper(factory)
        .registerModule(new MetricsModule(TimeUnit.SECONDS, TimeUnit.SECONDS, true));
    this.prettyPrinter = new MetricsPrettyPrinter();
  }

  @Override
  public void receive(HttpReceiverContext ctx) {
    boolean pretty = Boolean.parseBoolean(ctx.httpRequest().getParameter("pretty"));
    boolean showSamples = Boolean.parseBoolean(ctx.httpRequest().getParameter("samples"));

    try {
      ObjectMapper mapper = showSamples ? mapperSamplesOn : mapperSamplesOff;
      ObjectWriter writer = pretty ? mapper.writer(prettyPrinter) : mapper.writer();
      writer.writeValue(ctx.httpResponse().contentWriter(), registry);

      ctx.httpResponse()
          .setStatusOk()
          .setMediaType(MediaType.JSON_UTF_8)
          .setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static class MetricsPrettyPrinter extends DefaultPrettyPrinter {
    public MetricsPrettyPrinter() {
      super(DEFAULT_ROOT_VALUE_SEPARATOR);
    }

    public MetricsPrettyPrinter(MetricsPrettyPrinter pp) {
      super(pp);
    }

    @Override public DefaultPrettyPrinter createInstance() {
      return new MetricsPrettyPrinter(this);
    }

    @Override public void writeObjectFieldValueSeparator(JsonGenerator jg) throws IOException {
      jg.writeRaw(": ");
    }

    @Override public void writeEndArray(JsonGenerator jg, int nrOfValues) throws IOException {
        if (!_arrayIndenter.isInline()) {
            --_nesting;
        }
        if (nrOfValues > 0) {
            _arrayIndenter.writeIndentation(jg, _nesting);
        }
        jg.writeRaw(']');
    }
  }
}
