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

package com.google.openbidder.bigquery;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.bigquery.Bigquery;
import com.google.api.services.bigquery.BigqueryScopes;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.openbidder.oauth.OAuth2CredentialFactory;

import javax.inject.Singleton;

/**
 * BigQuery support.
 */
public class BigQueryModule extends AbstractModule {

  @Override
  protected void configure() {
  }

  @Provides
  @Singleton
  public Bigquery provideGoogleBigQuery(
      HttpTransport httpTransport,
      OAuth2CredentialFactory credentialFactory,
      JsonFactory jsonFactory) {

    return new Bigquery.Builder(
            httpTransport,
            jsonFactory,
            credentialFactory.retrieveCredential(BigqueryScopes.BIGQUERY))
        .setApplicationName("Open Bidder")
        .build();
  }
}
