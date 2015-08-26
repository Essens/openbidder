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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.api.services.compute.model.Firewall;
import com.google.api.services.compute.model.Network;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.openbidder.ui.compute.ComputeClient;
import com.google.openbidder.ui.compute.ComputeResourceType;
import com.google.openbidder.ui.compute.ComputeService;
import com.google.openbidder.ui.compute.FirewallBuilder;
import com.google.openbidder.ui.compute.NetworkBuilder;
import com.google.openbidder.ui.compute.ResourceName;
import com.google.openbidder.ui.compute.exception.ComputeResourceNotFoundException;
import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.notify.NotificationService;
import com.google.openbidder.ui.notify.Topic;
import com.google.openbidder.ui.project.ProjectService;
import com.google.openbidder.ui.project.ProjectUser;
import com.google.openbidder.ui.project.exception.NoProjectWriteAccessException;
import com.google.openbidder.ui.resource.NetworkResourceService;
import com.google.openbidder.ui.resource.model.FirewallResource;
import com.google.openbidder.ui.resource.model.NetworkResource;
import com.google.openbidder.ui.resource.support.AbstractComputeResourceService;
import com.google.openbidder.ui.resource.support.ResourceCollectionId;
import com.google.openbidder.ui.resource.support.ResourceId;
import com.google.openbidder.ui.resource.support.ResourceMethod;
import com.google.openbidder.ui.resource.support.ResourceType;
import com.google.openbidder.ui.util.web.WebUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * A {@link com.google.openbidder.ui.resource.ResourceService} for {@link NetworkResource}s.
 */
