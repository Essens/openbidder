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

package com.google.openbidder.cloudstorage;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.openbidder.googleapi.GoogleApiModule;
import com.google.openbidder.googlecompute.InstanceMetadata;
import com.google.openbidder.oauth.OAuth2Module;
import com.google.openbidder.system.Platform;

import com.beust.jcommander.JCommander;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

/**
 * Tests for {@link GoogleCloudStorage}.
 */
public class GoogleCloudStorageModuleTest {

  @Test
  public void testModule() {
    List<Module> modules = ImmutableList.<Module>of(
        new Module() {
          @Override public void configure(Binder binder) {
            binder.bind(Platform.class).toInstance(Platform.GOOGLE_COMPUTE);
            InstanceMetadata metadata = Mockito.mock(InstanceMetadata.class);
            when(metadata.metadata("hostname")).thenReturn("localhost");
            when(metadata.metadata("zone")).thenReturn("us-central");
            binder.bind(InstanceMetadata.class).toInstance(metadata);
        }},
        new GoogleApiModule(),
        new OAuth2Module(),
        new GoogleCloudStorageModule());
    JCommander jcommander = new JCommander(modules);
    jcommander.parse(
        "--api_project_id=0",
        "--api_project_number=0",
        "--service_account=default");
    Injector injector = Guice.createInjector(Stage.DEVELOPMENT, modules);
    assertNotNull(injector.getInstance(GoogleCloudStorage.class));
  }
}
