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

import com.google.api.services.compute.model.MachineType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.openbidder.ui.compute.ComputeClient;
import com.google.openbidder.ui.compute.ComputeService;
import com.google.openbidder.ui.project.ProjectService;
import com.google.openbidder.ui.project.ProjectUser;
import com.google.openbidder.ui.resource.MachineTypeResourceService;
import com.google.openbidder.ui.resource.model.MachineTypeResource;
import com.google.openbidder.ui.resource.support.AbstractComputeGrandChildResourceService;
import com.google.openbidder.ui.resource.support.ResourceCollectionId;
import com.google.openbidder.ui.resource.support.ResourceId;
import com.google.openbidder.ui.resource.support.ResourceMethod;
import com.google.openbidder.ui.resource.support.ResourceType;

import java.util.EnumSet;

import javax.inject.Inject;

/**
 * A {@link com.google.openbidder.ui.resource.ResourceService}
 * for {@link com.google.openbidder.ui.resource.model.MachineTypeResource}s.
 */
public class MachineTypeResourceServiceImpl
    extends AbstractComputeGrandChildResourceService<MachineTypeResource>
    implements MachineTypeResourceService {

  @Inject
  public MachineTypeResourceServiceImpl(
      ProjectService projectService,
      ComputeService computeService) {

    super(projectService,
        computeService,
        ResourceType.MACHINE_TYPE,
        EnumSet.of(ResourceMethod.GET, ResourceMethod.LIST));
  }

  @Override
  protected MachineTypeResource get(
      ComputeClient computeClient,
      ProjectUser projectUser,
      ResourceId resourceId,
      Multimap<String, String> params) {

    String zoneName = resourceId.getParentResource().getResourceName();
    MachineType machineType = computeClient.getMachineType(zoneName, resourceId.getResourceName());
    return build(resourceId.getParent(), machineType);
  }

  @Override
  protected ImmutableList<MachineTypeResource> list(
      ComputeClient computeClient,
      ProjectUser projectUser,
      final ResourceCollectionId resourceCollectionId,
      Multimap<String, String> params) {

    String zoneName = resourceCollectionId.getParent().getResourceName();
    ImmutableList.Builder<MachineTypeResource> machineTypeResources = ImmutableList.builder();
    for (MachineType machineType : computeClient.listMachineTypes(zoneName)) {
      if (machineType.getDeprecated() == null) {
        machineTypeResources.add(build(resourceCollectionId, machineType));
      }
    }
    return machineTypeResources.build();
  }

  private static MachineTypeResource build(
      ResourceCollectionId resourceCollectionId,
      MachineType machineType) {

    MachineTypeResource machineTypeResource = new MachineTypeResource();
    machineTypeResource.setId(resourceCollectionId.getResourceId(machineType.getName()));
    machineTypeResource.setDescription(machineType.getDescription());
    machineTypeResource.setGuestCpus(machineType.getGuestCpus());
    machineTypeResource.setMemoryMb(machineType.getMemoryMb());
    machineTypeResource.setImageSpaceGb(machineType.getImageSpaceGb());
    return machineTypeResource;
  }
}
