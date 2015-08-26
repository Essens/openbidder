/*
 * Copyright 2014 Google Inc. All Rights Reserved.
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
import static org.mockito.Mockito.when;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.net.MediaType;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.Stage;
import com.google.openbidder.bigquery.BigQueryModule;
import com.google.openbidder.config.googleapi.ApiProjectId;
import com.google.openbidder.googlecompute.InstanceMetadata;
import com.google.openbidder.oauth.OAuth2CredentialFactory;
import com.google.openbidder.util.ResourceHttpTransport;

import com.beust.jcommander.JCommander;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

/**
 * Tests for {@link BigQueryMetricsReporterModule}.
 */
public class BigQueryMetricsReporterModuleTest {

  @Test
  public void testEnabled() {
    testModule(true);
  }

  @Test
  public void testDisabled() {
    testModule(false);
  }

  private void testModule(boolean enabled) {
    List<Module> modules = ImmutableList.of(
        new Module() {
          @Override public void configure(Binder binder) {
            InstanceMetadata metadata = Mockito.mock(InstanceMetadata.class);
            when(metadata.metadata("hostname")).thenReturn("localhost");
            when(metadata.metadata("zone")).thenReturn("us-central");
            binder.bind(InstanceMetadata.class).toInstance(metadata);
            binder.bind(String.class).annotatedWith(ApiProjectId.class).toInstance("0");
            binder.bind(HttpTransport.class).toInstance(ResourceHttpTransport.create()
                .setResourceName("/datasets_get.json")
                .setContentType(MediaType.JSON_UTF_8));
            binder.bind(JsonFactory.class).to(JacksonFactory.class).in(Scopes.SINGLETON);
            binder.bind(OAuth2CredentialFactory.class).toInstance(
                Mockito.mock(OAuth2CredentialFactory.class));
          }
        },
        new BigQueryModule(),
        new BigQueryMetricsReporterModule());
    JCommander jcommander = new JCommander(modules);
    jcommander.parse(enabled ? "--metrics_reporter_dataset=tmp" : "");
    assertNotNull(Guice.createInjector(Stage.DEVELOPMENT, modules));
  }
}
