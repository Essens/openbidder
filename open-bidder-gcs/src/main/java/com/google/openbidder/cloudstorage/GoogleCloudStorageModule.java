/*
 * Copyright 2012 Google Inc. All Rights Reserved.
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

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;
import com.google.openbidder.cloudstorage.config.StorageOAuth2Scope;
import com.google.openbidder.cloudstorage.config.StorageRequestInitializers;
import com.google.openbidder.config.googleapi.ApiProjectNumber;
import com.google.openbidder.oauth.OAuth2CredentialFactory;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.util.Set;

import javax.inject.Singleton;

/**
 * Guice bindings provider for {@link GoogleCloudStorage}.
 */
@Parameters(separators = "=")
public class GoogleCloudStorageModule extends AbstractModule {

  @Parameter(names = "--storage_oauth2_scope",
      description = "Google Cloud Storage OAuth 2.0 scope",
      required = false)
  private String storageOAuth2Scope = StorageOAuth2Scope.DEFAULT;

  @Override
  protected void configure() {
    bind(String.class).annotatedWith(StorageOAuth2Scope.class).toInstance(storageOAuth2Scope);
    Multibinder.newSetBinder(
        binder(), HttpRequestInitializer.class, StorageRequestInitializers.class);
  }

  @Provides
  @Singleton
  public GoogleCloudStorage provideGoogleCloudStorage(
      HttpTransport httpTransport,
      OAuth2CredentialFactory oauth2CredentialFactory,
      @ApiProjectNumber long apiProjectNumber,
      @StorageOAuth2Scope String storageOAuth2Scope,
      @StorageRequestInitializers Set<HttpRequestInitializer> requestInitializers) {

    return GoogleCloudStorageFactory.newFactory()
        .setHttpTransport(httpTransport)
        .setApiProjectNumber(apiProjectNumber)
        .setCredential(oauth2CredentialFactory.retrieveCredential(storageOAuth2Scope))
        .setRequestInitializers(requestInitializers)
        .build();
  }
}
