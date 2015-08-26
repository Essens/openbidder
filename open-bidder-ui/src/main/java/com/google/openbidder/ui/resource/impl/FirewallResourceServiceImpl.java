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

import com.google.api.services.compute.model.Firewall;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.openbidder.ui.compute.ComputeClient;
import com.google.openbidder.ui.compute.ComputeResourceType;
import com.google.openbidder.ui.compute.ComputeService;
import com.google.openbidder.ui.compute.ResourceName;
import com.google.openbidder.ui.compute.exception.ComputeResourceNotFoundException;
import com.google.openbidder.ui.project.ProjectService;
import com.google.openbidder.ui.project.ProjectUser;
import com.google.openbidder.ui.project.exception.NoProjectWriteAccessException;
import com.google.openbidder.ui.resource.FirewallResourceService;
import com.google.openbidder.ui.resource.model.FirewallResource;
import com.google.openbidder.ui.resource.support.AbstractComputeResourceService;
import com.google.openbidder.ui.resource.support.ResourceCollectionId;
import com.google.openbidder.ui.resource.support.ResourceId;
import com.google.openbidder.ui.resource.support.ResourceMethod;
import com.google.openbidder.ui.resource.support.ResourceType;

import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.inject.Inject;

/**
 * A {@link com.google.openbidder.ui.resource.ResourceService}
 * for {@link com.google.openbidder.ui.resource.model.FirewallResource}s.
 */
public class FirewallResourceServiceImpl
    extends AbstractComputeResourceService<FirewallResource>
    implements FirewallResourceService {

  private final long computeTimeoutMillis;

  @Inject
  public FirewallResourceServiceImpl(
      ProjectService projectService,
      ComputeService computeService,
      @Value("${Management.Compute.TimeOutMs}") long computeTimeoutMillis) {

    super(ResourceType.FIREWALL,
        EnumSet.of(ResourceMethod.GET, ResourceMethod.LIST, ResourceMethod.DELETE),
        projectService,
        computeService);
    this.computeTimeoutMillis = computeTimeoutMillis;
  }

  @Override
  protected FirewallResource get(
      ComputeClient computeClient,
      ProjectUser projectUser,
      ResourceId resourceId,
      Multimap<String, String> params) {

    String networkName = projectUser.getProject().getNetworkName();
    ResourceName firewallResourceName = ComputeResourceType.FIREWALL.buildName(
        projectUser.getProject().getApiProjectId(), resourceId.getResourceName());
    if (networkName == null) {
      throw new ComputeResourceNotFoundException(firewallResourceName);
    }
    Firewall firewall = computeClient.getFirewall(resourceId.getResourceName());
    ResourceName networkResourceName = ResourceName.parseResource(firewall.getNetwork());
    if (!networkName.equals(networkResourceName.getResourceName())) {
      throw new ComputeResourceNotFoundException(firewallResourceName);
    }
    return FirewallResource.build(
        projectUser.getProject().getApiProjectId(),
        resourceId.getParent(),
        firewall);
  }

  @Override
  protected List<FirewallResource> list(
      ComputeClient computeClient,
      final ProjectUser projectUser,
      final ResourceCollectionId resourceCollectionId,
      Multimap<String, String> params) {

    String networkName = projectUser.getProject().getNetworkName();
    if (networkName == null) {
      return new ArrayList<>();
    }
    List<Firewall> firewalls = computeClient.listFirewallsForNetwork(networkName);
    return Lists.transform(firewalls, new Function<Firewall, FirewallResource>() {
      @Override public FirewallResource apply(Firewall firewall) {
        return FirewallResource.build(
            projectUser.getProject().getApiProjectId(),
            resourceCollectionId,
            firewall);
      }});
  }

  @Override
  protected void delete(
      ComputeClient computeClient,
      ProjectUser projectUser,
      ResourceId resourceId) {

    if (!projectUser.getUserRole().getProjectRole().isWrite()) {
      throw new NoProjectWriteAccessException(
          projectUser.getProject().getId(),
          projectUser.getEmail());
    }
    String networkName = projectUser.getProject().getNetworkName();
    ResourceName resourceName = ResourceName.buildName(
        projectUser.getProject().getApiProjectId(), resourceId);
    if (networkName == null) {
      throw new ComputeResourceNotFoundException(resourceName);
    }
    Firewall firewall = computeClient.getFirewall(resourceId.getResourceName());
    ResourceName networkResourceName = ResourceName.parseResource(firewall.getNetwork());
    if (!networkName.equals(networkResourceName.getResourceName())) {
      throw new ComputeResourceNotFoundException(resourceName);
    }
    computeClient.deleteFirewallAndWait(resourceId.getResourceName(), computeTimeoutMillis);
  }
}
