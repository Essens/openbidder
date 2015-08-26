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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.resource.support.ResourceType;

import org.junit.Test;

/**
 * Tests for {@link ComputeResourceType}.
 */
public class ComputeResourceTypeTest {

  @Test
  public void buildName_validInstance_returnsCorrectResourceName() {
    Project project = new Project();
    project.setApiProjectId("google.com:test");

    assertEquals("server-1", ComputeResourceType.INSTANCE.buildName(
        project, "rtb-asia-east1-a", "server-1").getResourceName());
  }

  @Test
  public void buildName_validMachineType_returnsCorrectUrl() {
    Project project = new Project();
    project.setApiProjectId("google.com:test");
    String expectedUrl = "https://www.googleapis.com/compute/" + ResourceNameTest.API_TAG
        + "/projects/google.com:test/zones/rtb-asia-east1-a/machineTypes/rtb-standard-1";
    ResourceName actual = ComputeResourceType.MACHINE_TYPE.buildName(
        project, "rtb-asia-east1-a", "rtb-standard-1");
    assertEquals(expectedUrl, actual.getResourceUrl());
  }

  @Test
  public void everyResourceTypeButUserMapsToAComputeResourceType() {
    for (ResourceType resourceType : ResourceType.values()) {
      if (resourceType.isComputeResource()) {
        assertNotNull("Resource type " + resourceType + " has no matching type",
            ComputeResourceType.fromResourceType(resourceType));
      }
    }
  }
}
