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

package com.google.openbidder.ui.entity;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;

import org.joda.time.Instant;

/**
 * XSRF authorization token.
 */
@Entity
public class AuthorizationToken {

  @Parent
  @Index
  private Key<UserPreference> userPreferences;

  @Id
  @Index
  private String token;

  @Index
  private Instant createdAt;

  @SuppressWarnings("unused")
  private AuthorizationToken() {
  }

  public AuthorizationToken(String userEmail, String token, Instant createdAt) {
    this.userPreferences = UserPreference.key(userEmail);
    this.token = checkNotNull(token);
    this.createdAt = checkNotNull(createdAt);
  }

  public Key<UserPreference> getUserPreferences() {
    return userPreferences;
  }

  public String getUserEmail() {
    return userPreferences.getParent().getName();
  }

  public String getToken() {
    return token;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("userEmail", userPreferences.getName())
        .add("token", token)
        .add("createdAt", createdAt)
        .toString();
  }

  public static Key<AuthorizationToken> key(String userEmail, String token) {
    return Key.create(UserPreference.key(userEmail), AuthorizationToken.class, token);
  }
}
