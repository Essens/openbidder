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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.services.compute.Compute;
import com.google.api.services.compute.model.Instance;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.project.ProjectUser;
import com.google.openbidder.ui.resource.support.ResourceId;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

/**
 * Google Compute uses names in various forms depending on what API call you're making. This
 * class exists to convert between them. The forms are:
 * <dl>
 *   <dt>Resource URL</dt>
 *   <dd>The URL format depends on the resource type.</dd>
 *   <dd>For global resources eg images, networks, firewalls</dd>
 *   <dd>URL example: https://www.googleapis.com/compute/v1/projects/google.com:open-source-bidder/global/networks/network-0cc37e1147da4215af028632f6960b34</dd>
 *   <dd>For per-zone resources eg instances, machine types</dd>
 *   <dd>URL example: https://www.googleapis.com/compute/v1/projects/google.com:open-source-bidder/zones/rtb-us-east2/instances/load-balancer-1340002895954</dd>
 *   <dt>Long</dt>
 *   <dd>eg projects/google.com:open-source-bidder/zones/rtb-us-east2/instances/load-balancer-1340002895954</dd>
 *   <dt>Simple/short</dt>
 *   <dd>eg load-balancer-1340002895954</dd>
 * </dl>
 */
public class ResourceName {

  private static final Pattern ERROR_PATTERN = Pattern.compile("The resource '(.*)'.*");
  private static final String DEFAULT_BASE_URL =
      Compute.DEFAULT_ROOT_URL + Compute.DEFAULT_SERVICE_PATH;
  private static final String GLOBAL_RESOURCE_SCOPE = "global";
  private static final String PER_ZONE_RESOURCE_SCOPE = "zones";
  private static final String IMAGE_RESOURCE_TYPE_IN_GCE = "images";

  private final String apiProjectId;
  private final ComputeResourceType resourceType;
  @Nullable
  private final String parentResourceName;
  private final String resourceName;

  public ResourceName(
      String apiProjectId,
      ComputeResourceType resourceType,
      @Nullable String parentResourceName,
      String resourceName) {
    this.apiProjectId = checkNotNull(apiProjectId);
    this.resourceType = checkNotNull(resourceType);
    this.parentResourceName = parentResourceName;
    this.resourceName = checkNotNull(resourceName);
  }

  /**
   * @return Google API project ID
   */
  public String getApiProjectId() {
    return apiProjectId;
  }

  /**
   * @return {@link ComputeResourceType} type
   */
  public ComputeResourceType getResourceType() {
    return resourceType;
  }

  /**
   * @return Shortest possible name being simply the {@link #resourceName}
   */
  public String getResourceName() {
    return resourceName;
  }

  /**
   * For global resources, parentResourceName is a null string.
   * For per-zone resources, it is the name of the zone the resources belong to.
   *
   * @return parent resource name
   */
  @Nullable
  public String getParentResourceName() {
    return parentResourceName;
  }

