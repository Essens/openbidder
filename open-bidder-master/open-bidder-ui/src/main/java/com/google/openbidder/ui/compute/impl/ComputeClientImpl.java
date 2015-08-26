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

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.compute.Compute;
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
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.openbidder.ui.compute.ComputeClient;
import com.google.openbidder.ui.compute.ComputeResourceType;
import com.google.openbidder.ui.compute.ComputeUtils;
import com.google.openbidder.ui.compute.OperationStatus;
import com.google.openbidder.ui.compute.ResourceName;
import com.google.openbidder.ui.compute.exception.ApiProjectNotFoundException;
import com.google.openbidder.ui.compute.exception.ComputeAccessForbiddenException;
import com.google.openbidder.ui.compute.exception.ComputeResourceAlreadyExistsException;
import com.google.openbidder.ui.compute.exception.ComputeResourceNotFoundException;
import com.google.openbidder.ui.compute.exception.ComputeResourceNotReadyException;
import com.google.openbidder.ui.compute.exception.QuotaExceededException;
import com.google.openbidder.ui.compute.exception.UnknownComputeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

/**
 * Default implementation for {@link ComputeClient}.
 */
public class ComputeClientImpl extends AbstractComputeClient {

  private static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
  private static final String ALREADY_EXISTS = "RESOURCE_ALREADY_EXISTS";
  private static final String QUOTA_EXCEEDED = "QUOTA_EXCEEDED";
  private static final String RESOURCE_NOT_READY = "RESOURCE_NOT_READY";

  // instance summary only requires a partial response from listinstances() calls
  private static final String FIELDS_INSTANCE_SUMMARY =
      "items(name,zone,tags,networkInterfaces(accessConfigs/natIP,network,networkIP))";

  private static final Logger logger = LoggerFactory.getLogger(ComputeClient.class);

  private final Compute compute;

  public ComputeClientImpl(
      Compute compute,
      long projectId,
      String apiProjectId) {

    super(projectId, apiProjectId);
    this.compute = Preconditions.checkNotNull(compute);
  }

  @Override
  public boolean resourceExists(final ResourceName resourceName) {
    final String shortName = resourceName.getResourceName();
    if (!apiProjectId.equals(resourceName.getApiProjectId())) {
      throw new ApiProjectNotFoundException(resourceName.getApiProjectId());
    }
    try {
      execute(new Action<Object>() {
        @Override
        public Object execute() throws IOException {
          switch (resourceName.getResourceType()) {
            case CUSTOM_IMAGE:
              return compute.images().get(apiProjectId, shortName).execute();
            case DEFAULT_IMAGE:
              return compute.images().get(
                  ComputeUtils.findStandardImageProjectApi(shortName), shortName).execute();
            case DISK:
              return compute.disks().get(
                  apiProjectId, resourceName.getParentResourceName(), shortName).execute();
            case FIREWALL:
              return compute.firewalls().get(apiProjectId, shortName).execute();
            case INSTANCE:
              return compute.instances().get(
                  apiProjectId, resourceName.getParentResourceName(), shortName).execute();
            case MACHINE_TYPE:
              return compute.machineTypes().get(
                  apiProjectId, resourceName.getParentResourceName(), shortName).execute();
            case NETWORK:
              return compute.networks().get(apiProjectId, shortName).execute();
            case ZONE:
              return compute.zones().get(apiProjectId, shortName).execute();
            case REGION:
              return compute.regions().get(apiProjectId, shortName).execute();
            default:
              throw new IllegalArgumentException("Unknown resource type " +
                  resourceName.getResourceType());
          }
        }
      }, resourceName);
      return true;
    } catch (ComputeResourceNotFoundException e) {
      return false;
    }
  }

  @Override
  public Project getProject() {
    return execute(new Action<Project>() {
      @Override
      public Project execute() throws IOException {
        return compute.projects().get(apiProjectId).execute();
      }
    }, ComputeResourceType.PROJECT, apiProjectId);
  }

