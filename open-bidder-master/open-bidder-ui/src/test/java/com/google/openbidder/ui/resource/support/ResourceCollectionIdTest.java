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
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Tests for {@link ResourceCollectionId}.
 */
public class ResourceCollectionIdTest {

  @SuppressWarnings("unused")
  @Test(expected = NullPointerException.class)
  public void constructor_nullType_throwsError() {
    new ResourceCollectionId(/* resource type */ null);
  }

  @Test
  public void constructor_noParentNoneExpected_returnsOk() {
    ResourceCollectionId resourceCollectionId = new ResourceCollectionId(ResourceType.PROJECT);
    assertEquals(ResourceType.PROJECT, resourceCollectionId.getResourceType());
    assertNull(resourceCollectionId.getParent());
    assertEquals("/projects", resourceCollectionId.getResourceUri());
  }

  @Test(expected = IllegalArgumentException.class)
  @SuppressWarnings("unused")
  public void constructor_noParentTypeRequiresParent_throwsError() {
    new ResourceCollectionId(ResourceType.NETWORK);
  }

  @Test(expected = IllegalArgumentException.class)
  @SuppressWarnings("unused")
  public void constructor_withUnexpectedParent_throwsError() {
    new ResourceCollectionId(ResourceType.PROJECT.getResourceId("foo"), ResourceType.PROJECT);
  }

  @Test
  public void constructor_withCorrectParent_returnsOk() {
    ResourceId projectId = ResourceType.PROJECT.getResourceId("foo");
    ResourceCollectionId networkCollection = projectId.getChildCollection(ResourceType.NETWORK);
    assertEquals(projectId, networkCollection.getParent());
    assertEquals(ResourceType.NETWORK, networkCollection.getResourceType());
  }

  @Test(expected = IllegalArgumentException.class)
  @SuppressWarnings("unused")
  public void constructor_withIncorrectParent_returnsOk() {
    ResourceId projectId = ResourceType.PROJECT.getResourceId("foo");
    ResourceId networkId = projectId
        .getChildCollection(ResourceType.NETWORK)
        .getResourceId("bar");
    new ResourceCollectionId(networkId, ResourceType.FIREWALL);
  }
}
