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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.openbidder.ui.resource.ResourceService;

import java.util.List;
import java.util.Set;

/**
 * Abstract implementation of {@link ResourceService} for child resources that adds
 * supported method validation and converts {@link String} resource names to correct
 * {@link ResourceId} and {@link ResourceCollectionId} instances, as appropriate.
 * <p>
 * Note: all methods are implemented and will throw {@link UnsupportedOperationException}s
 * if called. The intent is that implementing subclasses need only override the supported
 * methods.
 */
public abstract class AbstractResourceService<P, C extends ExternalResource>
    extends AbstractResourceMethodService
    implements ResourceService<C> {

  protected AbstractResourceService(
      ResourceType resourceType,
      Set<ResourceMethod> supportedMethods) {

    super(resourceType, supportedMethods);
  }

  @Override
  public final boolean supports(ResourceMethod resourceMethod) {
    return isMethodSupported(resourceMethod);
  }

  @Override
  public final C get(
      String parentResourceName,
      String childResourceName,
      Multimap<String, String> params) {

    checkNotNull(parentResourceName);
    checkNotNull(childResourceName);
    ResourceId resourceId = getResourceId(parentResourceName, childResourceName);
    checkSupported(resourceId, ResourceMethod.GET);
    P parentResource = getParent(parentResourceName);
    return get(parentResource, resourceId, params);
  }

  @Override
  public final List<? extends C> list(
      String parentResourceName,
      Multimap<String, String> params) {

    checkNotNull(parentResourceName);
    ResourceCollectionId resourceCollectionId = getResourceType().getResourceCollectionId(
        parentResourceName);
    checkSupported(resourceCollectionId, ResourceMethod.LIST);
    P parentResource = getParent(parentResourceName);
    return ImmutableList.copyOf(list(parentResource, resourceCollectionId, params));
  }

  @Override
  public final C create(String parentResourceName, C newResource) {
    checkNotNull(parentResourceName);
    checkNotNull(newResource);
    ResourceCollectionId resourceCollectionId = getResourceType().getResourceCollectionId(
        parentResourceName);
    checkSupported(resourceCollectionId, ResourceMethod.CREATE);
    P parentResource = getParent(parentResourceName);
    return create(parentResource, resourceCollectionId, newResource);
  }

  @Override
  public final C update(String parentResourceName, String childResourceName, C updatedResource) {
    checkNotNull(parentResourceName);
    checkNotNull(childResourceName);
    checkNotNull(updatedResource);
    ResourceId resourceId = getResourceId(parentResourceName, childResourceName);
    checkSupported(resourceId, ResourceMethod.UPDATE);
    P parentResource = getParent(parentResourceName);
    return update(parentResource, resourceId, updatedResource);
  }

  @Override
  public final void delete(String parentResourceName, String childResourceName) {
    checkNotNull(parentResourceName);
    checkNotNull(childResourceName);
    ResourceId resourceId = getResourceId(parentResourceName, childResourceName);
    checkSupported(resourceId, ResourceMethod.DELETE);
    P parentResource = getParent(parentResourceName);
    delete(parentResource, resourceId);
  }

  protected final ResourceId getResourceId(String parentResourceName, String childResourceName) {
    return getResourceType().getResourceId(parentResourceName, childResourceName);
  }

  /**
   * Required implementation by subclasses. Must turn a parent resource name into a parent
   * resource.
   */
  protected abstract P getParent(String resourceName);

  /**
   * Helper method for {@link #get(String, String, Multimap)} that uses {@link ResourceId}
   * instead of {@link String} resource names and the parent resource has already been resolved.
   * This method is only called when the {@link ResourceMethod#GET} method is supported.
   */
  protected abstract C get(
      P parentResource,
      ResourceId resourceId,
      Multimap<String, String> params);

  /**
   * Helper method for {@link #list(String, Multimap)} that uses {@link ResourceCollectionId}
   * instead of {@link String} resource names and the parent resource has already been resolved.
   * This method is only called when the {@link ResourceMethod#LIST} method is supported.
   */
  protected abstract List<? extends C> list(
      P parentResource,
      ResourceCollectionId resourceCollectionId,
      Multimap<String, String> params);

  /**
   * Helper method for {@link #create(String, ExternalResource)} that uses
   * {@link ResourceCollectionId} instead of {@link String} resource names and the parent
   * resource has already been resolved.
   * This method is only called when the {@link ResourceMethod#CREATE} method is supported.
   */
  protected abstract C create(
      P parentResource,
      ResourceCollectionId resourceCollectionId,
      C childResource);

  /**
   * Helper method for {@link #update(String, String, ExternalResource)} where the parent
   * resource has already been resolved.
   * This method is only called when the {@link ResourceMethod#CREATE} method is supported.
   */
  protected abstract C update(
      P parentResource,
      ResourceId childResourceId,
      C childResource);

  /**
   * Helper method for {@link #delete(String, String)} that uses {@link ResourceId} instead of
   * {@link String} resource names and the parent resource has already been resolved.
   * This method is only called when the {@link ResourceMethod#DELETE} method is supported.
   */
  protected abstract void delete(
      P parentResource,
      ResourceId childResourceId);
}
