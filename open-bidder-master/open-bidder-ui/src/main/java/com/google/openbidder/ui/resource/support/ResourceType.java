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

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import org.codehaus.jackson.annotate.JsonValue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Represents a resource type. This resource type can be nested or top-level. Resource types
 * define two things:
 * <ul>
 *   <li>A {@link ResourceCollectionId}: a set of resources of the same type, possibly with a
 *   specific uniquely identified {@link ResourceId} ancestor
 *   eg /projects (no ancestor), /projects/5/networks (/projects/5 is the ancestor); and</li>
 *   <li>A {@link ResourceId}: a specific unique resource identified by an ID and a path
 *   to any ancestors eg /projects/1, /projects/1/bidders/bidder-1234</li>
 * </ul>
 */
public enum ResourceType {
  PROJECT("projects"),
  // Global resources
  ACCOUNT("accounts", PROJECT, false),
  ACTION("actions", PROJECT, false),
  AD("ads", PROJECT, false),
  AD_GROUP("adgroups", PROJECT, false),
  CAMPAIGN("campaigns", PROJECT, false),
  CUSTOM_IMAGE("customimages", PROJECT),
  DEFAULT_IMAGE("defaultimages", PROJECT),
  FIREWALL("firewalls", PROJECT),
  NETWORK("networks", PROJECT),
  QUOTA("quotas", PROJECT, false),
  REGION("regions", PROJECT),
  REPORT("reports", PROJECT, false),
  USER("users", PROJECT, false),
  ZONE("zones", PROJECT),
  // Per-zone resources
  DISK("disks", ZONE),
  INSTANCE("instances", ZONE),
  MACHINE_TYPE("machinetypes", ZONE);

  private static final Map<String, ResourceType> RESOURCE_TYPES = ImmutableMap.copyOf(
      Maps.uniqueIndex(Arrays.asList(values()), new Function<ResourceType, String>() {
        @Override
        public String apply(ResourceType resourceType) {
          return resourceType.getResourceType();
        }
      }));

  private final ImmutableList<ResourceType> ancestorResourceTypes;
  private final ResourceType parentResourceType;
  private final String resourceType;
  private final boolean computeResource;

  private ResourceType(String resourceType) {
    this(resourceType, /* parent resource type */ null);
  }

  private ResourceType(
      String resourceType,
      @Nullable ResourceType parentResourceType) {

    this(resourceType, parentResourceType, true);
  }

  private ResourceType(
      String resourceType,
      @Nullable ResourceType parentResourceType,
      boolean computeResource) {

    this.resourceType = Preconditions.checkNotNull(resourceType);
    this.parentResourceType = parentResourceType;
    this.computeResource = computeResource;
    if (parentResourceType == null) {
      ancestorResourceTypes = ImmutableList.of();
    } else {
      ancestorResourceTypes = ImmutableList.<ResourceType>builder()
          .addAll(parentResourceType.ancestorResourceTypes)
          .add(parentResourceType)
          .build();
    }
  }

  /**
   * @return {@code true} if the {@code parent} is a valid parent resource type for this
   * resource type, otherwise {@code false}.
   */
  public boolean isValidParent(@Nullable ResourceId parent) {
    return parentResourceType == null ? parent == null
        : parent != null && parentResourceType == parent.getResourceType();
  }

  /**
   * @return {@link String} identifier used in URIs for this resource type
   */
  @JsonValue
  public String getResourceType() {
    return resourceType;
  }

  /**
   * @return Parent {@link ResourceType}, which can be {@code null} for root resource types
   */
  public ResourceType getParentResourceType() {
    return parentResourceType;
  }

  /**
   * @return {@link List} of parent {@link ResourceType}s from root to direct parent. Can be
   * empty but never {@code null}
   */
  public ImmutableList<ResourceType> getAncestorResourceTypes() {
    return ancestorResourceTypes;
  }

