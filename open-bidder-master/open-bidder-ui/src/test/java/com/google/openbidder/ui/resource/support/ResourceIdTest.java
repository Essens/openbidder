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

import org.junit.Test;

/**
 * Tests for {@link com.google.openbidder.ui.resource.support.ResourceId}.
 */
public class ResourceIdTest {

  @Test(expected = NullPointerException.class)
  @SuppressWarnings("unused")
  public void constructor_noResourceType_throwsError() {
    new ResourceId((ResourceType) null, "foo");
  }

  @Test(expected = NullPointerException.class)
  @SuppressWarnings("unused")
  public void constructor_noResourceName_throwsError() {
    new ResourceId(ResourceType.PROJECT, /* resource name */ null);
  }

  @Test
  public void constructor_noParentNoneExpected_returnsOk() {
    ResourceId resourceId = new ResourceId(ResourceType.PROJECT, "foo");
    assertEquals(ResourceType.PROJECT, resourceId.getResourceType());
    assertEquals("foo", resourceId.getResourceName());
    assertEquals(new ResourceCollectionId(ResourceType.PROJECT), resourceId.getParent());
    assertEquals("/projects/foo", resourceId.getResourceUri());
  }

  @Test(expected = IllegalArgumentException.class)
  @SuppressWarnings("unused")
  public void constructor_noParentTypeRequiresParent_throwsError() {
    new ResourceId(ResourceType.NETWORK, "foo");
  }

  @Test
  public void constructor_withCorrectParent_returnsOk() {
    ResourceId projectId = ResourceType.PROJECT.getResourceId("foo");
    ResourceId networkId = projectId
        .getChildCollection(ResourceType.NETWORK)
        .getResourceId("bar");
    assertEquals(ResourceType.NETWORK, networkId.getResourceType());
    assertEquals(projectId.getChildCollection(ResourceType.NETWORK), networkId.getParent());
    assertEquals(projectId, networkId.getParent().getParent());
    assertEquals("bar", networkId.getResourceName());
    assertEquals("/projects/foo/networks/bar", networkId.getResourceUri());
  }
}
