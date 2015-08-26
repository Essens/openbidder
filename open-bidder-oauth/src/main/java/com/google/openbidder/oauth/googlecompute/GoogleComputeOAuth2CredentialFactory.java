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

package com.google.openbidder.oauth.googlecompute;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.api.client.auth.oauth2.Credential;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.openbidder.googlecompute.InstanceMetadata;
import com.google.openbidder.oauth.OAuth2CredentialFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides OAuth2 {@link Credential}s from the Google Compute metadata server.
 *
 * <p>
 * Credentials can only be retrieved for scopes that were included in the service account scopes
 * set when the instance was created.  See the
 * <a href="https://developers.google.com/compute/docs/authentication">
 * Google Compute documentation</a> for more information
 * about OAuth2 authentication on Google Compute and defining service account scopes.
 */
public class GoogleComputeOAuth2CredentialFactory implements OAuth2CredentialFactory {
  private static final Logger logger = LoggerFactory.getLogger(
      GoogleComputeOAuth2CredentialFactory.class);
  private final InstanceMetadata metadata;
  private final String serviceAccount;

  public GoogleComputeOAuth2CredentialFactory(
      InstanceMetadata metadata,
      String serviceAccount) {

    checkArgument(!Strings.isNullOrEmpty(serviceAccount), "Must specify --service_account.");
    this.metadata = checkNotNull(metadata);
    this.serviceAccount = serviceAccount;
  }

  @Override
  public Credential retrieveCredential(String scope) {
    if (logger.isDebugEnabled()) {
      logger.debug("Retrieving OAuth2 scope {} from Compute Engine metadata server", scope);
    }
    return new GoogleComputeCredential(metadata, serviceAccount, scope);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("serviceAccount", serviceAccount)
        .toString();
  }
}
