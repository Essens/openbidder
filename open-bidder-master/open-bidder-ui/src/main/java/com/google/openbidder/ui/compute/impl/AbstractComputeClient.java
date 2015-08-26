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

package com.google.openbidder.ui.compute.impl;

import com.google.api.services.compute.model.Disk;
import com.google.api.services.compute.model.Firewall;
import com.google.api.services.compute.model.Instance;
import com.google.api.services.compute.model.Network;
import com.google.api.services.compute.model.Operation;
import com.google.common.base.Preconditions;
import com.google.openbidder.ui.compute.ComputeClient;
import com.google.openbidder.ui.compute.ComputeResourceType;
import com.google.openbidder.ui.compute.ResourceName;
import com.google.openbidder.ui.compute.exception.ComputeResourceAlreadyExistsException;

import javax.annotation.Nullable;

/**
 * Base implementation of {@link ComputeClient}.
 */
public abstract class AbstractComputeClient implements ComputeClient {

  protected final long projectId;
  protected final String apiProjectId;

  public AbstractComputeClient(
      long projectId, String apiProjectId) {
    this.projectId = projectId;
    this.apiProjectId = Preconditions.checkNotNull(apiProjectId);
  }

  @Override
  public final long getProjectId() {
    return projectId;
  }

  @Override
  public final String getApiProjectId() {
    return apiProjectId;
  }

  @Override
  public final ResourceName getResourceName(
      ComputeResourceType resourceType,
      @Nullable String parentResourceName,
      String resourceName) {
    Preconditions.checkNotNull(resourceType);
    Preconditions.checkNotNull(resourceName);
    return resourceType.buildName(apiProjectId, parentResourceName, resourceName);
  }

  @Override
  public final ResourceName getResourceName(ComputeResourceType resourceType, String resourceName) {
    return getResourceName(resourceType, /* parent resource name */ null, resourceName);
  }

  @Override
  public final ResourceName getNetworkName(String networkName) {
    return getResourceName(ComputeResourceType.NETWORK, networkName);
  }

  @Override
  public final ResourceName getFirewallName(String firewallName) {
    return getResourceName(ComputeResourceType.FIREWALL, firewallName);
  }

  @Override
  public final ResourceName getRegionName(String regionName) {
    return getResourceName(ComputeResourceType.REGION, regionName);
  }

  @Override
  public final ResourceName getInstanceName(String zoneName, String instanceName) {
    return getResourceName(ComputeResourceType.INSTANCE, zoneName, instanceName);
  }

  @Override
  public final ResourceName getDiskName(String zoneName, String diskName) {
    return getResourceName(ComputeResourceType.DISK, zoneName, diskName);
  }

  @Override
  public final ResourceName getMachineTypeName(String zoneName, String machineTypeName) {
    return getResourceName(ComputeResourceType.MACHINE_TYPE, zoneName, machineTypeName);
  }

  @Override
  public final ResourceName getDefaultImageName(String defaultImageName) {
    return getResourceName(ComputeResourceType.DEFAULT_IMAGE, defaultImageName);
  }

  @Override
  public final ResourceName getCustomImageName(String customImageName) {
    return getResourceName(ComputeResourceType.CUSTOM_IMAGE, customImageName);
  }

  @Override
  public final ResourceName getZoneName(String zoneName) {
    return getResourceName(ComputeResourceType.ZONE, zoneName);
  }

  @Override
  public final boolean networkExists(String networkName) {
    return resourceExists(ComputeResourceType.NETWORK, networkName);
  }

  @Override
  public final boolean regionExists(String regionName) {
    return resourceExists(ComputeResourceType.REGION, regionName);
  }

  @Override
  public final boolean machineTypeExists(String zoneName, String machineTypeName) {
    return resourceExists(ComputeResourceType.MACHINE_TYPE, zoneName, machineTypeName);
  }

  @Override
  public final boolean defaultImageExists(String defaultImageName) {
    return resourceExists(ComputeResourceType.DEFAULT_IMAGE, defaultImageName);
  }

  @Override
  public final boolean customImageExists(String customImageName) {
    return resourceExists(ComputeResourceType.CUSTOM_IMAGE, customImageName);
  }

