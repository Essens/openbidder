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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.common.base.MoreObjects;
import com.google.openbidder.googlecompute.InstanceMetadata;
import com.google.openbidder.googlecompute.OAuth2ServiceTokenMetadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of {@link Credential} which uses the Google
 * Compute metadata to retrieve and refresh OAuth2 authentication tokens.
 */
public class GoogleComputeCredential extends Credential {
  private static final Logger logger = LoggerFactory.getLogger(GoogleComputeCredential.class);
  private final InstanceMetadata metadata;
  private final String serviceAccountScope;
  private final String serviceAccount;

  public GoogleComputeCredential(
      InstanceMetadata metadata,
      String serviceAccount,
      String serviceAccountScope) {
    super(BearerToken.authorizationHeaderAccessMethod());

    this.metadata = checkNotNull(metadata);
    this.serviceAccountScope = checkNotNull(serviceAccountScope);
    this.serviceAccount = checkNotNull(serviceAccount);
  }

  /**
   * Refreshes the access token by re-reading the Compute metadata.
   *
   * As per the documentation in {@link Credential#executeRefreshToken()}
   * this does not need to include locking since the public method
   * {@link Credential#refreshToken()} handles the locking.
   *
   * @throws com.google.openbidder.googlecompute.MetadataOAuth2ScopeNotFoundException
   * If the scope was not found in the meta-data server
   */
  @Override
  protected TokenResponse executeRefreshToken() {
    if (logger.isDebugEnabled()) {
      logger.debug("Refreshing access token for oauth2 scope {}", serviceAccountScope);
    }
    OAuth2ServiceTokenMetadata serviceTokenMetadata = metadata.serviceToken(
        serviceAccount, serviceAccountScope);
    TokenResponse tokenResponse = new TokenResponse();
    tokenResponse.setAccessToken(serviceTokenMetadata.getAccessToken());
    tokenResponse.setExpiresInSeconds(serviceTokenMetadata.getExpiresIn());
    tokenResponse.setScope(serviceAccountScope);
    return tokenResponse;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("serviceAccount", serviceAccount)
        .add("serviceAccountScope", serviceAccountScope)
        .toString();
  }
}
