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

import org.junit.Test;

/**
 * Tests for {@link ResourceName}.
 */
public class ResourceNameTest {

  public static final String API_TAG = "v1";
  private static final String PROJECT_URL = "projects/google.com:open-bidder-dev2";
  private static final String NETWORK_URL = "https://www.googleapis.com/compute/" + API_TAG
      + "/projects/google.com:open-source-bidder"
      + "/global/networks/network-0cc37e1147da4215af028632f6960b34";
  private static final String INSTANCE_URL = "https://www.googleapis.com/compute/" + API_TAG
      + "/projects/google.com:open-source-bidder"
      + "/zones/rtb-us-east2/instances/load-balancer-1340002895954";
  private static final String ZONE_URL = "https://www.googleapis.com/compute/" + API_TAG
      + "/projects/google.com:open-source-bidder/zones/rtb-us-east2";

  @Test
  public void parseResource_project_ok() {

    ResourceName resourceName = ResourceName.parseResource(PROJECT_URL);
    assertEquals("https://www.googleapis.com/compute/" + API_TAG
        + "/projects/google.com:open-bidder-dev2",
        resourceName.getResourceUrl());
    assertEquals("google.com:open-bidder-dev2", resourceName.getApiProjectId());
    assertEquals("google.com:open-bidder-dev2", resourceName.getResourceName());
    assertEquals(ComputeResourceType.PROJECT, resourceName.getResourceType());
  }

  @Test
  public void parseResource_network_ok() {
    ResourceName resourceName = ResourceName.parseResource(NETWORK_URL);
    assertEquals(NETWORK_URL, resourceName.getResourceUrl());
    assertEquals("google.com:open-source-bidder", resourceName.getApiProjectId());
    assertEquals("network-0cc37e1147da4215af028632f6960b34", resourceName.getResourceName());
    assertEquals(ComputeResourceType.NETWORK, resourceName.getResourceType());
  }

  @Test
  public void parseResource_instance_ok() {
    ResourceName resourceName = ResourceName.parseResource(INSTANCE_URL);
    assertEquals(INSTANCE_URL, resourceName.getResourceUrl());
    assertEquals("google.com:open-source-bidder", resourceName.getApiProjectId());
    assertEquals("load-balancer-1340002895954", resourceName.getResourceName());
    assertEquals("rtb-us-east2", resourceName.getParentResourceName());
    assertEquals(ComputeResourceType.INSTANCE, resourceName.getResourceType());
  }

  @Test
  public void parseResource_zone_ok() {
    ResourceName resourceName = ResourceName.parseResource(ZONE_URL);
    assertEquals(ZONE_URL, resourceName.getResourceUrl());
    assertEquals("google.com:open-source-bidder", resourceName.getApiProjectId());
    assertEquals("rtb-us-east2", resourceName.getResourceName());
  }

  @Test
  public void parseResource_createdByOlderApi_ok() {
    ResourceName.parseResource(ZONE_URL.replaceFirst(API_TAG, "v1beta0"));
  }
}
