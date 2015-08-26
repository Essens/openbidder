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

import com.google.common.base.Objects;

import com.fasterxml.jackson.annotation.JsonValue;

import javax.annotation.Nullable;

/**
 * This represents a possibly nested set of resources. For example:
 * <ul>
 *   <li>/projects -> all projects</li>
 *   <li>/projects/1/bidders -> all bidders under project 1</li>
 * </ul>
 * A nested {@link ResourceCollectionId} will have a {@link ResourceId} parent that is a
 * specific resource rather than a category of resources.
 * <p>
 * {@link ResourceCollectionId}s support up to two operations:
 * <ul>
 *   <li>{@link ResourceMethod#LIST}:
 *   list or search operations, possibly paged</li>
 *   <li>{@link ResourceMethod#CREATE}:
 *   create a new resource. The API provider is free to take a suggested ID of the new
 *   resource or create a new one</li>
 * </ul>
 */
public class ResourceCollectionId implements ResourcePath {

  @Nullable
  private final ResourceId parent;
  private final ResourceType resourceType;

  public ResourceCollectionId(ResourceType resourceType) {
    this(/* parent */ null, resourceType);
  }

  public ResourceCollectionId(
      @Nullable ResourceId parent,
      ResourceType resourceType) {

    checkNotNull(resourceType);
    checkArgument(resourceType.isValidParent(parent),
        "resource type %s expects parent of type %s, found %s",
        resourceType,
        resourceType.getParentResourceType(),
        parent == null ? "no parent" : parent.getResourceType());
    this.parent = parent;
    this.resourceType = resourceType;
  }

  /**
   * @return Parent {@link ResourceId} (eg "/projects/15/zones" -> "/projects/15")
   */
  @Nullable
  public ResourceId getParent() {
    return parent;
  }

  /**
   * @return {@link ResourceType} for this {@link ResourceCollectionId} (eg
   * {@link ResourceType#ZONE} for "/projects/10/zones")
   */
  @Override
  public ResourceType getResourceType() {
    return resourceType;
  }

  @Override
  @JsonValue
  public String getResourceUri() {
    String parentNameUri = parent == null ? "" : parent.getResourceUri();
    return parentNameUri + "/" + getResourceType().getResourceType();
  }

  /**
   * @return {@link ResourceId} of a specific resource that belongs to this collection
   * eg "/projects", "1" -> "/projects/1"
   */
  public ResourceId getResourceId(String resourceName) {
    return new ResourceId(this, resourceName);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof ResourceCollectionId)) {
      return false;
    }
    ResourceCollectionId other = (ResourceCollectionId) o;
    return Objects.equal(parent, other.parent)
        && Objects.equal(getResourceType(), other.getResourceType());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(parent, getResourceType());
  }

  @Override
  public String toString() {
    return getResourceUri();
  }
}
