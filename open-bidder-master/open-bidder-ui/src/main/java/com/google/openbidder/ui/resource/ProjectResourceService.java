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

package com.google.openbidder.ui.resource;

import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.resource.model.ProjectResource;

/**
 * {@link RootResourceService} for {@link ProjectResource}s.
 */
public interface ProjectResourceService extends RootResourceService<ProjectResource> {

  /**
   * Sets the project with the given ID as the default project for the current user.
   */
  ProjectResource setAsDefault(String projectId);

  /**
   * Add default bidding config.
   */
  void addDefaultBidderConfig(Project project);

    /**
    * Determines if the current user has access tokens for the given project.
    */
  boolean isAuthorized(String projectId);

  /**
   * Revokes OAuth authorization for this project and user.
   */
  ProjectResource revokeAuthorization(String projectId);
}
