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

package com.google.openbidder.ui.compute;

import static java.util.Arrays.asList;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.resource.support.ResourceType;

import java.util.Map;

import javax.annotation.Nullable;

/**
 * A Compute resource.
 */
public enum ComputeResourceType {
  CUSTOM_IMAGE("customImages", "Custom Image"),
  DEFAULT_IMAGE("defaultImages", "Default Image"),
  DISK("disks", "Disk"),
  FIREWALL("firewalls", "Firewall"),
  INSTANCE("instances", "Instance"),
  MACHINE_TYPE("machineTypes", "Machine type"),
  NETWORK("networks", "Network"),
  PROJECT("projects", "Project"),
  REGION("regions", "Region"),
  ZONE("zones", "Zone");

  private static final Map<ResourceType, ComputeResourceType> MAPPING =
      ImmutableMap.<ResourceType, ComputeResourceType>builder()
          .put(ResourceType.CUSTOM_IMAGE, CUSTOM_IMAGE)
          .put(ResourceType.DEFAULT_IMAGE, DEFAULT_IMAGE)
          .put(ResourceType.DISK, DISK)
          .put(ResourceType.FIREWALL, FIREWALL)
          .put(ResourceType.INSTANCE, INSTANCE)
          .put(ResourceType.MACHINE_TYPE, MACHINE_TYPE)
          .put(ResourceType.NETWORK, NETWORK)
          .put(ResourceType.PROJECT, PROJECT)
          .put(ResourceType.REGION, REGION)
          .put(ResourceType.ZONE, ZONE)
          .build();

  public static final Function<ComputeResourceType, String> GET_NAME =
      new Function<ComputeResourceType, String>() {
        @Override
        public String apply(ComputeResourceType type) {
          return type.getType();
        }
      };

  public static final Map<String, ComputeResourceType> VALUES =
      Maps.uniqueIndex(asList(values()), GET_NAME);

  private final String type;
  private final String title;

  private ComputeResourceType(String type, String title) {
    this.type = type;
    this.title = title;
  }

  public String getType() {
    return type;
  }

  public String getTitle() {
    return title;
  }

  public ResourceName buildName(Project project, String simpleName) {
    return buildName(project, /* parent resource name */ null, simpleName);
  }

  public ResourceName buildName(
      Project project,
      @Nullable String parentResourceName,
      String simpleName) {
    return buildName(project.getApiProjectId(), parentResourceName, simpleName);
  }

  public ResourceName buildName(String apiProjectId, String simpleName) {
    return buildName(apiProjectId, null, simpleName);
  }

  public ResourceName buildName(
      String apiProjectId,
      @Nullable String parentResourceName,
      String simpleName) {
    return new ResourceName(apiProjectId, this, parentResourceName, simpleName);
  }

  /**
   * @return A {@link ComputeResourceType} instance based on its {@link #type} or {@code null} if
   *         none matching
   */
  public static @Nullable ComputeResourceType lookup(String resourceType) {
    return VALUES.get(resourceType);
  }

  /**
   * @return Equivalent {@link ComputeResourceType} to the {@link ResourceType}
   */
  public static ComputeResourceType fromResourceType(ResourceType resourceType) {
    return MAPPING.get(Preconditions.checkNotNull(resourceType));
  }
}
