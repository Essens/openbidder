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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.openbidder.ui.resource.exception.ResourceMethodNotAllowedException;

import java.util.Set;

/**
 * Adds capability for {@link ResourceMethod} support and error handling for calling
 * unsupported methods.
 */
public abstract class AbstractResourceMethodService {

  private static final ImmutableSet<ResourceMethod> ID_METHODS = Sets.immutableEnumSet(
      ResourceMethod.GET,
      ResourceMethod.UPDATE,
      ResourceMethod.DELETE);
  private static final ImmutableSet<ResourceMethod> COLLECTION_METHODS = Sets.immutableEnumSet(
      ResourceMethod.LIST,
      ResourceMethod.CREATE);

  private final ResourceType resourceType;
  private final ImmutableSet<ResourceMethod> supportedMethods;
  private final ImmutableSet<ResourceMethod> nameSupportedMethods;
  private final ImmutableSet<ResourceMethod> setSupportedMethods;

  protected AbstractResourceMethodService(
      ResourceType resourceType,
      Set<ResourceMethod> supportedMethods) {

    this.resourceType = checkNotNull(resourceType);
    this.supportedMethods = ImmutableSet.copyOf(supportedMethods);
    checkArgument(!this.supportedMethods.contains(null));
    this.nameSupportedMethods = ImmutableSet.copyOf(
        Sets.intersection(this.supportedMethods, ID_METHODS));
    this.setSupportedMethods = ImmutableSet.copyOf(
        Sets.intersection(this.supportedMethods, COLLECTION_METHODS));
  }

  /**
   * @return {@link ResourceType} for this service.
   */
  public final ResourceType getResourceType() {
    return resourceType;
  }

  /**
   * @return {@code true} if this service supports the given {@link ResourceMethod},
   * otherwise {@code false}.
   */
  protected final boolean isMethodSupported(ResourceMethod resourceMethod) {
    return supportedMethods.contains(resourceMethod);
  }

  /**
   * Verifies that this service supports the given {@link ResourceMethod}.
   * @throws ResourceMethodNotAllowedException if not supported.
   */
  protected final void checkSupported(
      ResourceId resourceId,
      ResourceMethod method) {

    if (!supportedMethods.contains(method)) {
      throw new ResourceMethodNotAllowedException(
          resourceId,
          method,
          nameSupportedMethods);
    }
  }

  /**
   * Verifies that this service supports the given {@link ResourceMethod}.
   * @throws ResourceMethodNotAllowedException if not supported.
   */
  protected final void checkSupported(
      ResourceCollectionId resourceCollectionId,
      ResourceMethod method) {

    if (!supportedMethods.contains(method)) {
      throw new ResourceMethodNotAllowedException(
          resourceCollectionId,
          method,
          setSupportedMethods);
    }
  }
}
