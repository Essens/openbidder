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

import com.google.common.base.Objects;

import com.fasterxml.jackson.annotation.JsonValue;

import javax.annotation.Nullable;

/**
 * This represents a specific possibly nested resource.
 * <ul>
 *   <li>/projects/3 -> project with ID 3</li>
 *   <li>/projects/3/zones/rtb-us-west1-a/instances/bidder-11 -> bidder with ID bidder-11
 *       under project with ID 1 in zone rtb-us-west1-a</li>
 * </ul>
 * Every {@link ResourceId} belongs to a {@link ResourceCollectionId}. It may optionally have a
 * parent, which itself is a {@link ResourceId}.
 * <p>
 * {@link ResourceId}s support up to three operations:
 * <ul>
 *   <li>{@link ResourceMethod#GET}: retrieve a specific
 *   resource by unique ID</li>
 *   <li>{@link ResourceMethod#UPDATE}: make an idempotent
 *   update to a specific resource identified by unique ID</li>
 *   <li>{@link ResourceMethod#DELETE}: delete a specific
 *   resource identified by unique ID</li>
 * </ul>
 */
public class ResourceId implements ResourcePath, Comparable<ResourceId> {
  private final ResourceCollectionId parent;
  private final String resourceName;

  public ResourceId(
      ResourceType resourceType,
      String resourceName) {

    this(new ResourceCollectionId(resourceType), resourceName);
  }

  public ResourceId(
      ResourceCollectionId parent,
      String resourceName) {

    this.parent = checkNotNull(parent);
    this.resourceName = checkNotNull(resourceName);
  }

  /**
   * @return Parent {@link ResourceCollectionId} (eg "/projects" for "/projects/15")
   */
  public ResourceCollectionId getParent() {
    return parent;
  }

  /**
   * @return Parent {@link ResourceId} or {@code null} if this resource has no parent (eg
   * "/projects/10" for "/projects/10/firewalls/firewall-123" and {@code null} for "/projects/15")
   */
  @Nullable
  public ResourceId getParentResource() {
    return parent.getParent();
  }

  /**
   * @return {@code true} if {@code resourceId} is a parent resource to this one,
   * otherwise {@code false} (eg "/projects/15/zones/us-east1" is a child of "/projects/15" but
   * "/projects/10/networks/network-1234" is not)
   */
  public boolean isChildOf(ResourceId resourceId) {
    return resourceId.equals(getParentResource());
  }

  /**
   * @return The resource name of this {@link ResourceId} (eg "us-east1" from
   * "/projects/123/zones/us-east1")
   */
  public String getResourceName() {
    return resourceName;
  }

  /**
   * @return {@link ResourceType} of this {@link ResourceId} (eg {@link ResourceType#NETWORK}
   * from "/projects/234/networks/network-123")
   */
  @Override
  public ResourceType getResourceType() {
    return parent.getResourceType();
  }

  @Override
  @JsonValue
  public String getResourceUri() {
    return getParent().getResourceUri() + "/" + resourceName;
  }

  /**
   * @return Child {@link ResourceCollectionId} of this {@link ResourceId}
   * eg "/projects/1", ResourceType.ZONE -> "/projects/1/zone"
   * @throws IllegalArgumentException if {@code resourceType} is not a valid child resource of
   * this id's {@link #getResourceType()}
   */
  public ResourceCollectionId getChildCollection(ResourceType resourceType) {
    return new ResourceCollectionId(this, resourceType);
  }

  /**
   * @return Child {@link ResourceId} with the given name
   * @throws IllegalArgumentException if {@code resourceType} is not a valid child resource of
   * this id's {@link #getResourceType()}
   */
  public ResourceId getChildResource(ResourceType resourceType, String resourceName) {
    return getChildCollection(resourceType).getResourceId(resourceName);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof ResourceId)) {
      return false;
    }
    ResourceId other = (ResourceId) o;
    return Objects.equal(getParent(), other.getParent())
        && Objects.equal(getResourceType(), other.getResourceType())
        && Objects.equal(resourceName, other.resourceName);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getParent(), getResourceType(), resourceName);
  }

  @Override
  public String toString() {
    return getResourceUri();
  }

  @Override
  public int compareTo(ResourceId other) {
    return resourceName.compareTo(other.resourceName);
  }
}