  /**
   * @return {@code true} if this represents a Compute Engine resource, otherwise {@code false}.
   */
  public boolean isComputeResource() {
    return computeResource;
  }

  /**
   * @return {@link ResourceCollectionId} for this resource type
   * eg {@code }ResourceType.ZONE.getResourceCollectionId("11")} returns "/projects/11/zones"
   * eg {@code }ResourceType.INSTANCE.getResourceCollectionId("11", "rtb-us-east1-a")}
   *    returns "projects/11/zones/rtb-us-east1-a/instances"
   * @throws IllegalArgumentException if there aren't exactly the right number of ancestor
   * resource names to construct a resource for the required depth of this resource type
   */
  public ResourceCollectionId getResourceCollectionId(String... ancestorResourceNames) {
    return getResourceCollectionId(Arrays.asList(ancestorResourceNames));
  }


  /**
   * @see #getResourceCollectionId(String...)
   */
  public ResourceCollectionId getResourceCollectionId(List<String> ancestorResourceNames) {
    int expectedAncestorDepth = ancestorResourceTypes.size();
    int actualAncestorDepth = ancestorResourceNames.size();
    Preconditions.checkArgument(actualAncestorDepth == expectedAncestorDepth,
        "Expected to find %d ancestor names, found %d",
        expectedAncestorDepth, actualAncestorDepth);

    ResourceId parent = null;
    if (actualAncestorDepth > 0) {
      parent = parentResourceType.getResourceId(ancestorResourceNames);
    }
    return new ResourceCollectionId(parent, this);
  }

  /**
   * @return {@link ResourceId} for this resource type
   * eg {@code }ResourceType.ZONE.getResourceId("11", "rtb1")} returns "/projects/11/zones/rtb1"
   * @throws IllegalArgumentException if there aren't exactly the right number of
   * resource names to construct a resource for the required depth of this resource type
   */
  public ResourceId getResourceId(String... resourceNames) {
    return getResourceId(Arrays.asList(resourceNames));
  }

  /**
   * @see #getResourceId(String...)
   */
  public ResourceId getResourceId(List<String> resourceNames) {
    int resourceNameLast = resourceNames.size() - 1;
    List<String> ancestorResourceNames = resourceNames.subList(0, resourceNameLast);
    String thisResourceName = resourceNames.get(resourceNameLast);
    ResourceCollectionId collectionId = getResourceCollectionId(ancestorResourceNames);
    return new ResourceId(collectionId, thisResourceName);
  }

  /**
   * Convert a URI into a {@link ResourcePath} eg:
   * <ul>
   *   <li>/projects -> ResourceSetId(PROJECT)</li>
   *   <li>/projects/15 -> ResourceNameId(PROJECT, 15)</li>
   *   <li>/projects/15/zones -> ResourceSetId(ResourceNameId(PROJECT, 15), ZONE)</li>
   *   <li>/projects/15/zones/rtb1 -> ResourceNameId(ResourceNameId(PROJECT, 15), ZONE, "rtb")</li>
   * </ul>
   */
  @SuppressWarnings("null")
  public static ResourcePath parseResourceUri(String resourceUri) {
    Preconditions.checkNotNull(resourceUri);
    Iterable<String> parts = Splitter.on('/')
        .omitEmptyStrings()
        .split(resourceUri);
    ResourcePath last = null;
    ResourceId name = null;
    ResourceCollectionId collection = null;
    for (String part : parts) {
      if (last == name) {
        ResourceType resourceType = RESOURCE_TYPES.get(part);
        Preconditions.checkArgument(resourceType != null, "Unknown resource type '%s'", part);
        collection = name == null
            ? resourceType.getResourceCollectionId()
            : name.getChildCollection(resourceType);
        last = collection;
      } else if (last == collection) {
        name = collection.getResourceId(part);
        last = name;
      }
    }
    Preconditions.checkArgument(last != null, "no URI entered");
    return last;
  }
}
