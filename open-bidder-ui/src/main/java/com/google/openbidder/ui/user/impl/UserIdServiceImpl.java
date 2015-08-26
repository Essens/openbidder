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

package com.google.openbidder.ui.user.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.openbidder.ui.dao.UserPreferenceDao;
import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.entity.UserPreference;
import com.google.openbidder.ui.project.ProjectUser;
import com.google.openbidder.ui.user.UserIdService;
import com.google.openbidder.ui.user.exception.NotLoggedInException;

import javax.inject.Inject;

/**
 * Default implementation for {@link UserIdService}.
 * <p>
 * Retrieves the current user and converts that into an email address. Note: this may
 * require some catering for {@link User#getFederatedIdentity} to fully support OpenID beyond
 * Google accounts.
 */
public class UserIdServiceImpl implements UserIdService {

  private final UserService userService;
  private final UserPreferenceDao userPreferenceDao;

  @Inject
  public UserIdServiceImpl(
      UserService userService,
      UserPreferenceDao userPreferenceDao) {

    this.userService = checkNotNull(userService);
    this.userPreferenceDao = checkNotNull(userPreferenceDao);
  }

  @Override
  public String getUserId() {
    User currentUser = userService.getCurrentUser();
    if (currentUser == null) {
      throw new NotLoggedInException();
    }
    return getUserId(userService.getCurrentUser());
  }

  @Override
  public String getUserId(User user) {
    return user.getEmail();
  }

  @Override
  public UserPreference getUserPreference(String userId) {
    return userPreferenceDao.getUserPreferences(userId);
  }

  @Override
  public boolean isAdmin(String userId) {
    checkNotNull(userId);
    User currentUser = userService.getCurrentUser();
    return userId.equals(getUserId(currentUser)) && userService.isUserAdmin();
  }

  @Override
  public ProjectUser buildProjectUser(Project project, UserPreference userPreference) {
    checkNotNull(project);
    checkNotNull(userPreference);
    return new ProjectUser(project,
        userPreference.getUserRole(project.getId()),
        userPreference.getEmail(),
        userPreference.isDefaultProject(project.getId()));
  }
}
