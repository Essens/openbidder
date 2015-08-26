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

package com.google.openbidder.ui.resource.impl;

import com.google.api.services.compute.model.AttachedDisk;
import com.google.api.services.compute.model.Disk;
import com.google.api.services.compute.model.Instance;
import com.google.api.services.compute.model.Operation;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.openbidder.ui.compute.BidderInstanceBuilder;
import com.google.openbidder.ui.compute.ComputeClient;
import com.google.openbidder.ui.compute.ComputeService;
import com.google.openbidder.ui.compute.ComputeUtils;
import com.google.openbidder.ui.compute.LoadBalancerInstanceBuilder;
import com.google.openbidder.ui.compute.ResourceName;
import com.google.openbidder.ui.compute.exception.ComputeResourceNotFoundException;
import com.google.openbidder.ui.compute.exception.QuotaExceededException;
import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.notify.NotificationService;
import com.google.openbidder.ui.notify.Topic;
import com.google.openbidder.ui.project.ProjectService;
import com.google.openbidder.ui.project.ProjectUser;
import com.google.openbidder.ui.project.exception.NoProjectWriteAccessException;
import com.google.openbidder.ui.resource.InstanceResourceService;
import com.google.openbidder.ui.resource.exception.BadRequestException;
import com.google.openbidder.ui.resource.model.InstanceResource;
import com.google.openbidder.ui.resource.support.AbstractComputeGrandChildResourceService;
import com.google.openbidder.ui.resource.support.InstanceStatus;
import com.google.openbidder.ui.resource.support.InstanceType;
import com.google.openbidder.ui.resource.support.ResourceCollectionId;
import com.google.openbidder.ui.resource.support.ResourceId;
import com.google.openbidder.ui.resource.support.ResourceMethod;
import com.google.openbidder.ui.resource.support.ResourceType;
import com.google.openbidder.util.Clock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * A {@link com.google.openbidder.ui.resource.ResourceService}
 * for {@link com.google.openbidder.ui.resource.model.InstanceResource}s.
 */
