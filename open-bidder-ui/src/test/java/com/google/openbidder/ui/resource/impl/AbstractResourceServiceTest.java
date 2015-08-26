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

import static org.junit.Assert.assertEquals;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.openbidder.ui.resource.exception.ResourceMethodNotAllowedException;
import com.google.openbidder.ui.resource.support.AbstractRootResourceService;
import com.google.openbidder.ui.resource.support.ExternalResource;
import com.google.openbidder.ui.resource.support.ResourceCollectionId;
import com.google.openbidder.ui.resource.support.ResourceId;
import com.google.openbidder.ui.resource.support.ResourceMethod;
import com.google.openbidder.ui.resource.support.ResourceType;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * Tests for {@link com.google.openbidder.ui.resource.support.AbstractRootResourceService}.
 */
public class AbstractResourceServiceTest {

  private TestRootResourceService noneSupported;
  private TestRootResourceService allSupported;
  private Resource fakeResource;

  @Before
  public void setUp() {
    noneSupported = new TestRootResourceService(
        ResourceType.PROJECT, EnumSet.noneOf(ResourceMethod.class));
    allSupported = new TestRootResourceService(
        ResourceType.PROJECT, EnumSet.allOf(ResourceMethod.class));
    fakeResource = new Resource("name", /* resource method */ null, "title");
  }

  @Test
  public void constructor_noneSupported_resourceType() {
    assertEquals(ResourceType.PROJECT, noneSupported.getResourceType());
  }

  @Test(expected = ResourceMethodNotAllowedException.class)
  public void get_noneSupported_throwsError() {
    noneSupported.get("foo", noParams());
  }

  @Test(expected = ResourceMethodNotAllowedException.class)
  public void list_noneSupported_throwsError() {
    noneSupported.list(noParams());
  }

  @Test(expected = ResourceMethodNotAllowedException.class)
  public void create_noneSupported_throwsError() {
    noneSupported.create(fakeResource);
  }

  @Test(expected = ResourceMethodNotAllowedException.class)
  public void update_noneSupported_throwsError() {
    noneSupported.update("foo", fakeResource);
  }

  @Test(expected = ResourceMethodNotAllowedException.class)
  public void delete_noneSupported_throwsError() {
    noneSupported.delete("foo");
  }

  @Test
  public void get_allSupported_returnsOk() {
    Resource actual = allSupported.get("foo", noParams());
    Resource expected = new Resource("foo", ResourceMethod.GET);
    assertEquals(expected, actual);
  }

  @Test
  public void list_allSupported_returnsOk() {
    List<? extends Resource> all = allSupported.list(noParams());
    assertEquals(1, all.size());
    Resource actual = Iterables.getFirst(all, null);
    Resource expected = new Resource("foo", ResourceMethod.LIST);
    assertEquals(expected, actual);
  }

  @Test
  public void create_allSupported_returnsOk() {
    Resource newResource = new Resource("foo123", /* resource method */ null, "some title");
    Resource actual = allSupported.create(newResource);
    Resource expected = new Resource("new name", ResourceMethod.CREATE, "some title");
    assertEquals(expected, actual);
  }

  @Test
  public void update_allSupported_returnsOk() {
    Resource updatedResource = new Resource("bar123", /* resource method */ null, "new title");
    Resource actual = allSupported.update("bar123", updatedResource);
    Resource expected = new Resource("bar123", ResourceMethod.UPDATE, "new title");
    assertEquals(expected, actual);
  }

  @Test
  public void delete_allSupported_returnsOk() {
    allSupported.delete("foo");
    ResourceId expected = ResourceType.PROJECT.getResourceId("foo");
    assertEquals(expected, allSupported.deleted);
  }

  private Multimap<String, String> noParams() {
    return HashMultimap.create();
  }

  private static class Resource extends ExternalResource {
    public final ResourceMethod resourceMethod;
    public final String title;

    public Resource(String resourceName, ResourceMethod resourceMethod) {
      this(resourceName, resourceMethod, /* title */ null);
    }

    public Resource(String resourceName, ResourceMethod resourceMethod, @Nullable String title) {
      setId(ResourceType.PROJECT.getResourceId(resourceName));
      this.resourceMethod = resourceMethod;
      this.title = title;
    }

    @Override public int hashCode() {
      return Objects.hashCode(getId(), resourceMethod, title);
    }

    @Override public boolean equals(@Nullable Object o) {
      if (o == this) {
        return true;
      } else if (!(o instanceof Resource) || !(super.equals(o))) {
        return false;
      }
      Resource other = (Resource) o;
      return Objects.equal(getId(), other.getId())
          && Objects.equal(resourceMethod, other.resourceMethod)
          && Objects.equal(title, other.title);
    }

    @Override protected MoreObjects.ToStringHelper toStringHelper() {
      return super.toStringHelper()
          .add("resourceMethod", resourceMethod)
          .add("title", title);
    }
  }

  private static class TestRootResourceService extends AbstractRootResourceService<Resource> {

    public ResourceId deleted;

    protected TestRootResourceService(
        ResourceType resourceType,
        Set<ResourceMethod> supportedMethods) {

      super(resourceType, supportedMethods);
    }

    @Override protected Resource get(ResourceId resourceId, Multimap<String, String> params) {
      return new Resource(resourceId.getResourceName(), ResourceMethod.GET);
    }

    @Override protected List<? extends Resource> list(
        ResourceCollectionId resourceCollectionId,
        Multimap<String, String> params) {

      return Collections.singletonList(new Resource("foo", ResourceMethod.LIST));
    }

    @Override protected Resource create(
        ResourceCollectionId resourceCollectionId,
        Resource newResource) {

      return new Resource(
          "new name",
          ResourceMethod.CREATE,
          newResource.title);
    }

    @Override protected Resource update(
        ResourceId resourceId,
        Resource updatedResource) {

      return new Resource(
          resourceId.getResourceName(),
          ResourceMethod.UPDATE,
          updatedResource.title);
    }

    @Override protected void delete(ResourceId resourceId) {
      deleted = resourceId;
    }
  }
}
