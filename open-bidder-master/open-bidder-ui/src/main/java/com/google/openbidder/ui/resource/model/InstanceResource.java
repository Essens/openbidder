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

package com.google.openbidder.ui.resource.model;

import com.google.api.services.compute.model.Instance;
import com.google.api.services.compute.model.NetworkInterface;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.openbidder.ui.compute.InstanceBuilder;
import com.google.openbidder.ui.compute.ResourceName;
import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.resource.support.ExternalResource;
import com.google.openbidder.ui.resource.support.InstanceType;
import com.google.openbidder.ui.resource.support.ResourceCollectionId;
import com.google.openbidder.ui.resource.support.ResourceId;
import com.google.openbidder.ui.resource.support.ResourceType;
import com.google.openbidder.ui.util.json.InstanceTypeDeserializer;
import com.google.openbidder.ui.util.json.InstantDeserializer;
import com.google.openbidder.ui.util.json.InstantSerializer;
import com.google.openbidder.ui.util.json.ResourceIdDeserializer;
import com.google.openbidder.ui.util.validation.Create;
import com.google.openbidder.ui.util.validation.ResourcePathType;
import com.google.openbidder.ui.util.validation.Uri;
import com.google.openbidder.ui.util.web.WebUtils;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.Instant;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * Represents a project-specific instance.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class InstanceResource extends ExternalResource {

  private InstanceType instanceType;
  private String status;

  @ResourcePathType(type = ResourceType.NETWORK)
  private ResourceId network;

  @NotNull(groups = {Create.class})
  @ResourcePathType(type = ResourceType.ZONE)
  private ResourceId zone;

  @ResourcePathType(type = ResourceType.MACHINE_TYPE)
  private ResourceId machineType;

  private ResourceId image;

  @Uri(scheme = "gs://", message = "must start with 'gs://'")
  private String userDistUri;

  private String internalIp;
  private String externalIp;
  private Instant createdAt;
  private @Nullable CustomBidderResource customBidderResource;

  private boolean hasInstanceType;
  private boolean hasStatus;
  private boolean hasNetwork;
  private boolean hasZone;
  private boolean hasMachineType;
  private boolean hasImage;
  private boolean hasUserDistUri;
  private boolean hasInternalIp;
  private boolean hasExternalIp;
  private boolean hasCreatedAt;
  private boolean hasCustomBidderResource;

  public InstanceType getInstanceType() {
    return instanceType;
  }

  @JsonDeserialize(using = InstanceTypeDeserializer.class)
  public void setInstanceType(InstanceType instanceType) {
    this.instanceType = instanceType;
    hasInstanceType = true;
  }

  public void clearInstanceType() {
    instanceType = null;
    hasInstanceType = false;
  }

  public boolean hasInstanceType() {
    return hasInstanceType;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
    hasStatus = true;
  }

  public void clearStatus() {
    this.status = null;
    hasStatus = false;
  }

  public boolean hasStatus() {
    return hasStatus;
  }

  public ResourceId getNetwork() {
    return network;
  }

  @JsonDeserialize(using = ResourceIdDeserializer.class)
  public void setNetwork(ResourceId network) {
    this.network = network;
    hasNetwork = true;
  }

  public void clearNetwork() {
    network = null;
    hasNetwork = false;
  }

  public boolean hasNetwork() {
    return hasNetwork;
  }

  public ResourceId getZone() {
    return zone;
  }

  @JsonDeserialize(using = ResourceIdDeserializer.class)
  public void setZone(ResourceId zone) {
    this.zone = zone;
    hasZone = true;
  }

  public void clearZone() {
    zone = null;
    hasZone = false;
  }

  public boolean hasZone() {
    return hasZone;
  }

  public ResourceId getMachineType() {
    return machineType;
  }

  @JsonDeserialize(using = ResourceIdDeserializer.class)
  public void setMachineType(ResourceId machineType) {
    this.machineType = machineType;
    hasMachineType = true;
  }

  public void clearMachineType() {
    machineType = null;
    hasMachineType = false;
  }

  public boolean hasMachineType() {
    return hasMachineType;
  }

  public String getInternalIp() {
    return internalIp;
  }

  public ResourceId getImage() {
    return image;
  }

  @JsonDeserialize(using = ResourceIdDeserializer.class)
  public void setImage(ResourceId image) {
    this.image = image;
    hasImage = true;
  }

  public void clearImage() {
    image = null;
    hasImage = false;
  }

  public boolean hasImage() {
    return hasImage;
  }

  public String getUserDistUri() {
    return userDistUri;
  }

  public void setUserDistUri(String userDistUri) {
    this.userDistUri = userDistUri;
    hasUserDistUri = true;
  }

  public void clearUserDistUri() {
    userDistUri = null;
    hasUserDistUri = false;
  }

  public boolean hasUserDistUri() {
    return hasUserDistUri;
  }

  public void setInternalIp(String internalIp) {
    this.internalIp = internalIp;
    hasInternalIp = true;
  }

  public void clearInternalIp() {
    internalIp = null;
    hasInternalIp = false;
  }

  public boolean hasInternalIp() {
    return hasInternalIp;
  }

  public String getExternalIp() {
    return externalIp;
  }

  public void setExternalIp(String externalIp) {
    this.externalIp = externalIp;
    hasExternalIp = true;
  }

  public void clearExternalIp() {
    externalIp = null;
    hasExternalIp = false;
  }

  public boolean hasExternalIp() {
    return hasExternalIp;
  }

  @JsonSerialize(using = InstantSerializer.class)
  public Instant getCreatedAt() {
    return createdAt;
  }

  @JsonDeserialize(using = InstantDeserializer.class)
  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
    hasCreatedAt = true;
  }

  public void clearCreatedAt() {
    createdAt = null;
    hasCreatedAt = false;
  }

  public boolean hasCreatedAt() {
    return hasCreatedAt;
  }

  public @Nullable CustomBidderResource getCustomBidderResource() {
    return customBidderResource;
  }

  public void setCustomBidderResource(@Nullable CustomBidderResource customBidderResource) {
    this.customBidderResource = customBidderResource;
    hasCustomBidderResource = true;
  }

  public boolean hasCustomBidderResource() {
    return hasCustomBidderResource;
  }

  public void clearCustomBidderResource() {
    customBidderResource = null;
    hasCustomBidderResource = false;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        super.hashCode(),
        instanceType,
        status,
        network,
        zone,
        machineType,
        image,
        internalIp,
        externalIp,
        customBidderResource
    );
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof InstanceResource) || !super.equals(o)) {
      return false;
    }
    InstanceResource other = (InstanceResource) o;
    return Objects.equal(instanceType, other.instanceType)
        && Objects.equal(status, other.status)
        && Objects.equal(network, other.network)
        && Objects.equal(zone, other.zone)
        && Objects.equal(machineType, other.machineType)
        && Objects.equal(image, other.image)
        && Objects.equal(userDistUri, other.userDistUri)
        && Objects.equal(internalIp, other.internalIp)
        && Objects.equal(externalIp, other.externalIp)
        && Objects.equal(createdAt, other.createdAt)
        && Objects.equal(customBidderResource, other.customBidderResource)
        && Objects.equal(hasInstanceType, other.hasInstanceType)
        && Objects.equal(hasImage, other.hasImage)
        && Objects.equal(hasUserDistUri, other.hasUserDistUri)
        && Objects.equal(hasStatus, other.hasStatus)
        && Objects.equal(hasNetwork, other.hasNetwork)
        && Objects.equal(hasZone, other.hasZone)
        && Objects.equal(hasMachineType, other.hasMachineType)
        && Objects.equal(hasInternalIp, other.hasInternalIp)
        && Objects.equal(hasExternalIp, other.hasExternalIp)
        && Objects.equal(hasCreatedAt, other.hasCreatedAt)
        && Objects.equal(hasCustomBidderResource, other.hasCustomBidderResource);
  }

  @Override
  protected MoreObjects.ToStringHelper toStringHelper() {
    return super.toStringHelper()
        .add("instanceType", instanceType)
        .add("status", status)
        .add("network", network)
        .add("zone", zone)
        .add("machineType", machineType)
        .add("image", image)
        .add("userDistUri", userDistUri)
        .add("internalIp", internalIp)
        .add("externalIp", externalIp)
        .add("createdAt", createdAt)
        .add("customBidderResource", customBidderResource);
  }

  /**
   * @return {@link InstanceResource} constructed from an {@link Instance}.
   */
  public static InstanceResource build(
      Project project, ResourceCollectionId resourceCollectionId, Instance instance) {
    InstanceType instanceType = Preconditions.checkNotNull(InstanceType.fromInstance(instance));
    return instanceType.build(project, resourceCollectionId, instance);
  }

  public static InstanceResource buildInstance (
      Project project, ResourceCollectionId resourceCollectionId, Instance instance) {

    InstanceResource instanceResource = new InstanceResource();
    configureInstance(project, instanceResource, resourceCollectionId, instance);
    return instanceResource;
  }

  static List<String> getInterceptors(Map<String, String> metadataMap, String id) {
    String metadata = metadataMap.get(id);
    return metadata == null
        ? Collections.<String>emptyList()
        : ImmutableList.copyOf(Splitter.on(',').omitEmptyStrings().split(metadata));
  }

  static ResourceId getImageId(
      Long projectId, Map<String, String> metadataMap, String imageNameId, String imageTypeId) {
    String imageName = metadataMap.get(imageNameId);
    String imageType = metadataMap.get(imageTypeId);
    if (imageName == null || imageType == null) {
      return ResourceType.CUSTOM_IMAGE.getResourceId(Long.toString(projectId), "Unknown");
    } else if (InstanceBuilder.IMAGE_DEFAULT.equals(imageType)) {
      return ResourceType.DEFAULT_IMAGE.getResourceId(Long.toString(projectId), imageName);
    } else {
      return ResourceType.CUSTOM_IMAGE.getResourceId(Long.toString(projectId), imageName);
    }
  }

  protected static void configureInstance(
      Project project,
      InstanceResource instanceResource,
      ResourceCollectionId resourceCollectionId,
      Instance instance) {

    ResourceId zoneId = resourceCollectionId.getParent();
    ResourceId projectId = zoneId.getParentResource();
    if (projectId == null || projectId.getResourceType() != ResourceType.PROJECT) {
      throw new IllegalStateException("Parent should be a project not " + projectId);
    }
    if (instance.getName() != null) {
      instanceResource.setId(resourceCollectionId.getResourceId(instance.getName()));
    }
    instanceResource.setInstanceType(InstanceType.UNKNOWN);
    instanceResource.setStatus(instance.getStatus());
    instanceResource.setDescription(instance.getDescription());
    List<NetworkInterface> networkInterfaces = instance.getNetworkInterfaces();
    if (networkInterfaces != null && !networkInterfaces.isEmpty()) {
      NetworkInterface first = instance.getNetworkInterfaces().get(0);
      String networkName = parseName(
          project.getApiProjectId(), first.getNetwork()).getResourceName();
      Preconditions.checkState(
          networkName.equals(project.getNetworkName()),
          "Expected network name %s, found %s",
          project.getNetworkName(),
          networkName);
      instanceResource.setNetwork(
          projectId.getChildCollection(ResourceType.NETWORK).getResourceId(networkName));
      instanceResource.setInternalIp(first.getNetworkIP());
      if (first.getAccessConfigs() != null && !first.getAccessConfigs().isEmpty()) {
        instanceResource.setExternalIp(first.getAccessConfigs().get(0).getNatIP());
      }
    }
    String zoneName = parseName(project.getApiProjectId(), instance.getZone()).getResourceName();
    instanceResource.setZone(
        projectId.getChildCollection(ResourceType.ZONE).getResourceId(zoneName));
    if (instance.getMachineType() != null) {
      String machineTypeName = parseName(
          project.getApiProjectId(), instance.getMachineType()).getResourceName();
      instanceResource.setMachineType(
          zoneId.getChildCollection(ResourceType.MACHINE_TYPE).getResourceId(machineTypeName));
    }
    instanceResource.setCreatedAt(WebUtils.parse8601(instance.getCreationTimestamp()));
  }

  private static ResourceName parseName(String apiProjectId, String resourceUrl) {
    ResourceName resourceName = ResourceName.parseResource(resourceUrl);
    Preconditions.checkState(resourceName.getApiProjectId().equals(apiProjectId),
        "Resource URL %s had API project ID %s, expected %s",
        resourceUrl,
        resourceName.getApiProjectId(),
        apiProjectId);
    return resourceName;
  }
}
