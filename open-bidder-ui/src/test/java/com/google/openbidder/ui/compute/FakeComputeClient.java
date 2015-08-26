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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;

import com.google.api.client.util.GenericData;
import com.google.api.services.compute.model.AccessConfig;
import com.google.api.services.compute.model.Disk;
import com.google.api.services.compute.model.Firewall;
import com.google.api.services.compute.model.Image;
import com.google.api.services.compute.model.Instance;
import com.google.api.services.compute.model.MachineType;
import com.google.api.services.compute.model.Network;
import com.google.api.services.compute.model.NetworkInterface;
import com.google.api.services.compute.model.Operation;
import com.google.api.services.compute.model.Project;
import com.google.api.services.compute.model.Region;
import com.google.api.services.compute.model.Zone;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.BiMap;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.google.openbidder.ui.compute.exception.ComputeResourceAlreadyExistsException;
import com.google.openbidder.ui.compute.exception.ComputeResourceNotFoundException;
import com.google.openbidder.ui.compute.exception.QuotaExceededException;
import com.google.openbidder.ui.compute.impl.AbstractComputeClient;
import com.google.openbidder.util.Clock;

import org.joda.time.Instant;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

/**
 * Fake implementation of {@link ComputeClient}.
 */
public class FakeComputeClient extends AbstractComputeClient {

  private static final Function<AccessConfig, AccessConfig> ACCESS_CONFIG_SUMMARY =
      new Function<AccessConfig, AccessConfig>() {
        @Override public AccessConfig apply(AccessConfig accessConfig) {
          AccessConfig partialConfig = new AccessConfig();
          partialConfig.setNatIP(accessConfig.getNatIP());
          return partialConfig;
        }};

  private static final Function<NetworkInterface, NetworkInterface> NETWORK_INTERFACE_SUMMARY =
      new Function<NetworkInterface, NetworkInterface>() {
        @Override public NetworkInterface apply(NetworkInterface networkInterface) {
          NetworkInterface partialInterface = new NetworkInterface();
          partialInterface.setNetwork(networkInterface.getNetwork());
          partialInterface.setNetworkIP(networkInterface.getNetworkIP());
          partialInterface.setAccessConfigs(Lists.transform(
              networkInterface.getAccessConfigs(),
              ACCESS_CONFIG_SUMMARY));
          return partialInterface;
        }};

  private static final Function<Instance, Instance> INSTANCE_SUMMARY =
      new Function<Instance, Instance>() {
        @Override public Instance apply(Instance instance) {
          Instance partialInstance = new Instance();
          partialInstance.setName(instance.getName());
          partialInstance.setZone(instance.getZone());
          partialInstance.setTags(instance.getTags());
          partialInstance.setNetworkInterfaces(Lists.transform(
              instance.getNetworkInterfaces(),
              NETWORK_INTERFACE_SUMMARY));
          return partialInstance;
        }};

  private static final AtomicInteger ID = new AtomicInteger(1);

  private static BiMap<ComputeResourceType, ResourceQuotaType> QUOTA_TYPES =
      ImmutableBiMap.<ComputeResourceType, ResourceQuotaType>builder()
          .put(ComputeResourceType.NETWORK, ResourceQuotaType.NETWORKS)
          .build();

  // (ComputeResourceType, Resource Name) -> Resource
  private final Table<ComputeResourceType, String, GenericData> resources =
      HashBasedTable.create();

  // to mimic async Operations
  private final Map<BigInteger, DeferredAction<? extends GenericData>> deferredActions =
      new HashMap<>();

  // resource type specific quotas
  private final Map<ResourceQuotaType, Integer> quotas = new HashMap<>();

  private final Clock clock;

  public FakeComputeClient(long projectId, String apiProjectId, Clock clock) {
    super(projectId, apiProjectId);
    this.clock = checkNotNull(clock);
  }

  @Override
  public boolean resourceExists(ResourceName resourceName) {
    return resourceName.getApiProjectId().equals(getApiProjectId())
        && resources.get(resourceName.getResourceType(), resourceName.getResourceName()) != null;
  }

  @Override
  public Project getProject() {
    return getResource(ComputeResourceType.PROJECT, getApiProjectId());
  }

  @Override
  public Network getNetwork(String networkName) {
    return getResource(ComputeResourceType.NETWORK, networkName);
  }

  @Override
  public Operation createNetwork(Network network) {
    return defer(
        ComputeResourceType.NETWORK.buildName(apiProjectId, network.getName()),
        copy(network));
  }

