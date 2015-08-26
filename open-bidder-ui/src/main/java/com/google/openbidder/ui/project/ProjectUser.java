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

package com.google.openbidder.ui.project;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.entity.support.UserRole;

/**
 * This is a view across a {@link Project} and the rights and preferences of a given user
 * (identified by email) over that project. This includes what {@link UserRole} the user
 * has for this project and whether or not it is the default project for that user.
 */
public class ProjectUser implements Comparable<ProjectUser> {
  private final Project project;
  private final UserRole userRole;
  private final String email;
  private final boolean isDefault;

  public ProjectUser(Project project, UserRole userRole, String email, boolean isDefault) {
    this.project = checkNotNull(project);
    this.userRole = checkNotNull(userRole);
    this.email = checkNotNull(email);
    this.isDefault = isDefault;
  }

  public Project getProject() {
    return project;
  }

  /**
   * In cases where the {@link Project} is updated as part of the work it is necessary to
   * update the {@link ProjectUser} with the new project.
   */
  public ProjectUser updateProject(Project project) {
    return new ProjectUser(project, userRole, email, isDefault);
  }

  public UserRole getUserRole() {
    return userRole;
  }

  public String getEmail() {
    return email;
  }

  public boolean isDefault() {
    return isDefault;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("project", project)
        .add("userRole", userRole)
        .add("email", email)
        .add("isDefault", isDefault)
        .toString();
  }

  @Override
  public int compareTo(ProjectUser other) {
    return project.getId().compareTo(other.project.getId());
  }
}
