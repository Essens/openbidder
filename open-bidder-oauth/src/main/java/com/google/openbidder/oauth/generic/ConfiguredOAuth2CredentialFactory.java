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

package com.google.openbidder.oauth.generic;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.openbidder.oauth.OAuth2CredentialFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

/**
 * Provides an OAuth2 {@link Credential} using values in the properties file.
 * <p>
 * The <a href="https://developers.google.com/accounts/docs/OAuth2ServiceAccount">service account
 * flow</a> is used here when you want to access data owned by your client application.
 * <p>
 * See the <a href="http://code.google.com/apis/accounts/docs/OAuth2.html">
 * OAuth2 Documentation</a> for more information.
 */
public class ConfiguredOAuth2CredentialFactory implements OAuth2CredentialFactory {
  private static final Logger logger =
      LoggerFactory.getLogger(ConfiguredOAuth2CredentialFactory.class);
  private final HttpTransport httpTransport;
  private final JsonFactory jsonFactory;
  private final String p12FilePath;
  private final String serviceAccountId;

  public ConfiguredOAuth2CredentialFactory(
      JsonFactory jsonFactory,
      HttpTransport httpTransport,
      String p12FilePath,
      String serviceAccountId) {

    checkArgument(!Strings.isNullOrEmpty(p12FilePath), "Must specify --p12_file_path");
    checkArgument(!Strings.isNullOrEmpty(serviceAccountId), "Must specify --service_account_id");
    this.jsonFactory = checkNotNull(jsonFactory);
    this.httpTransport = checkNotNull(httpTransport);
    this.p12FilePath = p12FilePath;
    this.serviceAccountId = serviceAccountId;
  }

  @Override
  public Credential retrieveCredential(String scope) {
    GoogleCredential googleCredential = null;
    File p12File = new File(p12FilePath);

    checkState(p12File.exists(), String.format("P12 file %s does not exist", p12FilePath));

    try {
      logger.info("Retrieving credentials for scope: {}", scope);
      googleCredential = new GoogleCredential.Builder().setTransport(httpTransport)
          .setJsonFactory(jsonFactory)
          .setServiceAccountId(serviceAccountId)
          .setServiceAccountScopes(Collections.singletonList(scope))
          .setServiceAccountPrivateKeyFromP12File(p12File)
          .build();
    } catch (GeneralSecurityException | IOException e) {
      throw new IllegalStateException("Error retrieving OAuth2 credentials", e);
    }
    return googleCredential;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("serviceAccountId", serviceAccountId)
        .add("p12FilePath", p12FilePath)
        .toString();
  }
}