  @Override
  public Operation deleteNetwork(String networkName) {
    return deferDelete(ComputeResourceType.NETWORK.buildName(apiProjectId, networkName));
  }

  @Override
  public Firewall getFirewall(String firewallName) {
    return getResource(ComputeResourceType.FIREWALL, firewallName);
  }

  @Override
  public ImmutableList<Firewall> listFirewallsForNetwork(String networkName) {
    return getAllResources(ComputeResourceType.FIREWALL);
  }

  @Override
  public Operation createFirewall(Firewall firewall) {
    return defer(
        ComputeResourceType.FIREWALL.buildName(apiProjectId, firewall.getName()),
        copy(firewall));
  }

  @Override
  public Operation updateFirewall(Firewall firewall) {
    return defer(
        ComputeResourceType.FIREWALL.buildName(apiProjectId, firewall.getName()),
        copy(firewall));
  }

  @Override
  public Operation deleteFirewall(String firewallName) {
    return deferDelete(ComputeResourceType.FIREWALL.buildName(apiProjectId, firewallName));
  }

  @Override
  public Instance getInstance(String zoneName, String instanceName) {
    return getResource(ComputeResourceType.INSTANCE, zoneName, instanceName);
  }

  @Override
  public ImmutableList<Instance> listInstanceSummary(String networkName, String zoneName) {
    return ImmutableList.copyOf(
        Lists.transform(listInstances(networkName, zoneName), INSTANCE_SUMMARY));
  }

  @Override
  public ImmutableList<Instance> listInstances(final String networkName, final String zoneName) {
    ImmutableList<Instance> instances = getAllResources(ComputeResourceType.INSTANCE);
    return ImmutableList.copyOf(Collections2.filter(instances, new Predicate<Instance>() {
        @Override public boolean apply(Instance instance) {
          ResourceName resourceName = ResourceName.parseResource(instance.getZone());
          return resourceName.getResourceName().equals(zoneName);
        }}));
  }

  @Override
  public Operation createInstance(Instance instance) {
    final String zoneName = ResourceName.parseResource(instance.getZone()).getResourceName();
    return defer(
        ComputeResourceType.INSTANCE.buildName(apiProjectId, zoneName, instance.getName()),
        copy(instance));
  }

  @Override
  public Operation deleteInstance(String zoneName, String instanceName) {
    return deferDelete(ComputeResourceType.INSTANCE.buildName(
        apiProjectId, zoneName, instanceName));
  }

  @Override
  public Disk getDisk(String zoneName, String diskName) {
    return getResource(ComputeResourceType.DISK, zoneName, diskName);
  }

  @Override
  public ImmutableList<Disk> listDisks(final String zoneName) {
    ImmutableList<Disk> disks = getAllResources(ComputeResourceType.DISK);
    return ImmutableList.copyOf(Collections2.filter(disks, new Predicate<Disk>() {
        @Override public boolean apply(Disk disk) {
          ResourceName resourceName = ResourceName.parseResource(disk.getZone());
          return resourceName.getResourceName().equals(zoneName);
        }}));
  }

  @Override
  public Operation createDisk(Disk disk, String imageUrl) {
    final String zoneName = ResourceName.parseResource(disk.getZone()).getResourceName();
    return defer(
        ComputeResourceType.DISK.buildName(apiProjectId, zoneName, disk.getName()),
        copy(disk));
  }

  @Override
  public Operation deleteDisk(String zoneName, String diskName) {
    return deferDelete(ComputeResourceType.DISK.buildName(
        apiProjectId, zoneName, diskName));
  }

  @Override
  public MachineType getMachineType(String zoneName, String machineTypeName) {
    return getResource(ComputeResourceType.MACHINE_TYPE, zoneName, machineTypeName);
  }

  @Override
  public ImmutableList<MachineType> listMachineTypes(final String zoneName) {
    ImmutableList<MachineType> machineTypes = getAllResources(ComputeResourceType.MACHINE_TYPE);
    return ImmutableList.copyOf(Collections2.filter(machineTypes, new Predicate<MachineType>() {
        @Override public boolean apply(MachineType machineType) {
          return machineType.getZone().equals(zoneName);
        }}));
  }

  @Override
  public Region getRegion(String regionName) {
    return getResource(ComputeResourceType.REGION, regionName);
  }

  @Override
  public ImmutableList<Region> listRegions() {
    return getAllResources(ComputeResourceType.REGION);
  }

  @Override
  public Zone getZone(String zoneName) {
    return getResource(ComputeResourceType.ZONE, zoneName);
  }

