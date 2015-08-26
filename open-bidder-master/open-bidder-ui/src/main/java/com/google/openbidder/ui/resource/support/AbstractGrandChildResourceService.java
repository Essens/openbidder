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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.openbidder.ui.resource.GrandChildResourceService;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Abstract implementation of {@link GrandChildResourceService} for Google Compute Engine
 * per zone resources.
 */
public abstract class AbstractGrandChildResourceService<P, C extends ExternalResource>
    extends AbstractResourceMethodService
    implements GrandChildResourceService<C> {

  protected AbstractGrandChildResourceService(
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
      String grandChildResourceName,
      Multimap<String, String> params) {

    Preconditions.checkNotNull(parentResourceName);
    Preconditions.checkNotNull(childResourceName);
    Preconditions.checkNotNull(grandChildResourceName);
    ResourceId resourceId = getResourceId(
        Arrays.asList(parentResourceName, childResourceName, grandChildResourceName));
    checkSupported(resourceId, ResourceMethod.GET);
    P parentResource = getParentResource(parentResourceName);
    return get(parentResource, resourceId, params);
  }

  @Override
  public final C create(String parentResourceName, String childResourceName, C newResource) {
    Preconditions.checkNotNull(parentResourceName);
    Preconditions.checkNotNull(childResourceName);
    ResourceCollectionId resourceCollectionId = getResourceType().getResourceCollectionId(
        Arrays.asList(parentResourceName, childResourceName));
    checkSupported(resourceCollectionId, ResourceMethod.CREATE);
    P parentResource = getParentResource(parentResourceName);
    return create(parentResource, resourceCollectionId, newResource);
  }

  @Override
  public final void delete(
      String parentResourceName,
      String childResourceName,
      String grandChildResourceName) {
    Preconditions.checkNotNull(parentResourceName);
    Preconditions.checkNotNull(childResourceName);
    Preconditions.checkNotNull(grandChildResourceName);
    ResourceId resourceId = getResourceId(
        Arrays.asList(parentResourceName, childResourceName, grandChildResourceName));
    checkSupported(resourceId, ResourceMethod.DELETE);
    P parentResource = getParentResource(parentResourceName);
    delete(parentResource, resourceId);
  }

  @Override
  public final List<? extends C> list(
    String parentResourceName,
    String childResourceName,
    Multimap<String, String> params) {
    Preconditions.checkNotNull(parentResourceName);
    Preconditions.checkNotNull(childResourceName);
    ResourceCollectionId resourceCollectionId = getResourceType().getResourceCollectionId(
        parentResourceName, childResourceName);
    checkSupported(resourceCollectionId, ResourceMethod.LIST);
    P parentResource = getParentResource(parentResourceName);
    return ImmutableList.copyOf(list(parentResource, resourceCollectionId, params));
  }

  /**
   * Required implementation by subclasses. Must turn a parent resource name into a parent
   * resource.
   */
  protected abstract P getParentResource(String resourceName);

  /**
   * Helper method for {@link #get(String, String, String, Multimap)} that uses {@link ResourceId}
   * instead of {@link String} parent, child and grandchild resource names that have already been
   * resolved. This method is only called when the {@link ResourceMethod#GET} method is supported.
   */
  @SuppressWarnings("unused")
  protected C get(
      P parentResource,
      ResourceId resourceId,
      Multimap<String, String> params) {

    throw new UnsupportedOperationException("get");
  }


  /**
   * Helper method for {@link #list(String, String, Multimap)} that uses
   * {@link ResourceCollectionId} instead of {@link String} resource names and the parent
   * resource has already been resolved.
   * This method is only called when the {@link ResourceMethod#LIST} method is supported.
   */
  @SuppressWarnings("unused")
  protected List<? extends C> list(
      P parentResource,
      ResourceCollectionId resourceCollectionId,
      Multimap<String, String> params) {

    throw new UnsupportedOperationException("list");
  }

  /**
   * Helper method for {@link #create(String, String, ExternalResource)} that uses
   * {@link ResourceCollectionId} instead of {@link String} resource names and the parent
   * resource has already been resolved.
   * This method is only called when the {@link ResourceMethod#CREATE} method is supported.
   */
  @SuppressWarnings("unused")
  protected C create(
      P parentResource,
      ResourceCollectionId resourceCollectionId,
      C grandChildResource) {

    throw new UnsupportedOperationException("create");
  }

  /**
   * Helper method for {@link #delete(String, String, String)} that uses {@link ResourceId}
   * instead of {@link String} root, pareant and child resource names have already been resolved.
   * This method is only called when the {@link ResourceMethod#DELETE} method is supported.
   */
  @SuppressWarnings("unused")
  protected void delete(
      P parentResource,
      ResourceId grandChildResourceId) {

    throw new UnsupportedOperationException("delete");
  }

  protected final ResourceId getResourceId(List<String> resourceNames) {
    return getResourceType().getResourceId(resourceNames);
  }
}