public class NetworkResourceServiceImpl
    extends AbstractComputeResourceService<NetworkResource>
    implements NetworkResourceService {

  private static final Logger logger = LoggerFactory.getLogger(NetworkResourceServiceImpl.class);

  private static final String FIREWALLS = "firewalls";

  private final NotificationService notificationService;
  private final NetworkBuilder networkBuilder;
  private final FirewallBuilder firewallBuilder;
  private final long computeTimeoutMillis;

  @Inject
  public NetworkResourceServiceImpl(
      ProjectService projectService,
      ComputeService computeService,
      NotificationService notificationService,
      NetworkBuilder networkBuilder,
      FirewallBuilder firewallBuilder,
      @Value("${Management.Compute.TimeOutMs}") long computeTimeoutMillis) {

    super(ResourceType.NETWORK,
        EnumSet.complementOf(EnumSet.of(ResourceMethod.UPDATE)),
        projectService,
        computeService);
    this.notificationService = checkNotNull(notificationService);
    this.networkBuilder = checkNotNull(networkBuilder);
    this.firewallBuilder = checkNotNull(firewallBuilder);
    this.computeTimeoutMillis = computeTimeoutMillis;
  }

  @Override
  protected NetworkResource get(
      ComputeClient computeClient,
      ProjectUser projectUser,
      ResourceId resourceId,
      Multimap<String, String> params) {

    if (!resourceId.getResourceName().equals(projectUser.getProject().getNetworkName())) {
      throw new ComputeResourceNotFoundException(
          ResourceName.buildName(
              projectUser.getProject(),
              ComputeResourceType.NETWORK,
              resourceId.getResourceName()));
    }
    return getNetwork(projectUser.getProject(), computeClient, params.containsKey(FIREWALLS));
  }

  @Override
  protected List<NetworkResource> list(
      ComputeClient computeClient,
      ProjectUser projectUser,
      ResourceCollectionId resourceCollectionId,
      Multimap<String, String> params) {

    if (projectUser.getProject().getNetworkName() == null) {
      return new ArrayList<>();
    }
    try {
      return Collections.singletonList(getNetwork(
          projectUser.getProject(), computeClient, params.containsKey(FIREWALLS)));
    } catch (ComputeResourceNotFoundException e) {
      return new ArrayList<>();
    }
  }

  @Override
  protected NetworkResource create(
      ComputeClient computeClient,
      ProjectUser projectUser,
      ResourceCollectionId resourceCollectionId,
      final NetworkResource newNetworkResource) {

    Project project = projectUser.getProject();
    long projectId = project.getId();
    if (!projectUser.getUserRole().getProjectRole().isWrite()) {
      throw new NoProjectWriteAccessException(projectId, projectUser.getEmail());
    }

    // create and retrieve network
    Network network = networkBuilder.build(projectUser);
    final String networkName = network.getName();
    final boolean preexistingNetwork = computeClient.networkExists(networkName);
    computeClient.createNetworkIfDoesNotExist(network, computeTimeoutMillis);
    Network createdNetwork = computeClient.getNetwork(networkName);

    // update project network and whitelisted IP ranges
    project = getProjectService().updateProject(
        projectUser.getProject().getId(),
        new Function<Project, Project>() {
          @Override public Project apply(@Nullable Project project) {
            if (preexistingNetwork) {
              if (!Objects.equal(project.getLoadBalancerRequestPort(),
                      newNetworkResource.getLoadBalancerRequestPort())
                  || !Objects.equal(project.getLoadBalancerStatPort(),
                      newNetworkResource.getLoadBalancerStatPort())
                  || !Objects.equal(project.getBidderRequestPort(),
                      newNetworkResource.getBidderRequestPort())
                  || !Objects.equal(project.getBidderAdminPort(),
                      newNetworkResource.getBidderAdminPort())) {
                logger.info("Ignoring modified ports");
              }
            } else {
              project.setLoadBalancerRequestPort(newNetworkResource.getLoadBalancerRequestPort());
              project.setLoadBalancerStatPort(newNetworkResource.getLoadBalancerStatPort());
              project.setBidderRequestPort(newNetworkResource.getBidderRequestPort());
              project.setBidderAdminPort(newNetworkResource.getBidderAdminPort());
            }

            project.setWhiteListedIpRanges(newNetworkResource.getWhiteListedIpRanges());
            project.setNetworkName(networkName);
            return project;
          }
        });
    ProjectUser updatedProjectUser = projectUser.updateProject(project);

    // publish network creation
    notificationService.notify(projectId, Topic.NETWORK,
        build(project, resourceCollectionId, createdNetwork));
    logger.info("Created Compute network {}", networkName);

    // create and publish firewall creation
    List<FirewallResource> firewallResources = configureFirewall(
        updatedProjectUser,
        computeClient,
        ResourceType.PROJECT.getResourceId(Long.toString(projectId)),
        createdNetwork);

    NetworkResource networkResource = build(project, resourceCollectionId, createdNetwork);
    networkResource.setFirewalls(firewallResources);
    return networkResource;
  }

  @Override
  protected void delete(
      ComputeClient computeClient,
      ProjectUser projectUser,
      ResourceId projectNetworkId) {

    if (!projectUser.getUserRole().getProjectRole().isWrite()) {
      throw new NoProjectWriteAccessException(
          projectUser.getProject().getId(),
          projectUser.getEmail());
    }
    String networkName = projectUser.getProject().getNetworkName();
    if (networkName == null || !networkName.equals(projectNetworkId.getResourceName())) {
      throw new ComputeResourceNotFoundException(
          ResourceName.buildName(projectUser.getProject().getApiProjectId(), projectNetworkId));
    }
    computeClient.deleteNetworkAndWait(networkName, computeTimeoutMillis);
  }

  private NetworkResource getNetwork(
      final Project project,
      ComputeClient computeClient,
      boolean includeFirewalls) {

    ResourceId projectResourceId = ResourceType.PROJECT.getResourceId(
        Long.toString(project.getId()));
    ResourceCollectionId networkCollection = projectResourceId.getChildCollection(
        ResourceType.NETWORK);
    final ResourceCollectionId firewallCollection = projectResourceId.getChildCollection(
        ResourceType.FIREWALL);
    Network network = computeClient.getNetwork(project.getNetworkName());
    NetworkResource networkResource = build(project, networkCollection, network);
    if (includeFirewalls) {
      List<Firewall> firewalls = computeClient.listFirewallsForNetwork(network.getName());
      networkResource.setFirewalls(Lists.transform(
          firewalls,
          new Function<Firewall, FirewallResource>() {
            @Override public FirewallResource apply(Firewall firewall) {
              return FirewallResource
                  .build(project.getApiProjectId(), firewallCollection, firewall);
            }}));
    }
    return networkResource;
  }

  private List<FirewallResource> configureFirewall(
      ProjectUser projectUser,
      ComputeClient computeClient,
      ResourceId projectResourceId,
      Network network) {

    Project project = projectUser.getProject();
    Map<String, Firewall> firewalls = firewallBuilder.build(project, network);
    List<FirewallResource> firewallResources = new ArrayList<>();
    for (Map.Entry<String, Firewall> entry : firewalls.entrySet()) {
      String firewallName = entry.getKey();
      Firewall firewall = entry.getValue();
      if (firewall == null) {
        deleteFirewall(computeClient, projectResourceId, firewallName);
      } else {
        firewallResources.add(createOrUpdateFirewall(
            projectUser, computeClient, projectResourceId, firewall));
      }
    }
    return firewallResources;
  }

  private void deleteFirewall(
      ComputeClient computeClient,
      ResourceId projectResourceId,
      String firewallName) {

    logger.info("Deleting firewall {} for project {} ({})",
        firewallName, computeClient.getProjectId(), computeClient.getApiProjectId());
    try {
      computeClient.deleteFirewallAndWait(firewallName, computeTimeoutMillis);
      notificationService.notify(computeClient.getProjectId(), Topic.FIREWALL_DELETE,
          projectResourceId.getChildResource(ResourceType.FIREWALL, firewallName));
      logger.info("Deleted firewall {} for project {} ({})",
          firewallName, computeClient.getProjectId(), computeClient.getApiProjectId());
    } catch (ComputeResourceNotFoundException e) {
      logger.info("Firewall {} for project {} ({}) already deleted",
          firewallName, computeClient.getProjectId(), computeClient.getApiProjectId());
    }
  }

  private FirewallResource createOrUpdateFirewall(
      ProjectUser projectUser,
      ComputeClient computeClient,
      ResourceId projectResourceId,
      Firewall firewall) {

    logger.info("Updating firewall {} for project {} ({})",
        firewall.getName(), computeClient.getProjectId(), computeClient.getApiProjectId());
    Firewall createdFirewall = computeClient.createOrUpdateFirewall(firewall, computeTimeoutMillis);
    FirewallResource firewallResource = FirewallResource.build(
        projectUser.getProject().getApiProjectId(),
        projectResourceId.getChildCollection(ResourceType.FIREWALL),
        createdFirewall);
    notificationService.notify(computeClient.getProjectId(), Topic.FIREWALL, firewallResource);
    logger.info("Updated firewall {} for project {} ({})",
        firewall.getName(), computeClient.getProjectId(), computeClient.getApiProjectId());
    return firewallResource;
  }

  private static NetworkResource build(
      Project project,
      ResourceCollectionId resourceCollectionId,
      Network network) {

    NetworkResource networkResource = new NetworkResource();
    networkResource.setId(resourceCollectionId.getResourceId(network.getName()));
    networkResource.setDescription(network.getDescription());
    networkResource.setIpv4Range(network.getIPv4Range());
    networkResource.setCreatedAt(WebUtils.parse8601(network.getCreationTimestamp()));
    networkResource.setWhiteListedIpRanges(project.getWhiteListedIpRanges());
    networkResource.setLoadBalancerRequestPort(project.getLoadBalancerRequestPort());
    networkResource.setLoadBalancerStatPort(project.getLoadBalancerStatPort());
    networkResource.setBidderRequestPort(project.getBidderRequestPort());
    networkResource.setBidderAdminPort(project.getBidderAdminPort());
    return networkResource;
  }
}
