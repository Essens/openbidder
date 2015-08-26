/*
 * Copyright 2013 Google Inc. All Rights Reserved.
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

package com.google.openbidder.oauth;

import static org.junit.Assert.assertNotNull;

import com.google.common.collect.ImmutableList;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.openbidder.googleapi.GoogleApiModule;
import com.google.openbidder.googlecompute.GoogleComputeModule;
import com.google.openbidder.system.Platform;

import com.beust.jcommander.JCommander;

import org.junit.Test;

import java.util.List;

/**
 * Tests for {@link OAuth2Module}.
 */
public class OAuth2ModuleTest {

  @Test
  public void testGoogleCompute() {
    testModule(Platform.GOOGLE_COMPUTE);
  }

  @Test
  public void testGeneric() {
    testModule(Platform.GENERIC);
  }

  private void testModule(final Platform platform) {
    List<Module> modules = ImmutableList.<Module>of(
        new Module() {
          @Override public void configure(Binder binder) {
            binder.bind(Platform.class).toInstance(platform);
        }},
        new GoogleApiModule(),
        new GoogleComputeModule(),
        new OAuth2Module());
    JCommander jcommander = new JCommander(modules);
    if (platform == Platform.GOOGLE_COMPUTE) {
      jcommander.parse(
          "--api_project_id=0",
          "--api_project_number=0",
          "--service_account=default");
    } else {
      jcommander.parse(
          "--api_project_id=0",
          "--api_project_number=0",
          "--p12_file_path=/path",
          "--service_account_id=0");
    }
    Injector injector = Guice.createInjector(Stage.DEVELOPMENT, modules);
    assertNotNull(injector.getInstance(OAuth2CredentialFactory.class));
  }
}
