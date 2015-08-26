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

package com.google.openbidder.googlecompute;

import com.google.api.client.util.Key;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import javax.annotation.Nullable;

/**
 * OAuth2 service token information stored on the Google Compute instance's
 * metadata server
 */
public final class OAuth2ServiceTokenMetadata {

  @Key(value = "accessToken")
  private String accessToken;
  @Key(value = "expiresAt")
  private Long expiresAt;
  @Key(value = "expiresIn")
  private Long expiresIn;

  /**
   * @return The OAuth2 access token
   */
  public final String getAccessToken() {
    return accessToken;
  }

  public final void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  /**
   * @return Timestamp when the token will expire
   */
  public final Long getExpiresAt() {
    return expiresAt;
  }

  public final void setExpiresAt(Long expiresAt) {
    this.expiresAt = expiresAt;
  }

  /**
   * @return Number of seconds until the token expires
   */
  public final Long getExpiresIn() {
    return expiresIn;
  }

  public final void setExpiresIn(Long expiresIn) {
    this.expiresIn = expiresIn;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(accessToken, expiresAt, expiresIn);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (obj == this) {
      return true;
    } else if (!(obj instanceof OAuth2ServiceTokenMetadata)) {
      return false;
    }

    OAuth2ServiceTokenMetadata other = (OAuth2ServiceTokenMetadata) obj;

    return Objects.equal(accessToken, other.accessToken)
        && Objects.equal(expiresAt, other.expiresAt)
        && Objects.equal(expiresIn, other.expiresIn);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("accessToken", accessToken)
        .add("expiresAt", expiresAt)
        .add("expiresIn", expiresIn)
        .toString();
  }
}
