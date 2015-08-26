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

package com.google.openbidder.oauth;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.common.base.Strings;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.openbidder.googlecompute.InstanceMetadata;
import com.google.openbidder.oauth.generic.ConfiguredOAuth2CredentialFactory;
import com.google.openbidder.oauth.googlecompute.GoogleComputeOAuth2CredentialFactory;
import com.google.openbidder.system.Platform;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Guice support for OAuth2.
 */
@Parameters(separators = "=")
public class OAuth2Module extends AbstractModule {

  private static final Logger logger = LoggerFactory.getLogger(OAuth2Module.class);

  @Parameter(names = "--p12_file_path",
      description = "P12 file path (required if platform is generic)")
  private String p12FilePath;

  @Parameter(names = "--service_account_id",
      description = "Service account ID (required if platform is generic)")
  private String serviceAccountId;

  @Parameter(names = "--service_account",
      description = "Service account (required if platform is compute)")
  private String serviceAccount = "default";

  @Override
  protected void configure() {
  }

  @Provides public OAuth2CredentialFactory provideOAuth2CredentialFactory(
      Platform platform,
      InstanceMetadata metadata,
      JsonFactory jsonFactory,
      HttpTransport httpTransport) {

    switch (platform) {
      case GOOGLE_COMPUTE:
        logger.info("Configuring GCE OAuth 2.0 support");
        checkArgument(!Strings.isNullOrEmpty(serviceAccount), "Must specify --service_account");
        return new GoogleComputeOAuth2CredentialFactory(metadata, serviceAccount);

      case GENERIC:
        logger.info("Configuring generic Linux OAuth 2.0 support");
        checkArgument(!Strings.isNullOrEmpty(p12FilePath), "Must specify --p12_file_path");
        checkArgument(!Strings.isNullOrEmpty(serviceAccountId), "Must specify --service_account_id");
        return new ConfiguredOAuth2CredentialFactory(
            jsonFactory, httpTransport, p12FilePath, serviceAccountId);

      default:
        throw new IllegalStateException("Unknown platform: " + platform);
    }
  }
}
