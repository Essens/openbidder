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

package com.google.openbidder.googleapi;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.openbidder.config.googleapi.ApiProjectId;
import com.google.openbidder.config.googleapi.ApiProjectNumber;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 * Common bindings for the Google API and HTTP Java libraries.
 */
@Parameters(separators = "=")
public class GoogleApiModule extends AbstractModule {

  @Parameter(names = "--api_project_id",
      description = "Google API Project ID",
      required = true)
  private String apiProjectId;

  @Parameter(names = "--api_project_number",
      description = "Google API Project Number",
      required = true)
  private long projectNumber;

  @Override
  protected void configure() {
    bind(String.class).annotatedWith(ApiProjectId.class).toInstance(apiProjectId);
    bind(Long.class).annotatedWith(ApiProjectNumber.class).toInstance(projectNumber);
    bind(JsonFactory.class).to(JacksonFactory.class).in(Scopes.SINGLETON);
    bind(HttpTransport.class).to(NetHttpTransport.class).in(Scopes.SINGLETON);
  }
}