  @Override
  public Network getNetwork(final String networkName) {
    return execute(new Action<Network>() {
      @Override
      public Network execute() throws IOException {
        return compute.networks().get(apiProjectId, networkName).execute();
      }
    }, ComputeResourceType.NETWORK, networkName);
  }

  @Override
  public Operation createNetwork(final Network network) {
    return execute(new Action<Operation>() {
      @Override
      public Operation execute() throws IOException {
        return compute.networks().insert(apiProjectId, network).execute();
      }
    }, ComputeResourceType.NETWORK, network.getName());
  }

  @Override
  public Operation deleteNetwork(final String networkName) {
    return execute(new Action<Operation>() {
      @Override
      public Operation execute() throws IOException {
        return compute.networks().delete(apiProjectId, networkName).execute();
      }
    }, ComputeResourceType.NETWORK, networkName);
  }

  @Override
  public Firewall getFirewall(final String firewallName) {
    return execute(new Action<Firewall>() {
      @Override
      public Firewall execute() throws IOException {
        return compute.firewalls().get(apiProjectId, firewallName).execute();
      }
    }, ComputeResourceType.FIREWALL, firewallName);
  }

  @Override
  public List<Firewall> listFirewallsForNetwork(final String networkName) {
    return execute(new Action<List<Firewall>>() {
      @Override
      public List<Firewall> execute() throws IOException {
        List<Firewall> firewalls = compute.firewalls()
            .list(apiProjectId)
            .setFilter(filterEndsWith("network", networkName))
            .execute()
            .getItems();
        return firewalls == null ? new ArrayList<Firewall>() : firewalls;
      }
    }, ComputeResourceType.FIREWALL);
  }

  @Override
  public Operation createFirewall(final Firewall firewall) {
    return execute(new Action<Operation>() {
      @Override
      public Operation execute() throws IOException {
        return compute.firewalls().insert(apiProjectId, firewall).execute();
      }
    }, ComputeResourceType.FIREWALL, firewall.getName());
  }

  @Override
  public Operation updateFirewall(final Firewall firewall) {
    return execute(new Action<Operation>() {
      @Override
      public Operation execute() throws IOException {
        return compute.firewalls().update(apiProjectId, firewall.getName(), firewall).execute();
      }
    }, ComputeResourceType.FIREWALL, firewall.getName());
  }

  @Override
  public Operation deleteFirewall(final String firewallName) {
    return execute(new Action<Operation>() {
      @Override
      public Operation execute() throws IOException {
        return compute.firewalls().delete(apiProjectId, firewallName).execute();
      }
    }, ComputeResourceType.FIREWALL, firewallName);
  }

  @Override
  public Region getRegion(final String regionName) {
    return execute(new Action<Region>() {
      @Override
      public Region execute() throws IOException {
        return compute.regions().get(apiProjectId, regionName).execute();
      }
    }, ComputeResourceType.REGION, regionName);
  }

  @Override
  public List<Region> listRegions() {
    return execute(new Action<List<Region>>() {
      @Override
      public List<Region> execute() throws IOException {
        List<Region> regions = compute.regions().list(apiProjectId).execute().getItems();
        return regions == null ? new ArrayList<Region>() : regions;
      }
    }, ComputeResourceType.REGION);
  }

  @Override
  public Instance getInstance(final String zoneName, final String instanceName) {
    return execute(new Action<Instance>() {
      @Override
      public Instance execute() throws IOException {
        return compute.instances().get(apiProjectId, zoneName, instanceName).execute();
      }
    }, ComputeResourceType.INSTANCE, instanceName, zoneName);
  }

  @Override
  public List<Instance> listInstanceSummary(final String networkName, final String zoneName) {
    return execute(new Action<List<Instance>>() {
      @Override
      public List<Instance> execute() throws IOException {
        List<Instance> instances = compute.instances()
            .list(apiProjectId, zoneName)
            .setFields(FIELDS_INSTANCE_SUMMARY)
            .execute()
            .getItems();
        return instancesForNetwork(instances, networkName);
      }
    }, ComputeResourceType.INSTANCE);
  }

