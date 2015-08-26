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

import com.google.api.services.compute.model.Disk;
import com.google.api.services.compute.model.Firewall;
import com.google.api.services.compute.model.Image;
import com.google.api.services.compute.model.Instance;
import com.google.api.services.compute.model.MachineType;
import com.google.api.services.compute.model.Network;
import com.google.api.services.compute.model.Operation;
import com.google.api.services.compute.model.Project;
import com.google.api.services.compute.model.Region;
import com.google.api.services.compute.model.Zone;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Abstracts operations to Google Compute Engine on behalf of a user for a given project.
 */
public interface ComputeClient {

  long getProjectId();

  String getApiProjectId();

  // resources

  boolean resourceExists(
      ComputeResourceType computeResourceType,
      String parentResourceName,
      String resourceName);

  boolean resourceExists(ComputeResourceType computeResourceType, String resourceName);

  boolean resourceExists(ResourceName resourceName);

  boolean resourceExists(String resourceUrl);

  ResourceName getResourceName(
      ComputeResourceType resourceType,
      String parentResourceName,
      String resourceName);

  ResourceName getResourceName(ComputeResourceType resourceType, String resourceName);

  // projects

  Project getProject();

  // networks

  boolean networkExists(String networkName);

  ResourceName getNetworkName(String networkName);

  Network getNetwork(String networkName);

  Operation createNetwork(Network network);

  Operation createNetworkAndWait(Network network, long timeoutMilliseconds);

  Network createNetworkIfDoesNotExist(Network network, long timeoutMilliseconds);

  Operation deleteNetwork(String networkName);

  Operation deleteNetworkAndWait(String networkName, long timeoutMilliseconds);

  // firewalls

  boolean firewallExists(String firewallName);

  ResourceName getFirewallName(String firewallName);

  Firewall getFirewall(String firewallName);

  List<Firewall> listFirewallsForNetwork(String networkName);

  Operation createFirewall(Firewall firewall);

  Operation createFirewallAndWait(Firewall firewall, long timeoutMilliseconds);

  Operation updateFirewall(Firewall firewall);

  Operation updateFirewallAndWait(Firewall firewall, long timeoutMilliseconds);

  Firewall createOrUpdateFirewall(Firewall firewall, long timeoutMilliseconds);

  Operation deleteFirewall(String firewallName);

  Operation deleteFirewallAndWait(String firewallName, long timeoutMilliseconds);

  // regions

  boolean regionExists(String regionName);

  ResourceName getRegionName(String regionName);

  Region getRegion(String regionName);

  List<Region> listRegions();

  // instances

  boolean instanceExists(String zoneName, String instanceName);

  ResourceName getInstanceName(String zoneName, String instanceName);

  Instance getInstance(String zoneName, String instanceName);

  List<Instance> listInstanceSummary(String networkName, String zoneName);

  List<Instance> listInstances(String networkName, String zoneName);

  Operation createInstance(Instance instance);

  Operation createInstanceAndWait(Instance instance, long timeoutMilliseconds);

  Operation deleteInstance(String zoneName, String instanceName);

  Operation deleteInstanceAndWait(String zoneName, String instanceName, long timeoutMilliseconds);

  // disks

  boolean diskExists(String zoneName, String diskName);

  ResourceName getDiskName(String zoneName, String diskName);

  Disk getDisk(String zoneName, String diskName);

  List<Disk> listDisks(String zoneName);

  Operation createDisk(Disk disk, String imageUrl);

  Operation createDiskAndWait(Disk disk, String imageUrl, long timeoutMilliseconds);

  Operation deleteDisk(String zoneName, String diskName);

  Operation deleteDiskAndWait(String zoneName, String diskName, long timeoutMilliseconds);

  // machine types

  boolean machineTypeExists(String zoneName, String machineTypeName);

  ResourceName getMachineTypeName(String zoneName, String machineTypeName);

  MachineType getMachineType(String zoneName, String machineTypeName);

  List<MachineType> listMachineTypes(String zoneName);

  // zones

  boolean zoneExists(String zoneName);

  ResourceName getZoneName(String zoneName);

  Zone getZone(String zoneName);

  List<Zone> listZones();

  // default images

  boolean defaultImageExists(String defaultImageName);

  ResourceName getDefaultImageName(String defaultImageName);

  Image getDefaultImage(String defaultImageName);

  List<Image> listDefaultImages();

  // custom images

  boolean customImageExists(String customImageName);

  ResourceName getCustomImageName(String customImageName);

  Image getCustomImage(String customImageName);

  List<Image> listCustomImages();

  // operations

  Operation awaitCompletion(
      Operation operation,
      long timeoutMilliseconds,
      @Nullable String parentResourceName);

  Operation awaitCompletion(Operation operation, long timeoutMilliseconds);
}
