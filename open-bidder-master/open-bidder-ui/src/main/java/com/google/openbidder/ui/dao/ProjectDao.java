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

import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.util.db.Transactable;

import java.util.Map;

/**
 * Persistence functions related to {@link Project}s.
 */
public interface ProjectDao {

  /**
   * Get {@link Project} by ID.
   *
   * @throws com.google.openbidder.ui.project.exception.ProjectNotFoundException
   * if project is not found
   */
  Project getProjectById(long projectId);

  /**
   * Batch fetch a number of {@link Project}s.
   */
  Map<Long, Project> getProjectsByIds(Iterable<Long> projectIds);

  /**
   * Create a {@link Project}.
   *
   * @return ID of newly created project.
   */
  long createProject(Project project);

  /**
   * Atomically update a {@link Project}.
   *
   * @throws com.google.openbidder.ui.project.exception.ProjectNotFoundException
   * if project is not found
   */
  <T> T updateProject(long projectId, Transactable<Project, T> worker);
}
