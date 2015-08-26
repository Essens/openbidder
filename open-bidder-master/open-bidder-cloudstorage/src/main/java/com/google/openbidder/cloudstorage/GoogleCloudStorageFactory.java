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

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.common.collect.ImmutableSet;
import com.google.openbidder.cloudstorage.impl.GoogleCloudStorageImpl;

import java.io.IOException;
import java.util.Set;

/**
 * Factory for {@link GoogleCloudStorage}.
 */
public final class GoogleCloudStorageFactory {
  private HttpTransport httpTransport;
  private long apiProjectNumber;
  private Credential credential;
  private Set<HttpRequestInitializer> requestInitializers = ImmutableSet.of();

  private GoogleCloudStorageFactory() {
  }

  public static GoogleCloudStorageFactory newFactory() {
    return new GoogleCloudStorageFactory();
  }

  public GoogleCloudStorageFactory setHttpTransport(HttpTransport httpTransport) {
    this.httpTransport = httpTransport;
    return this;
  }

  public GoogleCloudStorageFactory setApiProjectNumber(long apiProjectNumber) {
    this.apiProjectNumber = apiProjectNumber;
    return this;
  }

  public GoogleCloudStorageFactory setCredential(Credential credential) {
    this.credential = credential;
    return this;
  }

  public GoogleCloudStorageFactory setRequestInitializers(
      Set<HttpRequestInitializer> requestInitializers) {
    this.requestInitializers = ImmutableSet.copyOf(requestInitializers);
    return this;
  }

  public GoogleCloudStorage build() {
    return new GoogleCloudStorageImpl(httpTransport, new BasicInitializer());
  }

  class BasicInitializer implements HttpRequestInitializer {
    @Override public void initialize(HttpRequest request) throws IOException {
      // Add the OAuth2 headers
      credential.initialize(request);

      // Add some cloud storage specific headers
      request.getHeaders().put(
          GoogleCloudStorageConstants.API_VERSION_HEADER,
          GoogleCloudStorageConstants.API_VERSION);
      request.getHeaders().put(
          GoogleCloudStorageConstants.PROJECT_ID_HEADER,
          apiProjectNumber);

      for (HttpRequestInitializer requestInitializer : requestInitializers) {
        requestInitializer.initialize(request);
      }
    }
  }
}
