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

package com.google.openbidder.googlecompute;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

/**
 * Bindings for Google Compute Engine.
 */
public class GoogleComputeModule extends AbstractModule {

  @Override
  protected void configure() {
  }

  @Provides public InstanceMetadata provideInstanceMetadata(
      JsonFactory jsonFactory, HttpTransport httpTransport) {
    return new InstanceMetadata(jsonFactory, httpTransport);
  }
}
