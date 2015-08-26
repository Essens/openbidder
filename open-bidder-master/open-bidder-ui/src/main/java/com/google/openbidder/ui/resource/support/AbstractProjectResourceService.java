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

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import com.google.openbidder.ui.project.ProjectService;
import com.google.openbidder.ui.project.ProjectUser;
import com.google.openbidder.ui.project.exception.ProjectNotFoundException;

import java.util.List;
import java.util.Set;

/**
 * Parent {@link com.google.openbidder.ui.resource.ResourceService} for resources with
 * {@link ResourceType#PROJECT} as a parent.
 * @param <T> resource object
 * @param <S> service or object required to fulfill child resource requests
 */
public abstract class AbstractProjectResourceService<T extends ExternalResource, S>
    extends AbstractResourceService<ProjectUser, T> {

  private final ProjectService projectService;

  protected AbstractProjectResourceService(
      ResourceType resourceType,
      Set<ResourceMethod> supportedMethods,
      ProjectService projectService) {

    super(resourceType, supportedMethods);
    Preconditions.checkArgument(resourceType.getParentResourceType() == ResourceType.PROJECT,
        "Expected %s to have parent project resource type, found %s",
        resourceType, resourceType.getParentResourceType());
    this.projectService = Preconditions.checkNotNull(projectService);
  }

  protected final ProjectService getProjectService() {
    return projectService;
  }

  /**
   * Implementations rely on an external data provider to get information on the child
   * resource.
   * @return An instance of {@code S}
   */
  protected abstract S getService(ProjectUser projectUser);

  @Override
  protected final ProjectUser getParent(String resourceName) {
    Preconditions.checkNotNull(resourceName);
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

    Preconditions.checkNotNull(parentResource);
    Preconditions.checkNotNull(resourceId);
    return get(getService(parentResource), parentResource, resourceId, params);
  }

  @Override
  protected final List<? extends T> list(
      ProjectUser parentResource,
      ResourceCollectionId resourceCollectionId,
      Multimap<String, String> params) {

    Preconditions.checkNotNull(parentResource);
    Preconditions.checkNotNull(resourceCollectionId);
    return list(getService(parentResource), parentResource, resourceCollectionId, params);
  }

  @Override
  protected final T create(
      ProjectUser parentResource,
      ResourceCollectionId resourceCollectionId,
      T childResource) {

    Preconditions.checkNotNull(parentResource);
    Preconditions.checkNotNull(resourceCollectionId);
    Preconditions.checkNotNull(childResource);
    return create(getService(parentResource), parentResource, resourceCollectionId, childResource);
  }

  @Override
  protected final T update(
      ProjectUser parentResource,
      ResourceId childResourceId,
      T childResource) {

    Preconditions.checkNotNull(parentResource);
    Preconditions.checkNotNull(childResource);
    return update(getService(parentResource), parentResource, childResourceId, childResource);
  }

  @Override
  protected final void delete(ProjectUser parentResource, ResourceId resourceId) {
    Preconditions.checkNotNull(parentResource);
    Preconditions.checkNotNull(resourceId);
    delete(getService(parentResource), parentResource, resourceId);
  }

  /**
   * Helper implementation for {@link #get(ProjectUser, ResourceId, Multimap)} that has resolved a
   * {code S}. Any calls to this method have already checked whether the project exists and the
   * user has access to it.
   */
  @SuppressWarnings("unused")
  protected T get(
      S service,
      ProjectUser projectUser,
      ResourceId resourceId,
      Multimap<String, String> params) {

    // this method should only be called if the subclass says that
    // ResourceMethod.GET is supported. If so, the subclass should be overriding it
    throw new UnsupportedOperationException();
  }

  /**
   * Helper implementation for {@link #list(ProjectUser, ResourceCollectionId, Multimap)}
   * that has resolved a {@code S}. Any calls to this method have already checked whether the
   * project exists and the user has access to it.
   */
  @SuppressWarnings("unused")
  protected List<? extends T> list(
      S service,
      ProjectUser projectUser,
      ResourceCollectionId resourceCollectionId,
      Multimap<String, String> params) {

    // this method should only be called if the subclass says that
    // ResourceMethod.LIST is supported. If so, the subclass should be overriding it
    throw new UnsupportedOperationException();
  }

  /**
   * Helper implementation for {@link #create(ProjectUser, ResourceCollectionId, ExternalResource)}
   * that has resolved a {@code S}. Any calls to this method have already checked whether the
   * project exists and the user has access to it.
   */
  @SuppressWarnings("unused")
  protected T create(
      S service,
      ProjectUser projectUser,
      ResourceCollectionId resourceCollectionId,
      T newResource) {

    // this method should only be called if the subclass says that
    // ResourceMethod.CREATE is supported. If so, the subclass should be overriding it
    throw new UnsupportedOperationException();
  }

  /**
   * Helper implementation for {@link #update(ProjectUser, ResourceId, ExternalResource)} that has
   * resolved a {@code S}. Any calls to this method have already checked whether the project exists
   * and the user has access to it.
   */
  @SuppressWarnings("unused")
  protected T update(
      S service,
      ProjectUser projectUser,
      ResourceId childResourceId,
      T updatedResource) {

    // this method should only be called if the subclass says that
    // ResourceMethod.UPDATE is supported. If so, the subclass should be overriding it
    throw new UnsupportedOperationException();
  }

  /**
   * Helper implementation for {@link #delete(ProjectUser, ResourceId)} that has resolved a
   * {code S}. Any calls to this method have already checked whether the project exists and the
   * user has access to it.
   */
  @SuppressWarnings("unused")
  protected void delete(
      S service,
      ProjectUser projectUser,
      ResourceId resourceId) {

    // this method should only be called if the subclass says that
    // ResourceMethod.DELETE is supported. If so, the subclass should be overriding it
    throw new UnsupportedOperationException();
  }
}
