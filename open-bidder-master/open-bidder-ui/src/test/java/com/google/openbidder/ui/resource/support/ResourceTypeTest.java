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

package com.google.openbidder.ui.resource.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Tests for {@link com.google.openbidder.ui.resource.support.ResourceType}.
 */
public class ResourceTypeTest {

  @Test
  public void uniqueResourceTypes() {
    Set<String> resourceTypeSet = new HashSet<>();
    ResourceType[] resourceTypes = ResourceType.values();
    for (ResourceType resourceType : resourceTypes) {
      assertNotNull(resourceType.getResourceType());
      assertTrue("Resource type " + resourceType.getResourceType() + " duplicate",
          resourceTypeSet.add(resourceType.getResourceType()));
    }
  }

  @Test
  public void constructor_noParent_correctState() {
    ResourceType resourceType = ResourceType.PROJECT;
    assertNull(resourceType.getParentResourceType());
    assertEquals(Collections.emptyList(), resourceType.getAncestorResourceTypes());
  }

  @Test
  public void constructor_withParent_correctState() {
    ResourceType resourceType = ResourceType.NETWORK;
    assertEquals(ResourceType.PROJECT, resourceType.getParentResourceType());
    assertEquals(Collections.singletonList(ResourceType.PROJECT),
        resourceType.getAncestorResourceTypes());
  }

  @Test(expected = NullPointerException.class)
  public void parseResourceUri_null_throwsError() {
    ResourceType.parseResourceUri(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void parseResourceError_empty_throwsError() {
    ResourceType.parseResourceUri("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void parseResourceError_invalidRootSet_throwsError() {
    ResourceType.parseResourceUri("/foo");
  }

  @Test(expected = IllegalArgumentException.class)
  public void parseResourceError_invalidRootName_throwsError() {
    ResourceType.parseResourceUri("/foo/11");
  }

  @Test(expected = IllegalArgumentException.class)
  public void parseResourceError_invalidChildSet_throwsError() {
    ResourceType.parseResourceUri("/projects/abcd/foo");
  }

  @Test(expected = IllegalArgumentException.class)
  public void parseResourceError_invalidChildName_throwsError() {
    ResourceType.parseResourceUri("/projects/qwerty/foo/11");
  }

  @Test
  public void parseResourceError_validRootSet_returnsOk() {
    assertEquals(ResourceType.PROJECT.getResourceCollectionId(),
        ResourceType.parseResourceUri("/projects"));
  }

  @Test
  public void parseResourceError_validRootName_returnsOk() {
    assertEquals(ResourceType.PROJECT.getResourceId("1234"),
        ResourceType.parseResourceUri("/projects/1234"));
  }

  @Test
  public void parseResourceError_validChildSet_returnsOk() {
    assertEquals(ResourceType.NETWORK.getResourceCollectionId("12"),
        ResourceType.parseResourceUri("/projects/12/networks"));
  }

  @Test
  public void parseResourceError_validChildName_returnsOk() {
    assertEquals(ResourceType.FIREWALL.getResourceId("1234", "lb-bidder"),
        ResourceType.parseResourceUri("/projects/1234/firewalls/lb-bidder"));
  }
}
