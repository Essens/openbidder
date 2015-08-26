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

import com.google.common.base.Function;
import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.entity.UserPreference;

import java.util.List;

/**
 * Per-project configuration services.
 */
public interface ProjectService {

  /**
   * Retrieves a {@link Project} by ID.
   *
   * @return Project summary
   * @throws com.google.openbidder.ui.project.exception.ProjectNotFoundException
   * if project doesn't exist
   * @throws com.google.openbidder.ui.project.exception.ProjectUserNotFoundException
   * if the current user
   * does not have access to the given project
   */
  ProjectUser getProject(long projectId);

  /**
   * Retrieves a {@link Project} by ID.
   *
   * @return Project summary
   * @throws com.google.openbidder.ui.project.exception.ProjectNotFoundException
   * if project doesn't exist
   * @throws com.google.openbidder.ui.user.exception.UserNotFoundException
   * if the user doesn't exist
   * @throws com.google.openbidder.ui.project.exception.ProjectUserNotFoundException
   * if the specified user does not have access to the given project
   */
  ProjectUser getProjectForUser(long projectId, String userEmail);

  /**
   * Select the given project as the default for the specified user.
   */
  UserPreference setAsDefaultProject(ProjectUser projectUser);

  /**
   * Retrieve all {@link Project}s a user has access to.
   */
  List<ProjectUser> getAllProjectsForUser();

  /**
   * Retrieve all users attached to a {@link Project}.
   *
   * @throws com.google.openbidder.ui.project.exception.ProjectNotFoundException
   * if project doesn't exist
   */
  List<ProjectUser> getProjectUsers(long projectId);

  /**
   * Create a new {@link Project} for the current user.
   */
  ProjectUser insertProject(Project project);

  /**
   * Atomically update a {@link Project} with a transformation {@link Function}.
   *
   * @throws com.google.openbidder.ui.project.exception.ProjectNotFoundException
   * if project doesn't currently exist
   * @throws com.google.openbidder.ui.project.exception.NoProjectWriteAccessException
   * if the current user does not have write access.
   */
  Project updateProject(long projectId, Function<Project, Project> projectTransformer);

  /**
   * Delete a {@link Project}.
   */
  void deleteProject(long projectId);
}