  @Override
  public List<Instance> listInstances(String networkName, final String zoneName) {

    final ResourceName resourceName = getNetworkName(networkName);
    return execute(new Action<List<Instance>>() {
      @Override
      public List<Instance> execute() throws IOException {
        List<Instance> instances = compute.instances()
            .list(apiProjectId, zoneName)
            .execute()
            .getItems();
        if (instances == null) {
          return new ArrayList<>();
        }
        return new ArrayList<>(Collections2.filter(instances,
            new Predicate<Instance>() {
              @Override public boolean apply(Instance instance) {
                return resourceName.equals(getNetworkName(instance));
              }
            }));
      }
    }, ComputeResourceType.INSTANCE);
  }

  @Override
  public Operation createInstance(final Instance instance) {
    Preconditions.checkNotNull(instance);
    final String zoneName = ResourceName.parseResource(instance.getZone()).getResourceName();
    return execute(new Action<Operation>() {
      @Override
      public Operation execute() throws IOException {
        return compute.instances().insert(apiProjectId, zoneName, instance).execute();
      }
    }, ComputeResourceType.INSTANCE, zoneName, instance.getName());
  }

  @Override
  public Operation deleteInstance(final String zoneName, final String instanceName) {
    return execute(new Action<Operation>() {
      @Override
      public Operation execute() throws IOException {
        return compute.instances().delete(apiProjectId, zoneName, instanceName).execute();
      }
    }, ComputeResourceType.INSTANCE, instanceName, zoneName);
  }

  @Override
  public Operation deleteDisk(final String zoneName, final String diskName) {
    return execute(new Action<Operation>() {
      @Override
      public Operation execute() throws IOException {
        return compute.disks().delete(apiProjectId, zoneName, diskName).execute();
      }
    }, ComputeResourceType.DISK, diskName, zoneName);
  }

  @Override
  public Disk getDisk(final String zoneName, final String diskName) {
    return execute(new Action<Disk>() {
      @Override
      public Disk execute() throws IOException {
        return compute.disks().get(apiProjectId, zoneName, diskName).execute();
      }
    }, ComputeResourceType.DISK, diskName, zoneName);
  }

  @Override
  public List<Disk> listDisks(final String diskName) {

    return execute(new Action<List<Disk>>() {
      @Override
      public List<Disk> execute() throws IOException {
        List<Disk> disks = compute.disks()
            .list(apiProjectId, diskName)
            .execute()
            .getItems();
        return disks == null ? new ArrayList<Disk>() : disks;
      }
    }, ComputeResourceType.DISK);
  }

  @Override
  public Operation createDisk(final Disk disk, final String imageUrl) {
    Preconditions.checkNotNull(disk);
    final String zoneName = ResourceName.parseResource(disk.getZone()).getResourceName();
    return execute(new Action<Operation>() {
      @Override
      public Operation execute() throws IOException {
        return compute.disks().insert(apiProjectId, zoneName, disk)
            .setSourceImage(imageUrl).execute();
      }
    }, ComputeResourceType.DISK, zoneName, disk.getName());
  }

  @Override
  public MachineType getMachineType(final String zoneName, final String machineTypeName) {
    return execute(new Action<MachineType>() {
      @Override
      public MachineType execute() throws IOException {
        return compute.machineTypes().get(apiProjectId, zoneName, machineTypeName).execute();
      }
    }, ComputeResourceType.MACHINE_TYPE, zoneName, machineTypeName);
  }

  @Override
  public List<MachineType> listMachineTypes(final String zoneName) {
    return execute(new Action<List<MachineType>>() {
      @Override
      public List<MachineType> execute() throws IOException {
        List<MachineType> machineTypes = compute.machineTypes()
            .list(apiProjectId, zoneName)
            .execute()
            .getItems();
        if (machineTypes == null) {
          return new ArrayList<>();
        }
        return machineTypes;
      }
    }, ComputeResourceType.MACHINE_TYPE);
  }