  /**
   * @return Full name including the Compute base URL
   */
  public String getResourceUrl() {
    if (resourceType == ComputeResourceType.PROJECT) {
      return String.format("%s%s", DEFAULT_BASE_URL, apiProjectId);
    } else if (resourceType == ComputeResourceType.ZONE) {
      return String.format("%s%s/%s/%s",
          DEFAULT_BASE_URL,
          apiProjectId,
          resourceType.getType(),
          resourceName
      );

    } else if (Strings.isNullOrEmpty(getParentResourceName())) {
      if (resourceType == ComputeResourceType.DEFAULT_IMAGE) {
        return String.format("%s%s/%s/%s/%s",
            DEFAULT_BASE_URL,
            ComputeUtils.findStandardImageProjectApi(resourceName),
            GLOBAL_RESOURCE_SCOPE,
            IMAGE_RESOURCE_TYPE_IN_GCE,
            resourceName
        );
      } else {
        return String.format("%s%s/%s/%s/%s",
            DEFAULT_BASE_URL,
            apiProjectId,
            GLOBAL_RESOURCE_SCOPE,
            resourceType == ComputeResourceType.CUSTOM_IMAGE
                ? IMAGE_RESOURCE_TYPE_IN_GCE
                : resourceType.getType(),
            resourceName
            );
      }
    }
    return String.format("%s%s/%s/%s/%s/%s",
            DEFAULT_BASE_URL,
            apiProjectId,
            PER_ZONE_RESOURCE_SCOPE,
            parentResourceName,
            resourceType.getType(),
            resourceName);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("apiProjectId", apiProjectId)
        .add("resourceTypeType", resourceType)
        .add("parentResourceName", parentResourceName)
        .add("resourceName", resourceName)
        .toString();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(apiProjectId, resourceType, parentResourceName, resourceName);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof ResourceName)) {
      return false;
    }
    ResourceName otherName = (ResourceName) o;
    return Objects.equal(apiProjectId, otherName.apiProjectId)
        && Objects.equal(resourceType, otherName.resourceType)
        && Objects.equal(parentResourceName, otherName.parentResourceName)
        && Objects.equal(resourceName, otherName.resourceName);
  }

  /**
   * Parse either a full or long Compute resource URL.
   */
  public static ResourceName parseResource(String resourceUrlParam) {
    String resourceUrl;
    if (resourceUrlParam.startsWith(Compute.DEFAULT_ROOT_URL)) {
      resourceUrl = resourceUrlParam.substring(resourceUrlParam.indexOf("/projects/") + 10);
    } else if (resourceUrlParam.startsWith("projects/")) {
      resourceUrl = resourceUrlParam.substring("projects/".length());
    } else {
      resourceUrl = resourceUrlParam;
    }
    String[] parts = resourceUrl.split("/", 5);
    String apiProjectId = parts[0];
    ComputeResourceType computeResourceType;
    String resourceName;
    String parentResourceName = null;

    if (parts.length > 1) {
      if (parts.length == 3) {
        computeResourceType = ComputeResourceType.lookup(parts[1]);
        resourceName = parts[2];
      } else if (parts[1].equals(GLOBAL_RESOURCE_SCOPE)) {
        if (parts[2].equals(IMAGE_RESOURCE_TYPE_IN_GCE)) {
          if (ComputeUtils.ProjectMapper.valueOfProjectName(apiProjectId) != null) {
            computeResourceType = ComputeResourceType.DEFAULT_IMAGE;
          } else {
            computeResourceType = ComputeResourceType.CUSTOM_IMAGE;
          }
        } else {
          computeResourceType = ComputeResourceType.lookup(parts[2]);
        }
        resourceName = parts[3];
      } else {
        parentResourceName = parts[2];
        computeResourceType = ComputeResourceType.lookup(parts[3]);
        resourceName = parts[4];
      }
      checkNotNull(computeResourceType,
          "Unknown compute resource type for resource URL '%s'", resourceUrl);
    } else {
      computeResourceType = ComputeResourceType.PROJECT;
      resourceName = parts[0];
    }
    return new ResourceName(apiProjectId, computeResourceType, parentResourceName, resourceName);
  }

  public static ResourceName buildName(Instance instance) {
    // Instances we create ourselves have no selfLink. It can be derived from the apiProjectId
    // and short name. We get the apiProejctId from the instance's zone
    if (Strings.isNullOrEmpty(instance.getSelfLink())) {
      ResourceName zoneName = parseResource(instance.getZone());
      return new ResourceName(
          zoneName.getApiProjectId(),
          ComputeResourceType.INSTANCE,
          zoneName.getResourceName(),
          instance.getName());
    }
    return parseResource(instance.getSelfLink());
  }

  public static ResourceName buildName(
      Project project,
      ComputeResourceType computeResourceType,
      String simpleName) {
    return buildName(project, computeResourceType, null, simpleName);
  }

  public static ResourceName buildName(
      Project project,
      ComputeResourceType computeResourceType,
      @Nullable String parentResourceName,
      String simpleName) {

    return buildName(
        project.getApiProjectId(),
        computeResourceType,
        parentResourceName,
        simpleName);
  }

  public static ResourceName buildName(
      String apiProjectId,
      ComputeResourceType computeResourceType,
      String simpleName) {
    return buildName(apiProjectId, computeResourceType, null, simpleName);
  }

  public static ResourceName buildName(
      String apiProjectId,
      ComputeResourceType computeResourceType,
      @Nullable String parentResourceName,
      String simpleName) {

    return new ResourceName(apiProjectId, computeResourceType, parentResourceName, simpleName);
  }

  public static ResourceName buildName(ProjectUser projectUser, ResourceId resourceId) {
    return buildName(projectUser.getProject(), null, resourceId);
  }

  public static ResourceName buildName(
      ProjectUser projectUser,
      @Nullable String parentResourceName,
      ResourceId resourceId) {
    return buildName(projectUser.getProject(), parentResourceName, resourceId);
  }

  public static ResourceName buildName(Project project, ResourceId resourceId) {
    return buildName(project, null, resourceId);
  }

  public static ResourceName buildName(
      Project project,
      @Nullable String parentResourceName,
      ResourceId resourceId) {
    return buildName(project.getApiProjectId(), parentResourceName, resourceId);
  }

  public static ResourceName buildName(String apiProjectId, ResourceId resourceId) {
    return buildName(apiProjectId, null, resourceId);
  }

  public static ResourceName buildName(
      String apiProjectId,
      @Nullable String parentResourceName,
      ResourceId resourceId) {
    return buildName(
        apiProjectId,
        ComputeResourceType.fromResourceType(resourceId.getResourceType()),
        parentResourceName,
        resourceId.getResourceName());
  }

  public static @Nullable ResourceName parseFromErrorMessage(GoogleJsonError error) {
    return parseFromErrorMessage(error.getMessage());
  }

  public static @Nullable ResourceName parseFromErrorMessage(String errorMessage) {
    Matcher matcher = ERROR_PATTERN.matcher(errorMessage);
    if (matcher.find()) {
      return parseResource(matcher.group(1));
    }
    return null;
  }
}
