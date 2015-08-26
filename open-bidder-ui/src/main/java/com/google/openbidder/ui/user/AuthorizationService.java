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

package com.google.openbidder.ui.user;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.project.ProjectUser;

/**
 * Service for OAuth 2.0 based authorization.
 */
public interface AuthorizationService {

  /**
   * Initiate an OAuth 2.0 authorization request.
   */
  AuthorizationCodeFlow initializeFlow(Project project);

  /**
   * Determines if the user has tokens..
   */
  boolean isAuthorized(ProjectUser projectUser);

  /**
   * Determines if the flow has tokens..
   */
  boolean isAuthorized(AuthorizationCodeFlow flow);

  /**
   * Get the OAuth 2.0 credentials for the specified user and {@link Project}.
   *
   * @throws com.google.openbidder.ui.project.exception.ProjectNotFoundException
   * if project doesn't exist
   * @throws com.google.openbidder.ui.project.exception.ProjectUserNotFoundException
   * if the current user does not have access to the given project
   * @throws com.google.openbidder.ui.user.exception.NoCredentialsForProjectException
   * if the user has no credentials for the given project
   */
  Credential getCredentialsForProject(ProjectUser projectUser);

  /**
   * Remove tokens for the current user for the given project.
   *
   * @throws com.google.openbidder.ui.project.exception.ProjectNotFoundException
   * if project doesn't exist
   * @throws com.google.openbidder.ui.project.exception.ProjectUserNotFoundException
   * if the current user does not have access to the given project
   */
  ProjectUser revokeAuthorization(ProjectUser projectUser);

  /**
   * Get the {@link UserInfo} for the given {@link ProjectUser} and user.
   */
  UserInfo getUserInfo(ProjectUser projectUser);

  /**
   * Creates an XSRF token for AJAX requests for the currently logged in user.
   */
  String getAuthorizationToken();

  /**
   * Validates an XSRF token for the current user.
   */
  boolean isValidAuthorizationToken(String token);
}
