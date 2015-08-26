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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.openbidder.ui.resource.RootResourceService;

import java.util.List;
import java.util.Set;

/**
 * Abstract implementation of {@link RootResourceService} for root-level resources that adds
 * supported method validation and converts {@link String} resource names to correct
 * {@link ResourceId} and {@link ResourceCollectionId} instances, as appropriate.
 * <p>
 * Note: all methods are implemented and will throw {@link UnsupportedOperationException}s
 * if called. The intent is that implementing subclasses need only override the supported
 * methods.
 */
public abstract class AbstractRootResourceService<T extends ExternalResource>
    extends AbstractResourceMethodService
    implements RootResourceService<T> {

  protected AbstractRootResourceService(
      ResourceType resourceType,
      Set<ResourceMethod> supportedMethods) {

    super(resourceType, supportedMethods);
    Preconditions.checkArgument(resourceType.getParentResourceType() == null,
        "Resource %s expected no parent, found %s",
        resourceType,
        resourceType.getAncestorResourceTypes());
  }

  @Override
  public final boolean supports(ResourceMethod resourceMethod) {
    return isMethodSupported(resourceMethod);
  }

  @Override
  public final T get(String resourceName, Multimap<String, String> params) {
    Preconditions.checkNotNull(resourceName);
    ResourceId resourceId = getResourceId(resourceName);
    checkSupported(resourceId, ResourceMethod.GET);
    return get(resourceId, params);
  }

  @Override
  public final List<? extends T> list(Multimap<String, String> params) {
    ResourceCollectionId resourceCollectionId = getResourceType().getResourceCollectionId();
    checkSupported(resourceCollectionId, ResourceMethod.LIST);
    return ImmutableList.copyOf(list(resourceCollectionId, params));
  }

  @Override
  public final T create(T newResource) {
    Preconditions.checkNotNull(newResource);
    ResourceCollectionId resourceCollectionId = getResourceType().getResourceCollectionId();
    checkSupported(resourceCollectionId, ResourceMethod.CREATE);
    return create(resourceCollectionId, newResource);
  }

  @Override
  public final T update(String resourceName, T updatedResource) {
    Preconditions.checkNotNull(resourceName);
    Preconditions.checkNotNull(updatedResource);
    ResourceId resourceId = getResourceId(resourceName);
    checkSupported(resourceId, ResourceMethod.UPDATE);
    return update(resourceId, updatedResource);
  }

  @Override
  public final void delete(String resourceName) {
    Preconditions.checkNotNull(resourceName);
    ResourceId resourceId = getResourceId(resourceName);
    checkSupported(resourceId, ResourceMethod.DELETE);
    delete(resourceId);
  }

  protected final ResourceId getResourceId(String resourceName) {
    return getResourceType().getResourceId(resourceName);
  }

  /**
   * Helper implementation of {@link #get(String, Multimap)} that uses a {@link ResourceId}
   * instead. This method is only called when the {@link ResourceMethod#GET} method is supported.
   */
  @SuppressWarnings("unused")
  protected T get(ResourceId resourceId, Multimap<String, String> params) {
    // this method should only be called if the subclass says that
    // ResourceMethod.GET is supported. If so, the subclass should be overriding it
    throw new UnsupportedOperationException();
  }

  /**
   * Helper implementation of {@link #list(Multimap)} that uses a {@link ResourceCollectionId}
   * instead. This method is only called when the {@link ResourceMethod#LIST} method is supported.
   */
  @SuppressWarnings("unused")
  protected List<? extends T> list(
      ResourceCollectionId resourceCollectionId,
      Multimap<String, String> params) {

    // this method should only be called if the subclass says that
    // ResourceMethod.LIST is supported. If so, the subclass should be overriding it
    throw new UnsupportedOperationException();
  }

  /**
   * Helper implementation of {@link #create(ExternalResource)} that uses a
   * {@link ResourceCollectionId} instead.
   * This method is only called when the {@link ResourceMethod#CREATE} method is supported.
   */
  @SuppressWarnings("unused")
  protected T create(ResourceCollectionId resourceCollectionId, T newResource) {
    // this method should only be called if the subclass says that
    // ResourceMethod.CREATE is supported. If so, the subclass should be overriding it
    throw new UnsupportedOperationException();
  }

  /**
   * Helper implementation of {@link #update(String, ExternalResource)} that uses a
   * {@link ResourceId} instead.
   * This method is only called when the {@link ResourceMethod#UPDATE} method is supported.
   */
  @SuppressWarnings("unused")
  protected T update(ResourceId resourceId, T updatedResource) {
    // this method should only be called if the subclass says that
    // ResourceMethod.UPDATE is supported. If so, the subclass should be overriding it
    throw new UnsupportedOperationException();
  }

  /**
   * Helper implementation of {@link #delete(String)} that uses a {@link ResourceId} instead.
   * This method is only called when the {@link ResourceMethod#DELETE} method is supported.
   */
  @SuppressWarnings("unused")
  protected void delete(ResourceId resourceId) {
    // this method should only be called if the subclass says that
    // ResourceMethod.DELETE is supported. If so, the subclass should be overriding it
    throw new UnsupportedOperationException();
  }
}
