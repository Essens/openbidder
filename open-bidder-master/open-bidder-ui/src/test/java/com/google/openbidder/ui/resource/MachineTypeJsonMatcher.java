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

package com.google.openbidder.ui.resource;

import com.google.api.services.compute.model.MachineType;
import com.google.api.services.compute.model.Zone;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.openbidder.ui.resource.support.ResourceId;
import com.google.openbidder.ui.resource.support.ResourceType;

import net.minidev.json.JSONObject;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * Verifies JSON output matches the underlying {@link MachineType}.
 */
public class MachineTypeJsonMatcher extends BaseMatcher<MachineType> {

  private final long projectId;
  private final String zoneName;
  private final MachineType machineType;

  public MachineTypeJsonMatcher(long projectId, Zone zone, MachineType machineType) {
    this.projectId = projectId;
    zoneName = zone.getName();
    this.machineType = Preconditions.checkNotNull(machineType);
  }

  @Override
  public boolean matches(Object other) {
    if (!(other instanceof JSONObject)) {
      return false;
    }
    JSONObject object = (JSONObject) other;
    String resourceName = machineType.getName();
    ResourceId id = ResourceType.MACHINE_TYPE.getResourceId(
        Long.toString(projectId), zoneName, resourceName);
    return Objects.equal(id.getResourceUri(), object.get("id"))
        && Objects.equal(ResourceType.MACHINE_TYPE.getResourceType(), object.get("resourceType"))
        && Objects.equal(resourceName, object.get("resourceName"))
        && Objects.equal(machineType.getDescription(), object.get("description"))
        && Objects.equal(machineType.getGuestCpus(), object.get("guestCpus"))
        && Objects.equal(machineType.getMemoryMb(), object.get("memoryMb"))
        && Objects.equal(machineType.getImageSpaceGb(), object.get("imageSpaceGb"));
  }

  @Override
  public void describeTo(Description description) {
    description.appendValue(machineType);
  }
}