  @Override
  public ImmutableList<Zone> listZones() {
    return getAllResources(ComputeResourceType.ZONE);
  }

  @Override
  public Image getDefaultImage(String defaultImageName) {
    return getResource(ComputeResourceType.DEFAULT_IMAGE, defaultImageName);
  }

  @Override
  public ImmutableList<Image> listDefaultImages() {
    return getAllResources(ComputeResourceType.DEFAULT_IMAGE);
  }

  @Override
  public Image getCustomImage(String customImageName) {
    return getResource(ComputeResourceType.CUSTOM_IMAGE, customImageName);
  }

  @Override
  public ImmutableList<Image> listCustomImages() {
    return getAllResources(ComputeResourceType.CUSTOM_IMAGE);
  }

  @Override
  public Operation awaitCompletion(
      Operation operation,
      long timeoutMilliseconds,
      @Nullable String parentResourceName) {
    DeferredAction<? extends GenericData> deferredAction =
        deferredActions.remove(operation.getId());
    checkNotNull(deferredAction);
    Instant now = clock.now();
    Operation finalOperation = copy(deferredAction.getOperation());
    finalOperation.setEndTime(now.toString());
    finalOperation.setStatus("DONE");
    GenericData resource = deferredAction.getResource();
    ComputeResourceType resourceType = ComputeResourceType.lookup(operation.getOperationType());
    String shortName = operation.getName();
    ResourceName resourceName = resourceType.buildName(apiProjectId, parentResourceName, shortName);
    if (resource == null) {
      if (resources.remove(resourceType, shortName) == null) {
        throw new ComputeResourceNotFoundException(resourceName);
      }
    } else {
      if (resources.contains(resourceType, shortName)) {
        throw new ComputeResourceAlreadyExistsException(resourceName);
      }
      ResourceQuotaType resourceQuotaType = QUOTA_TYPES.get(resourceType);
      if (resourceQuotaType != null) {
        Integer limit = quotas.get(resourceQuotaType);
        if (limit != null && resources.row(resourceType).size() >= limit) {
          throw new QuotaExceededException(apiProjectId, resourceQuotaType, limit);
        }
      }
      resource.set("id", nextId());
      resource.set("creationTimestamp", now.toString());
      resources.put(resourceType, shortName, resource);
    }
    return finalOperation;
  }

  @SuppressWarnings("unchecked")
  public <T> T getResourceDirect(ComputeResourceType resourceType, String resourceName) {
    checkNotNull(resourceType);
    return (T) resources.get(resourceType, resourceName);
  }

  public void setProject(Project project) {
    resources.put(ComputeResourceType.PROJECT, apiProjectId, project);
  }

  public void addNetwork(Network network) {
    resources.put(ComputeResourceType.NETWORK, network.getName(), network);
  }

  public Network getNetworkDirect(String networkName) {
    return getResourceDirect(ComputeResourceType.NETWORK, networkName);
  }

  public void addFirewall(Firewall firewall) {
    resources.put(ComputeResourceType.FIREWALL, firewall.getName(), firewall);
  }

  public void addAllFirewalls(Firewall... firewalls) {
    for (Firewall firewall : firewalls) {
      addFirewall(firewall);
    }
  }

  public Firewall getFirewallDirect(String firewallName) {
    return getResourceDirect(ComputeResourceType.FIREWALL, firewallName);
  }

  public void addRegion(Region region) {
    resources.put(ComputeResourceType.REGION, region.getName(), region);
  }

  public void addAllRegions(Region... regions) {
    for(Region region : regions) {
      addRegion(region);
    }
  }

  public Instance getInstanceDirect(String instanceName) {
    return getResourceDirect(ComputeResourceType.INSTANCE, instanceName);
  }

  public void addInstance(Instance instance) {
    resources.put(ComputeResourceType.INSTANCE, instance.getName(), instance);
  }

  public void addAllInstances(Instance... instances) {
    for (Instance instance : instances) {
      addInstance(instance);
    }
  }

  public void addMachineType(MachineType machineType) {
    resources.put(ComputeResourceType.MACHINE_TYPE, machineType.getName(), machineType);
  }

  public void addAllMachineTypes(MachineType... machineTypes) {
    for (MachineType machineType : machineTypes) {
      addMachineType(machineType);
    }
  }

  public void addDefaultImage(Image defaultImage) {
    resources.put(ComputeResourceType.DEFAULT_IMAGE, defaultImage.getName(), defaultImage);
  }