  @Override
  public Image getDefaultImage(final String defaultImageName) {
    return execute(new Action<Image>() {
      @Override
      public Image execute() throws IOException {
        return compute.images().get(
            ComputeUtils.findStandardImageProjectApi(defaultImageName), defaultImageName).execute();
      }
    }, ComputeResourceType.DEFAULT_IMAGE, defaultImageName);
  }

  @Override
  public List<Image> listDefaultImages() {
    return execute(new Action<List<Image>>() {
      @Override
      public List<Image> execute() throws IOException {
        Set<Image> defaultImages = new TreeSet<>(ComputeUtils.IMAGE_COMPARATOR);
        for (ComputeUtils.ProjectMapper pm : ComputeUtils.ProjectMapper.values()) {
          if (pm.supported() || ComputeUtils.ProjectMapper.enableUnsupported()) {
            for (Image image : compute.images().list(pm.projectName()).execute().getItems()) {
              if (pm.imageNamePattern().matcher(image.getName()).matches()) {
                defaultImages.add(image);
              }
            }
          }
        }
        return new ArrayList<>(defaultImages);
      }
    }, ComputeResourceType.DEFAULT_IMAGE);
  }

  @Override
  public Image getCustomImage(final String customImageName) {
    return execute(new Action<Image>() {
      @Override
      public Image execute() throws IOException {
        return compute.images().get(apiProjectId, customImageName).execute();
      }
    }, ComputeResourceType.CUSTOM_IMAGE, customImageName);
  }

  @Override
  public List<Image> listCustomImages() {
    return execute(new Action<List<Image>>() {
      @Override
      public List<Image> execute() throws IOException {
        List<Image> customImages = compute.images().list(apiProjectId).execute().getItems();
        return customImages == null ? new ArrayList<Image>() : customImages;
      }
    }, ComputeResourceType.CUSTOM_IMAGE);
  }

  @Override
  public Zone getZone(final String zoneName) {
    return execute(new Action<Zone>() {
      @Override
      public Zone execute() throws IOException {
        return compute.zones().get(apiProjectId, zoneName).execute();
      }
    }, ComputeResourceType.ZONE, zoneName);
  }

  @Override
  public List<Zone> listZones() {
    return execute(new Action<List<Zone>>() {
      @Override
      public List<Zone> execute() throws IOException {
        List<Zone> zones = compute.zones().list(apiProjectId).execute().getItems();
        return zones == null ? new ArrayList<Zone>() : zones;
      }
    }, ComputeResourceType.ZONE);
  }

