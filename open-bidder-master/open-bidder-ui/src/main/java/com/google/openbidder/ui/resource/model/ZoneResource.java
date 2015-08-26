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

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.openbidder.ui.resource.support.ExternalResource;
import com.google.openbidder.ui.resource.support.InstanceType;
import com.google.openbidder.ui.resource.support.ResourceId;
import com.google.openbidder.ui.util.json.InstanceTypeSerializer;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Represents a single virtual location where bidders and load balancers can run.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZoneResource extends ExternalResource {

  private List<ScheduledOutage> scheduledOutages;
  private List<ResourceId> machineTypes;
  private String hostName;
  private Map<InstanceType, Integer> instanceSummary;
  private Integer instanceCount;
  private List<InstanceResource> instanceResources;
  private Integer maxBidRequestQps;
  private Boolean isRegistered;
  private List<ResourceId> images;
  private String status;

  private boolean hasScheduledOutages;
  private boolean hasMachineTypes;
  private boolean hasHostName;
  private boolean hasInstanceSummary;
  private boolean hasInstanceCount;
  private boolean hasInstances;
  private boolean hasMaxBidRequestQps;
  private boolean hasIsRegistered;
  private boolean hasImages;
  private boolean hasStatus;

  public List<ScheduledOutage> getScheduledOutages() {
    return scheduledOutages;
  }

  public void setScheduledOutages(List<ScheduledOutage> scheduledOutages) {
    this.scheduledOutages = scheduledOutages == null
        ? null
        : ImmutableList.copyOf(scheduledOutages);
    hasScheduledOutages = true;
  }

  public void clearScheduledOutages() {
    scheduledOutages = null;
    hasScheduledOutages = false;
  }

  public boolean hasScheduledOutages() {
    return hasScheduledOutages;
  }

  public List<ResourceId> getMachineTypes() {
    return machineTypes;
  }

  public void setMachineTypes(List<ResourceId> machineTypes) {
    this.machineTypes = machineTypes;
    hasMachineTypes = true;
  }

  public void clearMachineTypes() {
    machineTypes = null;
    hasMachineTypes = false;
  }

  public boolean hasMachineTypes() {
    return hasMachineTypes;
  }

  public String getHostName() {
    return hostName;
  }

  public void setHostName(String hostName) {
    this.hostName = hostName;
    hasHostName = true;
  }

  public void clearHostName() {
    hostName = null;
    hasHostName = false;
  }

  public boolean hasHostName() {
    return hasHostName;
  }

  @JsonSerialize(
      include = JsonSerialize.Inclusion.NON_NULL,
      keyUsing = InstanceTypeSerializer.class)
  public Map<InstanceType, Integer> getInstanceSummary() {
    return instanceSummary;
  }

  public void setInstanceSummary(Map<InstanceType, Integer> instanceSummary) {
    this.instanceSummary = instanceSummary;
    hasInstanceSummary = true;
  }

  public void clearInstanceSummary() {
    instanceSummary = null;
    hasInstanceSummary = false;
  }

  public boolean hasInstanceSummary() {
    return hasInstanceSummary;
  }

  @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
  public Integer getInstanceCount() {
    return instanceCount;
  }

  public void setInstanceCount(Integer instanceCount) {
    this.instanceCount = instanceCount;
    hasInstanceCount = true;
  }

  public void clearInstanceCount() {
    instanceCount = null;
    hasInstanceCount = false;
  }

  public boolean hasInstanceCount() {
    return hasInstanceCount;
  }

  @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
  public List<InstanceResource> getInstances() {
    return instanceResources;
  }

  public void setInstances(List<InstanceResource> instanceResources) {
    this.instanceResources = instanceResources;
    hasInstances = true;
  }

  public void clearInstances() {
    instanceResources = null;
    hasInstances = false;
  }

  public boolean hasInstances() {
    return hasInstances;
  }

  public Integer getMaxBidRequestQps() {
    return maxBidRequestQps;
  }

  public void setMaxBidRequestQps(Integer maxBidRequestQps) {
    this.maxBidRequestQps = maxBidRequestQps;
    hasMaxBidRequestQps = true;
  }

  public void clearMaxBidRequestQps() {
    maxBidRequestQps = null;
    hasMaxBidRequestQps = false;
  }

  public boolean hasMaxBidRequestQps() {
    return hasMaxBidRequestQps;
  }

  public Boolean getIsRegistered() {
    return isRegistered;
  }

  public void setIsRegistered(Boolean isRegistered) {
    this.isRegistered = isRegistered;
    hasIsRegistered = true;
  }

  public void clearIsRegistered() {
    isRegistered = null;
    hasIsRegistered = false;
  }

  public boolean hasIsRegistered() {
    return hasIsRegistered;
  }

  public List<ResourceId> getImages() {
    return images;
  }

  public void setImages(List<ResourceId> images) {
    this.images = images;
    hasImages = true;
  }

  public void clearImages() {
    images = null;
    hasImages = false;
  }

  public boolean hasImages() {
    return hasImages;
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

  @Override
  public int hashCode() {
    return Objects.hashCode(
        super.hashCode(),
        scheduledOutages,
        machineTypes,
        hostName,
        images,
        instanceSummary,
        instanceCount,
        instanceResources,
        maxBidRequestQps,
        isRegistered,
        status
    );
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof ZoneResource) || !super.equals(o)) {
      return false;
    }
    ZoneResource other = (ZoneResource) o;
    return Objects.equal(scheduledOutages, other.scheduledOutages)
        && Objects.equal(machineTypes, other.machineTypes)
        && Objects.equal(hostName, other.hostName)
        && Objects.equal(images, other.images)
        && Objects.equal(instanceSummary, other.instanceSummary)
        && Objects.equal(instanceCount, other.instanceCount)
        && Objects.equal(instanceResources, other.instanceResources)
        && Objects.equal(maxBidRequestQps, other.maxBidRequestQps)
        && Objects.equal(status, other.status)
        && Objects.equal(isRegistered, other.isRegistered)
        && Objects.equal(hasScheduledOutages, other.hasScheduledOutages)
        && Objects.equal(hasMachineTypes, other.hasMachineTypes)
        && Objects.equal(hasHostName, other.hasHostName)
        && Objects.equal(hasImages, other.hasImages)
        && Objects.equal(hasInstanceSummary, other.hasInstanceSummary)
        && Objects.equal(hasInstanceCount, other.hasInstanceCount)
        && Objects.equal(hasInstances, other.hasInstances)
        && Objects.equal(hasMaxBidRequestQps, other.hasMaxBidRequestQps)
        && Objects.equal(hasIsRegistered, other.hasIsRegistered)
        && Objects.equal(hasStatus, other.hasStatus);
  }

  @Override
  protected MoreObjects.ToStringHelper toStringHelper() {
    return super.toStringHelper()
        .add("scheduledOutages", scheduledOutages)
        .add("machineTypes", machineTypes)
        .add("hostName", hostName)
        .add("images", images)
        .add("instanceSummary", instanceSummary)
        .add("instanceCount", instanceCount)
        .add("instanceResources", instanceResources)
        .add("maxBidRequestQps", maxBidRequestQps)
        .add("isRegistered", isRegistered)
        .add("status", status);
  }
}