  public void addAllDefaultImages(Image... defaultImages) {
    for (Image defaultImage : defaultImages) {
      addDefaultImage(defaultImage);
    }
  }

  public void addZone(Zone zone) {
    resources.put(ComputeResourceType.ZONE, zone.getName(), zone);
  }

  public void addAllZones(Zone... zones) {
    addAllZones(asList(zones));
  }

  public void addAllZones(Iterable<Zone> zones) {
    for (Zone zone : zones) {
      addZone(zone);
    }
  }

  public void clear() {
    resources.clear();
    deferredActions.clear();
  }

  public void setQuota(ComputeResourceType resourceType, int limit) {
    checkNotNull(resourceType);
    ResourceQuotaType resourceQuotaType = QUOTA_TYPES.get(resourceType);
    checkNotNull(resourceQuotaType);
    checkArgument(limit > 0);
    quotas.put(resourceQuotaType, limit);
  }

  public void clearQuota(ComputeResourceType resourceType) {
    checkNotNull(resourceType);
    ResourceQuotaType resourceQuotaType = QUOTA_TYPES.get(resourceType);
    checkNotNull(resourceQuotaType);
    quotas.remove(resourceQuotaType);
  }

  public void clearAllQuotas() {
    quotas.clear();
  }

  private <T> T getResource(
      ComputeResourceType resourceType,
      String parentResourceName,
      String resourceName) {
    @SuppressWarnings("unchecked")
    T resource = (T) getResourceDirect(resourceType, resourceName);
    if (resource == null) {
      throw new ComputeResourceNotFoundException(
          resourceType.buildName(getApiProjectId(), parentResourceName, resourceName));
    }
    return resource;
  }

  private <T> T getResource(ComputeResourceType resourceType, String resourceName) {
    return getResource(resourceType, null, resourceName);
  }

  @SuppressWarnings("unchecked")
  private <T> ImmutableList<T> getAllResources(ComputeResourceType resourceType) {
    return ImmutableList.copyOf((Collection<T>) resources.row(resourceType).values());
  }

  private Operation deferDelete(ResourceName resourceName) {
    return defer(resourceName, /* resource */ null);
  }

  private <T extends GenericData> Operation defer(
      ResourceName resourceName,
      @Nullable T resource,
      Operation.Error.Errors... errors) {

    checkNotNull(resourceName);
    Operation operation = new Operation();
    operation.setId(nextId());
    Instant now = clock.now();
    operation.setCreationTimestamp(now.toString());
    operation.setInsertTime(now.toString());
    operation.setStartTime(now.toString());
    operation.setOperationType(resourceName.getResourceType().getType());
    operation.setName(resourceName.getResourceName());
    if (resource != null) {
      resource.set("selfLink", resourceName.getResourceUrl());
    }
    if (resource instanceof Instance) {
      ((Instance) resource).setStatus("RUNNING");
    }
    Operation copy = copy(operation);
    if (errors.length > 0) {
      Operation.Error error = new Operation.Error();
      error.setErrors(asList(errors));
      operation.setError(error);
    }
    deferredActions.put(operation.getId(), defer(operation, resource));
    return copy;
  }

  private BigInteger nextId() {
    return new BigInteger(Integer.toString(ID.incrementAndGet()));
  }

  private static Operation copy(Operation operation) {
    return copy(Operation.class, operation);
  }

  private static Network copy(Network network) {
    return copy(Network.class, network);
  }

  private static Firewall copy(Firewall firewall) {
    return copy(Firewall.class, firewall);
  }

  private static Instance copy(Instance instance) {
    return copy(Instance.class, instance);
  }

  private static Disk copy(Disk disk) {
    return copy(Disk.class, disk);
  }

  private static <T extends GenericData> T copy(Class<T> klass, T resource) {
    T copy;
    try {
      copy = klass.newInstance();
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
    for (Map.Entry<String, Object> entry : resource.entrySet()) {
      copy.set(entry.getKey(), entry.getValue());
    }
    return copy;
  }

  private static <T extends GenericData> DeferredAction<T> defer(Operation operation, T resource) {
    return new DeferredAction<>(operation, resource);
  }

  private static class DeferredAction<T extends GenericData> {
    private final Operation operation;
    @Nullable
    private final T resource;

    private DeferredAction(Operation operation, @Nullable T resource) {
      this.operation = checkNotNull(operation);
      this.resource = resource;
    }

    public Operation getOperation() {
      return operation;
    }

    @Nullable
    public T getResource() {
      return resource;
    }
  }
}