  @Override
  public final boolean zoneExists(String zoneName) {
    return resourceExists(ComputeResourceType.ZONE, zoneName);
  }

  @Override
  public final boolean resourceExists(
      ComputeResourceType resourceType,
      @Nullable String parentResourceName,
      String resourceName) {

    return resourceExists(
        new ResourceName(apiProjectId, resourceType, parentResourceName, resourceName));
  }

  @Override
  public final boolean resourceExists(ComputeResourceType resourceType, String resourceName) {
    return resourceExists(resourceType, /* parent resource name */ null, resourceName);
  }

  @Override
  public final boolean resourceExists(String resourceUrl) {
    return resourceExists(ResourceName.parseResource(resourceUrl));
  }

  @Override
  public final Operation createNetworkAndWait(Network network, long timeoutMilliseconds) {
    return awaitCompletion(createNetwork(network), timeoutMilliseconds);
  }

  @Override
  public final Network createNetworkIfDoesNotExist(Network network, long timeoutMilliseconds) {
    if (!networkExists(network.getName())) {
      try {
        createNetworkAndWait(network, timeoutMilliseconds);
      } catch(ComputeResourceAlreadyExistsException e) {
        // ignore
      }
    }
    return getNetwork(network.getName());
  }

  @Override
  public final Operation deleteNetworkAndWait(String networkName, long timeoutMilliseconds) {
    return awaitCompletion(deleteNetwork(networkName), timeoutMilliseconds);
  }

  @Override
  public final boolean firewallExists(String firewallName) {
    return resourceExists(ComputeResourceType.FIREWALL, firewallName);
  }

  @Override
  public final Operation createFirewallAndWait(Firewall firewall, long timeoutMilliseconds) {
    return awaitCompletion(createFirewall(firewall), timeoutMilliseconds);
  }

  @Override
  public final Operation updateFirewallAndWait(Firewall firewall, long timeoutMilliseconds) {
    return awaitCompletion(updateFirewall(firewall), timeoutMilliseconds);
  }

  @Override
  public final Operation deleteFirewallAndWait(String firewallName, long timeoutMilliseconds) {
    return awaitCompletion(deleteFirewall(firewallName), timeoutMilliseconds);
  }

  @Override
  public final boolean instanceExists(String zoneName, String instanceName) {
    return resourceExists(ComputeResourceType.INSTANCE, zoneName, instanceName);
  }

  @Override
  public final Operation createInstanceAndWait(Instance instance, long timeoutMilliseconds) {
    String zoneName = ResourceName.parseResource(instance.getZone()).getResourceName();
    return awaitCompletion(createInstance(instance), timeoutMilliseconds, zoneName);
  }

  @Override
  public final Operation deleteInstanceAndWait(
      String zoneName,
      String instanceName,
      long timeoutMilliseconds) {
    return awaitCompletion(deleteInstance(zoneName, instanceName), timeoutMilliseconds, zoneName);
  }

  @Override
  public final boolean diskExists(String zoneName, String diskName) {
    return resourceExists(ComputeResourceType.DISK, zoneName, diskName);
  }

  @Override
  public final Operation createDiskAndWait(Disk disk, String imageUrl, long timeoutMilliseconds) {
    String zoneName = ResourceName.parseResource(disk.getZone()).getResourceName();
    return awaitCompletion(createDisk(disk, imageUrl), timeoutMilliseconds, zoneName);
  }

  @Override
  public final Operation deleteDiskAndWait(
      String zoneName,
      String diskName,
      long timeoutMilliseconds) {
    return awaitCompletion(deleteDisk(zoneName, diskName), timeoutMilliseconds, zoneName);
  }

  @Override
  public final Firewall createOrUpdateFirewall(final Firewall firewall, long timeoutMilliseconds) {
    if (firewallExists(firewall.getName())) {
      updateFirewallAndWait(firewall, timeoutMilliseconds);
    } else {
      createFirewallAndWait(firewall, timeoutMilliseconds);
    }
    return getFirewall(firewall.getName());
  }

  @Override
  public Operation awaitCompletion(Operation operation, long timeoutMilliseconds) {
    return awaitCompletion(operation, timeoutMilliseconds, /* parent resource name */ null);
  }
}
