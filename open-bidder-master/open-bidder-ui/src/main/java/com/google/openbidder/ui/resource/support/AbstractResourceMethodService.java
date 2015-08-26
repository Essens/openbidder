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
import com.google.common.collect.Sets;
import com.google.openbidder.ui.resource.exception.ResourceMethodNotAllowedException;

import java.util.EnumSet;
import java.util.Set;

/**
 * Adds capability for {@link ResourceMethod} support and error handling for calling
 * unsupported methods.
 */
public abstract class AbstractResourceMethodService {

  private static final EnumSet<ResourceMethod> ID_METHODS = EnumSet.of(
      ResourceMethod.GET,
      ResourceMethod.UPDATE,
      ResourceMethod.DELETE);
  private static final EnumSet<ResourceMethod> COLLECTION_METHODS = EnumSet.of(
      ResourceMethod.LIST,
      ResourceMethod.CREATE);

  private final ResourceType resourceType;
  private final EnumSet<ResourceMethod> supportedMethods;
  private final EnumSet<ResourceMethod> nameSupportedMethods;
  private final EnumSet<ResourceMethod> setSupportedMethods;

  protected AbstractResourceMethodService(
      ResourceType resourceType,
      Set<ResourceMethod> supportedMethods) {

    this.resourceType = Preconditions.checkNotNull(resourceType);
    this.supportedMethods = Sets.newEnumSet(supportedMethods, ResourceMethod.class);
    Preconditions.checkArgument(!this.supportedMethods.contains(null));
    this.nameSupportedMethods = Sets.newEnumSet(
        Sets.intersection(this.supportedMethods, ID_METHODS), ResourceMethod.class);
    this.setSupportedMethods = Sets.newEnumSet(
        Sets.intersection(this.supportedMethods, COLLECTION_METHODS), ResourceMethod.class);
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
