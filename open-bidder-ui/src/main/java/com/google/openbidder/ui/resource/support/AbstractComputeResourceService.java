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

package com.google.openbidder.ui.resource.support;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.openbidder.ui.compute.ComputeClient;
import com.google.openbidder.ui.compute.ComputeService;
import com.google.openbidder.ui.project.ProjectService;
import com.google.openbidder.ui.project.ProjectUser;

import java.util.Set;

/**
 * An extension of {@link AbstractResourceService} that has bindings for
 * {@link ComputeService} to talk to Google Compute Engine and {@link ProjectUser} for resolving
 * project dependencies. All subclasses are children of projects.
 */
public abstract class AbstractComputeResourceService<T extends ExternalResource>
    extends AbstractProjectResourceService<T, ComputeClient> {

  private final ComputeService computeService;

  protected AbstractComputeResourceService(
      ResourceType resourceType,
      Set<ResourceMethod> supportedMethods,
      ProjectService projectService,
      ComputeService computeService) {

    super(resourceType, supportedMethods, projectService);
    checkArgument(resourceType.getParentResourceType() == ResourceType.PROJECT,
        "Expected %s to have parent project resource type, found %s",
        resourceType, resourceType.getParentResourceType());
    this.computeService = checkNotNull(computeService);
  }

  protected final ComputeService getComputeService() {
    return computeService;
  }

  @Override
  protected final ComputeClient getService(ProjectUser parentResource) {
    return computeService.connect(parentResource);
  }
}