public class InstanceResourceServiceImpl
    extends AbstractComputeGrandChildResourceService<InstanceResource>
    implements InstanceResourceService {

  private static final Logger logger =
      LoggerFactory.getLogger(InstanceResourceServiceImpl.class);

  private static final String STATUS_RUNNING = "RUNNING";
  private static final String DISK_PREFIX = "disk";
  private static final String DISK_TYPE = "PERSISTENT";
  private static final String DISK_KIND = "compute#attachedDisk";
  //TODO(jnwang): we should provide more flexible options in the future.
  private static final String DISK_MODE = "READ_WRITE";

  private final Clock clock;
  private final NotificationService notificationService;
  private final BidderInstanceBuilder bidderInstanceBuilder;
  private final LoadBalancerInstanceBuilder loadBalancerInstanceBuilder;
  private final long diskTimeoutMillis;
  private final long instanceTimeoutMillis;

  @Inject
  public InstanceResourceServiceImpl(
      Clock clock,
      ProjectService projectService,
      ComputeService computeService,
      NotificationService notificationService,
      BidderInstanceBuilder bidderInstanceBuilder,
      LoadBalancerInstanceBuilder loadBalancerInstanceBuilder,
      @Value("${Management.Disk.TimeOutMs}") long diskTimeoutMillis,
      @Value("${Management.Instance.TimeOutMs}") long instanceTimeoutMillis) {

    super(projectService,
        computeService,
        ResourceType.INSTANCE,
        EnumSet.of(ResourceMethod.GET, ResourceMethod.CREATE, ResourceMethod.DELETE));
    this.clock = clock;
    this.notificationService = Preconditions.checkNotNull(notificationService);
    this.bidderInstanceBuilder = Preconditions.checkNotNull(bidderInstanceBuilder);
    this.loadBalancerInstanceBuilder = Preconditions.checkNotNull(loadBalancerInstanceBuilder);
    this.diskTimeoutMillis = diskTimeoutMillis;
    this.instanceTimeoutMillis = instanceTimeoutMillis;
  }

  @Override
  protected InstanceResource get(
      ComputeClient computeClient,
      ProjectUser projectUser,
      ResourceId instanceId,
      Multimap<String, String> params) {

    String zoneName = instanceId.getParentResource().getResourceName();
    Instance instance = computeClient.getInstance(zoneName, instanceId.getResourceName());
    return InstanceResource.build(projectUser.getProject(), instanceId.getParent(), instance);
  }

  /**
   * Create a new instance and return it.
   * <p>
   * Note: passing the {@code machineType} field in will update the relevant field of the
   * associated {@link Project} (either {@link Project#getBidderMachineType(String)} or
   * {@link Project#getLoadBalancerMachineType(String)}), otherwise that field will be used.
   */
  @Override
  protected InstanceResource create(
      ComputeClient computeClient,
      ProjectUser projectUser,
      final ResourceCollectionId resourceCollectionId,
      InstanceResource newInstanceResource) {

    final Project project = projectUser.getProject();
    if (!projectUser.getUserRole().getProjectRole().isWrite()) {
      throw new NoProjectWriteAccessException(project.getId(), projectUser.getEmail());
    }
    if (Strings.isNullOrEmpty(project.getNetworkName())) {
      throw new BadRequestException("network");
    }

    ResourceName image = ResourceName.buildName(
        project,
        ComputeUtils.findImageResourceType(newInstanceResource.getImage()),
        newInstanceResource.getImage().getResourceName());
    if (newInstanceResource.getInstanceType() == InstanceType.BIDDER) {
      return createInstance(projectUser, computeClient, resourceCollectionId,
          buildBidder(projectUser, newInstanceResource),
          image.getResourceUrl());
    } else if (newInstanceResource.getInstanceType() == InstanceType.BALANCER) {
      return createInstance(projectUser, computeClient, resourceCollectionId,
          buildBalancer(projectUser, newInstanceResource),
          image.getResourceUrl());
    } else {
      throw new BadRequestException("instanceType");
    }
  }

  @Override
  protected void delete(
      ComputeClient computeClient,
      ProjectUser projectUser,
      ResourceId instanceId) {

    if (!projectUser.getUserRole().getProjectRole().isWrite()) {
      throw new NoProjectWriteAccessException(
          projectUser.getProject().getId(),
          projectUser.getEmail());
    }

    Project project = projectUser.getProject();
    String zoneName = instanceId.getParentResource().getResourceName();
    String networkName = project.getNetworkName();
    if (networkName == null) {
      ResourceName instanceResourceName = ResourceName.buildName(
          project.getApiProjectId(), zoneName, instanceId);
      throw new ComputeResourceNotFoundException(instanceResourceName);
    }

    long projectId = project.getId();
    String instanceName = instanceId.getResourceName();
    logger.info("Terminating instance {} for project {} for {}",
        instanceName, projectId, projectUser.getEmail());

    Instance instance;
    try {
      instance = computeClient.getInstance(zoneName, instanceName);
      logger.info("Found instance {}", instance);
    } catch (ComputeResourceNotFoundException e) {
      logger.info("{} {} not found", e.getResourceType().getTitle(), instanceName);
      InstanceResource instanceResource = new InstanceResource();
      instanceResource.setId(instanceId);
      instanceResource.setStatus(InstanceStatus.TERMINATED.toString());
      notificationService.notify(projectId, Topic.INSTANCE, instanceResource);
      return;
    }

    InstanceResource instanceResource = InstanceResource.build(
        project, instanceId.getParent(), instance);
    instanceResource.setStatus(InstanceStatus.STOPPING.toString());
    notificationService.notify(projectId, Topic.INSTANCE, instanceResource);

    try {
      Operation finalOperation = computeClient.deleteInstanceAndWait(
          zoneName, instanceName, instanceTimeoutMillis);
      logger.info("Final operation {}", finalOperation);
    } catch (ComputeResourceNotFoundException e) {
      logger.info("Instance already deleted", e);
    }

    // This unnecessary instance creation is to get around an annoying limitation with late
    // verification in Mockito for which there is no good answer otherwise.
    // See http://code.google.com/p/mockito/issues/detail?id=126
    instanceResource = InstanceResource.build(project, instanceId.getParent(), instance);
    instanceResource.setStatus(InstanceStatus.TERMINATED.toString());
    notificationService.notify(projectId, Topic.INSTANCE, instanceResource);
  }

  private InstanceResource createInstance(ProjectUser projectUser, ComputeClient computeClient,
      final ResourceCollectionId resourceCollectionId, Instance instance, String imageUrl) {
    final Project project = projectUser.getProject();
    Function<Instance, InstanceResource> instanceToResource =
        new Function<Instance, InstanceResource>() {
          @Override public InstanceResource apply(Instance instance) {
            return InstanceResource.build(project, resourceCollectionId, instance);
          }};
    String zoneName = resourceCollectionId.getParent().getResourceName();

    String diskName = String.format("%s-%s-%s", DISK_PREFIX, zoneName, clock.now().getMillis());
    Operation startOperation = computeClient.createDisk(
        new Disk().setZone(zoneName).setName(diskName), imageUrl);
    try {
      computeClient.awaitCompletion(startOperation, diskTimeoutMillis, zoneName);
    } catch (ComputeResourceNotFoundException e) {
      logger.info("Error creating root persistent disk for instance {} in zone {}: {}",
          instance.getName(), zoneName, e.getMessage());
      throw e;
    }
    return startInstance(project, computeClient, instance,
        computeClient.getDisk(zoneName, diskName), instanceToResource);
  }

  private Instance buildBidder(
      ProjectUser projectUser,
      final InstanceResource instanceResource) {

    ProjectUser updatedProjectUser = projectUser;
    long projectId = projectUser.getProject().getId();
    ResourceId projectResourceId = ResourceType.PROJECT.getResourceId(Long.toString(projectId));
    ResourceId zoneId = instanceResource.getZone();
    if (zoneId == null
        || zoneId.getResourceType() != ResourceType.ZONE
        || !zoneId.isChildOf(projectResourceId)) {
      throw new BadRequestException("zone");
    }

    Project project = projectUser.getProject();
    final String zoneName = zoneId.getResourceName();
    if (project.getBidderMachineType(zoneName) == null
        || project.getBidderImage() == null
        || instanceResource.getMachineType() != null
        || instanceResource.getImage() != null) {

      ResourceId machineTypeFromProject = getIdOrNull(
          ResourceType.MACHINE_TYPE,
          Long.toString(projectId),
          zoneName,
          project.getBidderMachineType(zoneName));
      final ResourceId machineType = getFirstNonNull(
          "machineType", instanceResource.getMachineType(), machineTypeFromProject);
      if (machineType.getResourceType() != ResourceType.MACHINE_TYPE
          || !machineType.isChildOf(zoneId)) {
        throw new BadRequestException("machineType");
      }

      ResourceId imageFromProject = getIdOrNull(
          project.getIsBidderImageDefault()
              ? ResourceType.DEFAULT_IMAGE
              : ResourceType.CUSTOM_IMAGE,
          Long.toString(projectId),
          project.getBidderImage());
      final ResourceId image = getFirstNonNull(
          "image", instanceResource.getImage(), imageFromProject);
      if ((image.getResourceType() != ResourceType.DEFAULT_IMAGE
          && image.getResourceType() != ResourceType.CUSTOM_IMAGE)
          || !image.isChildOf(projectResourceId)) {
        throw new BadRequestException("image");
      }

      project = getProjectService().updateProject(projectId,
          new Function<Project, Project>() {
            @Override
            public Project apply(Project project) {
              project.setBidderMachineType(zoneName, machineType.getResourceName());
              project.setBidderImage(image.getResourceName());
              project.setIsBidderImageDefault(isImageDefault(image));
              return project;
            }
          });
      updatedProjectUser = projectUser.updateProject(project);
    }

    String instanceName = String.format("%s-%s",
        instanceResource.getInstanceType().getInstanceType(), clock.now().getMillis());
    return bidderInstanceBuilder.build(
        updatedProjectUser, zoneId, instanceName, instanceResource.getCustomBidderResource());
  }

  private Instance buildBalancer(
      ProjectUser projectUser,
      final InstanceResource instanceResource) {

    ProjectUser updatedProjectUser = projectUser;
    long projectId = projectUser.getProject().getId();
    ResourceId projectResourceId = ResourceType.PROJECT.getResourceId(Long.toString(projectId));
    ResourceId zoneId = instanceResource.getZone();
    if (zoneId == null
        || zoneId.getResourceType() != ResourceType.ZONE
        || !zoneId.isChildOf(projectResourceId)) {
      throw new BadRequestException("zone");
    }

    Project project = projectUser.getProject();
    final String zoneName = zoneId.getResourceName();
    if (project.getLoadBalancerMachineType(zoneName) == null
        || project.getLoadBalancerImage() == null
        || instanceResource.getMachineType() != null
        || instanceResource.getImage() != null) {

      ResourceId machineTypeFromProject = getIdOrNull(
          ResourceType.MACHINE_TYPE,
          Long.toString(projectId),
          zoneName,
          project.getLoadBalancerMachineType(zoneName));
      final ResourceId machineType = getFirstNonNull(
          "machineType", instanceResource.getMachineType(), machineTypeFromProject);
      if (machineType.getResourceType() != ResourceType.MACHINE_TYPE
          || !machineType.isChildOf(zoneId)) {
        throw new BadRequestException("machineType");
      }

      ResourceId imageFromProject = getIdOrNull(
          project.getIsLoadBalancerImageDefault()
              ? ResourceType.DEFAULT_IMAGE
              : ResourceType.CUSTOM_IMAGE,
          Long.toString(projectId),
          project.getLoadBalancerImage());
      final ResourceId image = getFirstNonNull(
          "image", instanceResource.getImage(), imageFromProject);
      if ((image.getResourceType() != ResourceType.DEFAULT_IMAGE
          && image.getResourceType() != ResourceType.CUSTOM_IMAGE)
          || !image.isChildOf(projectResourceId)) {
        throw new BadRequestException("image");
      }
      project = getProjectService().updateProject(projectId,
          new Function<Project, Project>() {
            @Override
            public Project apply(Project project) {
              project.setLoadBalancerMachineType(zoneName, machineType.getResourceName());
              project.setLoadBalancerImage(image.getResourceName());
              project.setIsLoadBalancerImageDefault(isImageDefault(image));
              return project;
            }
          });
      updatedProjectUser = projectUser.updateProject(project);
    }

    String instanceName = String.format("%s-%s",
        instanceResource.getInstanceType().getInstanceType(), clock.now().getMillis());
    return loadBalancerInstanceBuilder.build(updatedProjectUser, zoneId, instanceName);
  }

  @SuppressWarnings("null")
  private InstanceResource startInstance(
      Project project,
      ComputeClient computeClient,
      Instance instance,
      Disk disk,
      Function<Instance, InstanceResource> instanceToResource) {

    String instanceName = instance.getName();
    String zoneName = ResourceName.parseResource(instance.getZone()).getResourceName();
    logger.info("Starting instance {} in zone {}", instanceName, zoneName);
    long projectId = project.getId();
    Operation startOperation;

    List<AttachedDisk> disks = ImmutableList.of(
        new AttachedDisk()
            .setDeviceName(disk.getName())
            .setType(DISK_TYPE)
            .setSource(Preconditions.checkNotNull(disk.getSelfLink()))
            .setKind(DISK_KIND)
            .setMode(DISK_MODE)
            .set("zone", disk.getZone())
            .setAutoDelete(true)
            .setBoot(true)
    );
    instance.setDisks(disks);
    logger.info("Attaching persistent disk {} to instance {}", disk.getName(), instanceName);

    try {
      startOperation = computeClient.createInstance(instance);
      publishInstance(projectId, instanceToResource.apply(instance), InstanceStatus.STARTING);
    } catch (ComputeResourceNotFoundException e) {
      logger.info("Error starting instance {} in zone {}: {}",
          instanceName, zoneName, e.getMessage());
      publishInstance(projectId, instanceToResource.apply(instance), InstanceStatus.ERROR);
      throw e;
    }

    logger.info("Waiting for instance {} in zone {}", instanceName, zoneName);
    try {
      computeClient.awaitCompletion(startOperation, instanceTimeoutMillis, zoneName);
    } catch (ComputeResourceNotFoundException | QuotaExceededException e) {
      logger.info("Error creating instance {} in zone {}: {}",
          instanceName, zoneName, e.getMessage());
      publishInstance(projectId, instanceToResource.apply(instance), InstanceStatus.ERROR);
      throw e;
    }

    // Wait for the instance to be in a RUNNING state, checking every second. Publish
    // messages whenever the state changes.
    logger.info("Instance {} started for zone {}", instanceName, zoneName);
    String publishedStatus = null;
    Instance finalInstance = null;
    while (!Thread.currentThread().isInterrupted()
        && !STATUS_RUNNING.equals(publishedStatus)) {

      try {
        finalInstance = computeClient.getInstance(zoneName, instanceName);
        if (publishedStatus == null || !publishedStatus.equals(finalInstance.getStatus())) {
          logger.info("Instance {} is at {}", finalInstance.getName(), finalInstance.getStatus());
          publishInstance(projectId, instanceToResource.apply(finalInstance));
          publishedStatus = finalInstance.getStatus();
        }
      } catch (ComputeResourceNotFoundException e) {
        // it's possible to get timing errors such that the resource isn't found. ignore them
        if (!e.getResourceName().equals(instanceName)) {
          throw e;
        }
      }
      try {
        TimeUnit.SECONDS.sleep(1);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    return instanceToResource.apply(finalInstance);
  }

  private void publishInstance(
      long projectId,
      InstanceResource instanceResource,
      InstanceStatus instanceStatus) {

    instanceResource.setStatus(instanceStatus.toString());
    publishInstance(projectId, instanceResource);
  }

  private void publishInstance(
      long projectId,
      InstanceResource instanceResource) {

    notificationService.notify(projectId, Topic.INSTANCE, instanceResource);
  }

  /**
   * @return {@code true} if this image is a default one, otherwise {@code false}.
   */
  private boolean isImageDefault(ResourceId resourceId) {
    return resourceId.getResourceType().equals(ResourceType.DEFAULT_IMAGE);
  }

  /**
   * Find the first non-null object.
   */
  @SafeVarargs
  private static <T> T getFirstNonNull(String fieldName, T... values) {
    for (T value : values) {
      if (value != null) {
        return value;
      }
    }
    throw new BadRequestException(fieldName);
  }

  private static @Nullable ResourceId getIdOrNull(ResourceType resourceType, String... values) {
    for (String value : values) {
      if (value == null) {
        return null;
      }
    }
    return resourceType.getResourceId(values);
  }
}
