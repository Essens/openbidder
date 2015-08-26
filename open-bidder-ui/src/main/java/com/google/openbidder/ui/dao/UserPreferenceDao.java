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

package com.google.openbidder.ui.dao;

import com.google.openbidder.ui.entity.UserPreference;
import com.google.openbidder.ui.util.db.Transactable;

/**
 * Persistence functions for user preferences.
 */
public interface UserPreferenceDao {

  /**
   * Get the {@link UserPreference}s for a user.
   *
   * @throws com.google.openbidder.ui.user.exception.UserNotFoundException if user not found.
   */
  UserPreference getUserPreferences(String userEmail);

  /**
   * Perform an atomic update of {@link UserPreference}s, creating an instance if it
   * doesn't exist already.
   */
  <T> T updateUserPreferences(String userEmail, Transactable<UserPreference, T> worker);

  /**
   * Get all {@link UserPreference}s for users attached to a given
   * {@link com.google.openbidder.ui.entity.Project}.
   */
  Iterable<UserPreference> getAllUsersForProject(long projectId);

  /**
   * Remove the project from all related {@link UserPreference}s after the project deletion.
   */
  void removeProjectFromAllUserPreferences(long projectId);

  /**
   * Create an XSRF authorization token with default expiry.
   */
  String getAuthorizationToken(String userEamil);

  /**
   * Validate an authorization token for a given user.
   */
  boolean isValidAuthorizationToken(String userEmail, String authorizationToken);
}