  @Override
  public Operation awaitCompletion(
      Operation operation,
      long timeoutMilliseconds,
      @Nullable String parentResourceName) {
    long nanoTime = System.nanoTime();
    long endTime = nanoTime + TimeUnit.MILLISECONDS.toNanos(timeoutMilliseconds);
    Preconditions.checkState(endTime >= nanoTime,
        "Nano time %d exceeds end time %d", nanoTime, endTime);
    Operation currentOperation = operation;
    while (!OperationStatus.DONE.toString().equals(currentOperation.getStatus())) {
      if (nanoTime > endTime
          || Thread.currentThread().isInterrupted()) {
        logger.warn("Operation timeout within {} ms ({}): {}",
            timeoutMilliseconds, nanoTime, operation);
        break;
      }
      try {
        if (logger.isDebugEnabled()) {
          logger.debug("Refreshing operation {}", currentOperation);
        }
        if (Strings.isNullOrEmpty(parentResourceName)) {
          currentOperation = compute.globalOperations()
              .get(apiProjectId, currentOperation.getName())
              .execute();
        } else {
          currentOperation = compute.zoneOperations()
              .get(apiProjectId, parentResourceName, currentOperation.getName())
              .execute();
        }
        try {
          TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
      nanoTime = System.nanoTime();
    }
    if (OperationStatus.DONE.toString().equals(currentOperation.getStatus())) {
      logger.info("Operation {} completed", operation);
      if (currentOperation.getError() != null
          && currentOperation.getError().getErrors() != null
          && !currentOperation.getError().getErrors().isEmpty()) {

        for (Operation.Error.Errors error : currentOperation.getError().getErrors()) {
          if (RESOURCE_NOT_FOUND.equals(error.getCode())) {
            throw new ComputeResourceNotFoundException(
                ResourceName.parseFromErrorMessage(error.getMessage()));
          }
          if (ALREADY_EXISTS.equals(error.getCode())) {
            throw new ComputeResourceAlreadyExistsException(
                ResourceName.parseFromErrorMessage(error.getMessage()));
          }
          if (QUOTA_EXCEEDED.equals(error.getCode())) {
            throw QuotaExceededException.parseErrorMessage(apiProjectId, error.getMessage());
          }
          if (RESOURCE_NOT_READY.equals(error.getCode())) {
            throw new ComputeResourceNotReadyException(
                ResourceName.parseFromErrorMessage(error.getMessage()));
          }
        }
      }
    }
    return currentOperation;
  }

  @Nullable
  private ResourceName getNetworkName(Instance instance) {
    List<NetworkInterface> networkInterfaces = instance.getNetworkInterfaces();
    if (networkInterfaces == null || networkInterfaces.isEmpty()) {
      return null;
    }
    String networkName = networkInterfaces.get(0).getNetwork();
    return Strings.isNullOrEmpty(networkName) ? null : ResourceName.parseResource(networkName);
  }

  private <T> T execute(Action<T> action, ComputeResourceType resourceType) {
    return execute(
        action,
        Preconditions.checkNotNull(resourceType),
        /* parent resource name */ null,
        /* short name */ null);
  }

  private <T> T execute(
      Action<T> action,
      ComputeResourceType resourceType,
      @Nullable String shortName) {
    return execute(
        action,
        Preconditions.checkNotNull(resourceType),
        /* parent resource name */ null,
        shortName);
  }

  private <T> T execute(Action<T> action, ResourceName resourceName) {
    Preconditions.checkArgument(apiProjectId.equals(resourceName.getApiProjectId()));
    return execute(
        action,
        resourceName.getResourceType(),
        resourceName.getParentResourceName(),
        resourceName.getResourceName());
  }

  private <T> T execute(
      Action<T> action,
      ComputeResourceType resourceType,
      @Nullable String parentResourceName,
      @Nullable String shortName) {

    try {
      return action.execute();
    } catch (GoogleJsonResponseException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND.value()) {
        ResourceName resourceName = resourceType.buildName(
            apiProjectId, parentResourceName, shortName);
        if (e.getDetails() == null) {
          logger.warn("Resource not found: {}", resourceName);
          throw new ComputeResourceNotFoundException(resourceName);
        }
        logger.warn("Error {}. Resource not found: {}", e.getDetails(), resourceName);
        ResourceName notFoundResource = ResourceName.parseFromErrorMessage(e.getDetails());
        if (notFoundResource.getResourceType() == ComputeResourceType.PROJECT) {
          throw new ApiProjectNotFoundException(notFoundResource.getApiProjectId());
        } else {
          throw new ComputeResourceNotFoundException(notFoundResource);
        }
      }
      if (e.getStatusCode() == HttpStatus.FORBIDDEN.value()) {
        throw new ComputeAccessForbiddenException(apiProjectId);
      }
      throw new UnknownComputeException(apiProjectId, e);
    } catch (IOException e) {
      throw new UnknownComputeException(apiProjectId, e);
    }
  }

  private List<Instance> instancesForNetwork(
      @Nullable List<Instance> instances,
      String networkName) {
    if (instances == null) {
      return new ArrayList<>();
    }
    final ResourceName resourceName = getNetworkName(networkName);
    return new ArrayList<>(Collections2.filter(instances,
        new Predicate<Instance>() {
          @Override
          public boolean apply(Instance instance) {
            return resourceName.equals(getNetworkName(instance));
          }
        }));
  }

  private static String filterEndsWith(String fieldName, String value) {
    return String.format("%s eq '.*%s'", fieldName, value);
  }

  private static interface Action<T> {
    T execute() throws IOException;
  }
}
