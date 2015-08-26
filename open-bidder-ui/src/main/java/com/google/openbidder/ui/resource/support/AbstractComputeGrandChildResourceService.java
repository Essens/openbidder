/*
 * Copyright 2013 Google Inc. All Rights Reserved.
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.openbidder.ui.compute.ComputeClient;
import com.google.openbidder.ui.compute.ComputeService;
import com.google.openbidder.ui.project.ProjectService;
import com.google.openbidder.ui.project.ProjectUser;
import com.google.openbidder.ui.project.exception.ProjectNotFoundException;

import java.util.List;
import java.util.Set;

/**
 * An extension of {@link AbstractResourceService} that has bindings for
 * {@link com.google.openbidder.ui.compute.ComputeService} to talk to Google Compute Engine
 * and {@link com.google.openbidder.ui.project.ProjectUser} for resolving project dependencies.
 * All subclasses are children of per zone resources.
 */
public abstract class AbstractComputeGrandChildResourceService<T extends ExternalResource>
    extends AbstractGrandChildResourceService<ProjectUser, T> {

  private final ProjectService projectService;
  private final ComputeService computeService;

  protected AbstractComputeGrandChildResourceService(
      ProjectService projectService,
      ComputeService computeService,
      ResourceType resourceType,
      Set<ResourceMethod> supportedMethods) {

    super(resourceType, supportedMethods);
    checkArgument(resourceType.getParentResourceType() == ResourceType.ZONE,
      "Expected %s to have parent zone resource type, found %s",
      resourceType, resourceType.getParentResourceType());
    this.projectService = checkNotNull(projectService);
    this.computeService = checkNotNull(computeService);
  }

  protected final ProjectService getProjectService() {
    return projectService;
  }

  protected final ComputeService getComputeService() {
    return computeService;
  }

  protected final ComputeClient getService(ProjectUser parentResource) {
    return computeService.connect(parentResource);
  }

  @Override
  protected final ProjectUser getParentResource(String resourceName) {
    checkNotNull(resourceName);
    long projectId;
    try {
      projectId = Long.parseLong(resourceName);
    } catch (NumberFormatException e) {
      throw new ProjectNotFoundException(resourceName);
    }
    return projectService.getProject(projectId);
  }

  @Override
  protected final T get(
      ProjectUser parentResource,
      ResourceId resourceId,
      Multimap<String, String> params) {

    checkNotNull(parentResource);
    checkNotNull(resourceId);
    return get(getService(parentResource), parentResource, resourceId, params);
  }

  @Override
  protected final T create(
      ProjectUser parentResource,
      ResourceCollectionId resourceCollectionId,
      T grandChildResource) {

    checkNotNull(parentResource);
    checkNotNull(resourceCollectionId);
    checkNotNull(grandChildResource);
    return create(
        getService(parentResource),
        parentResource,
        resourceCollectionId,
        grandChildResource);
  }

  @Override
  protected final void delete(ProjectUser parentResource, ResourceId resourceId) {
    checkNotNull(parentResource);
    checkNotNull(resourceId);
    delete(getService(parentResource), parentResource, resourceId);
  }

  @Override
  protected final List<? extends T> list(
      ProjectUser parentResource,
      ResourceCollectionId resourceCollectionId,
      Multimap<String, String> params) {
    checkNotNull(parentResource);
    checkNotNull(resourceCollectionId);
    return list(getService(parentResource), parentResource, resourceCollectionId, params);
  }

  /**
   * Helper implementation for {@link #get(ProjectUser, ResourceId, Multimap)}.
   * Any calls to this method have already checked whether the project exists and the
   * user has access to it.
   */
  protected T get(
      ComputeClient computeClient,
      ProjectUser projectUser,
      ResourceId resourceId,
      Multimap<String, String> params) {

    // this method should only be called if the subclass says that
    // ResourceMethod.GET is supported. If so, the subclass should be overriding it
    throw new UnsupportedOperationException();
  }


  /**
   * Helper implementation for {@link #list(ProjectUser, ResourceCollectionId, Multimap)}.
   * Any calls to this method have already checked whether the project exists and the user has
   * access to it.
   */
  protected ImmutableList<? extends T> list(
      ComputeClient computeClient,
      ProjectUser projectUser,
      ResourceCollectionId resourceCollectionId,
      Multimap<String, String> params) {

    // this method should only be called if the subclass says that
    // ResourceMethod.LIST is supported. If so the subclass should be overriding it
    throw new UnsupportedOperationException();
  }

  /**
   * Helper implementation for {@link #create(ProjectUser, ResourceCollectionId, ExternalResource)}.
   * Any calls to this method have already checked whether the project exists and the user has
   * access to it.
   */
  protected T create(
      ComputeClient computeClient,
      ProjectUser projectUser,
      ResourceCollectionId resourceCollectionId,
      T newResource) {

    // this method should only be called if the subclass says that
    // ResourceMethod.CREATE is supported. If so, the subclass should be overriding it
    throw new UnsupportedOperationException();
  }

  /**
   * Helper implementation for {@link #delete(ProjectUser, ResourceId)}.
   * Any calls to this method have already checked whether the project exists and the
   * user has access to it.
   */
  protected void delete(
      ComputeClient computeClient,
      ProjectUser projectUser,
      ResourceId resourceId) {

    // this method should only be called if the subclass says that
    // ResourceMethod.DELETE is supported. If so, the subclass should be overriding it
    throw new UnsupportedOperationException();
  }
}
