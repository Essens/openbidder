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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.openbidder.ui.resource.exception.ResourceMethodNotAllowedException;
import com.google.openbidder.ui.resource.support.AbstractResourceService;
import com.google.openbidder.ui.resource.support.ExternalResource;
import com.google.openbidder.ui.resource.support.ResourceCollectionId;
import com.google.openbidder.ui.resource.support.ResourceId;
import com.google.openbidder.ui.resource.support.ResourceMethod;
import com.google.openbidder.ui.resource.support.ResourceType;

import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * Tests for {@link com.google.openbidder.ui.resource.support.AbstractResourceService}.
 */
public class AbstractChildResourceServiceTest {

  private TestResourceService noneSupported;
  private TestResourceService allSupported;
  private Child fakeChild;

  @Before
  public void setUp() {
    noneSupported = new TestResourceService(
        ResourceType.NETWORK, EnumSet.noneOf(ResourceMethod.class));
    allSupported = new TestResourceService(
        ResourceType.FIREWALL, EnumSet.allOf(ResourceMethod.class));
    fakeChild = new Child(
        new Parent("foo"),
        /* resource method */ null,
        ResourceType.NETWORK.getResourceId("foo", "bar"),
        /* resource set */ null);
  }

  @Test
  public void constructor_noneSupported_resourceTypeIsNetwork() {
    assertEquals(ResourceType.NETWORK, noneSupported.getResourceType());
  }

  @Test
  public void constructor_allSupported_resourceTypeIsNetwork() {
    assertEquals(ResourceType.FIREWALL, allSupported.getResourceType());
  }

  @Test(expected = ResourceMethodNotAllowedException.class)
  public void get_notSupported_throwsException() {
    assertFalse(noneSupported.supports(ResourceMethod.GET));
    noneSupported.get("foo", "bar", noParams());
  }

  @Test(expected = ResourceMethodNotAllowedException.class)
  public void list_notSupported_throwsException() {
    assertFalse(noneSupported.supports(ResourceMethod.LIST));
    noneSupported.list("foo", noParams());
  }

  @Test(expected = ResourceMethodNotAllowedException.class)
  public void create_notSupported_throwsException() {
    assertFalse(noneSupported.supports(ResourceMethod.CREATE));
    noneSupported.create("foo", fakeChild);
  }

  @Test(expected = ResourceMethodNotAllowedException.class)
  public void update_notSupported_throwsException() {
    assertFalse(noneSupported.supports(ResourceMethod.UPDATE));
    noneSupported.update("foo", "bar", fakeChild);
  }

  @Test(expected = ResourceMethodNotAllowedException.class)
  public void delete_notSupported_throwsException() {
    assertFalse(noneSupported.supports(ResourceMethod.DELETE));
    noneSupported.delete("foo", "bar");
  }

  @Test
  public void get_supported_returnsOk() {
    Child expected = new Child(
        new Parent("foo"),
        ResourceMethod.GET,
        ResourceType.FIREWALL.getResourceId("foo", "bar"),
        /* resource set */ null);
    Child actual = allSupported.get("foo", "bar", noParams());
    assertTrue(allSupported.supports(ResourceMethod.GET));
    assertEquals(expected, actual);
  }

  @Test
  public void list_supported_returnsOk() {
    Child expected = new Child(
        new Parent("foo2"),
        ResourceMethod.LIST,
        /* resource name */ null,
        ResourceType.FIREWALL.getResourceCollectionId("foo2"));
    Collection<? extends Child> childList = allSupported.list("foo2", noParams());
    assertTrue(allSupported.supports(ResourceMethod.LIST));
    assertEquals(1, childList.size());
    Child actual = Iterables.getFirst(childList, null);
    assertEquals(expected, actual);
  }

  @Test
  public void create_supported_returnsOk() {
    Child expected = new Child(
        new Parent("foo3"),
        ResourceMethod.CREATE,
        /* resource name */ null,
        ResourceType.FIREWALL.getResourceCollectionId("foo3"));
    Child actual = allSupported.create("foo3", fakeChild);
    assertTrue(allSupported.supports(ResourceMethod.CREATE));
    assertEquals(expected, actual);
  }

  @Test
  public void update_supported_returnsOk() {
    Child expected = new Child(
        new Parent("foo4"),
        ResourceMethod.UPDATE,
        ResourceType.FIREWALL.getResourceId("foo4", "bar"),
        /* resource set */ null);
    Child updatedChild = new Child(
        new Parent("foo4"),
        /* resource method */ null,
        ResourceType.FIREWALL.getResourceId("foo4", "bar"),
        /* resource set */ null);
    Child actual = allSupported.update("foo4", "bar", updatedChild);
    assertTrue(allSupported.supports(ResourceMethod.UPDATE));
    assertEquals(expected, actual);
  }

  @Test
  public void delete_supported_returnsOk() {
    allSupported.delete("foo", "bar");
    ResourceId expectedId = ResourceType.FIREWALL.getResourceId("foo", "bar");
    Parent expectedParent = new Parent("foo");
    assertTrue(allSupported.supports(ResourceMethod.DELETE));
    assertEquals(expectedId, allSupported.calledId);
    assertEquals(expectedParent, allSupported.calledParent);
  }

  private Multimap<String, String> noParams() {
    return HashMultimap.create();
  }

  private static class Parent extends ExternalResource {
    public Parent(String name) {
      setId(ResourceType.PROJECT.getResourceId(name));
    }

    @Override public int hashCode() {
      return getId().hashCode();
    }

    @Override public boolean equals(@Nullable Object o) {
      if (o == this) {
        return true;
      } else if (!(o instanceof Parent) || !(super.equals(o))) {
        return false;
      }
      Parent other = (Parent) o;
      return Objects.equal(getId(), other.getId());
    }
  }

  private static class Child extends ExternalResource {
    private final Parent parent;
    private final ResourceMethod resourceMethod;
    private final ResourceCollectionId resourceCollectionId;

    private Child(
        Parent parent,
        ResourceMethod resourceMethod,
        ResourceId resourceId,
        ResourceCollectionId resourceCollectionId) {

      this.parent = parent;
      this.resourceMethod = resourceMethod;
      setId(resourceId);
      this.resourceCollectionId = resourceCollectionId;
    }

    @Override public int hashCode() {
      return Objects.hashCode(parent, resourceMethod, getId(), resourceCollectionId);
    }

    @Override public boolean equals(@Nullable Object o) {
      if (o == this) {
        return true;
      } else if (!(o instanceof Child) || !(super.equals(o))) {
        return false;
      }
      Child other = (Child) o;
      return Objects.equal(parent, other.parent)
          && Objects.equal(resourceMethod, other.resourceMethod)
          && Objects.equal(getId(), other.getId())
          && Objects.equal(resourceCollectionId, other.resourceCollectionId);
    }

    @Override protected MoreObjects.ToStringHelper toStringHelper() {
      return super.toStringHelper()
          .add("parent", parent)
          .add("resourceMethod", resourceMethod)
          .add("resourceSetId", resourceCollectionId);
    }
  }

  private static class TestResourceService
      extends AbstractResourceService<Parent, Child> {

    public Parent calledParent;
    public ResourceId calledId;

    private TestResourceService(
        ResourceType resourceType,
        Set<ResourceMethod> supportedMethods) {

      super(resourceType, supportedMethods);
    }

    @Override protected Parent getParent(String resourceName) {
      return new Parent(resourceName);
    }

    @Override protected Child get(
        Parent parentResource,
        ResourceId resourceId,
        Multimap<String, String> params) {

      return new Child(
          parentResource,
          ResourceMethod.GET,
          resourceId,
          /* resourceSetId */ null);
    }

    @Override protected List<? extends Child> list(
        Parent parentResource,
        ResourceCollectionId resourceCollectionId,
        Multimap<String, String> params) {

      return Collections.singletonList(new Child(
          parentResource,
          ResourceMethod.LIST,
          /* resourceNameId */ null,
          resourceCollectionId));
    }

    @Override protected Child create(
        Parent parentResource,
        ResourceCollectionId resourceCollectionId,
        Child childResource) {

      return new Child(
          parentResource,
          ResourceMethod.CREATE,
          /* resourceNameId */ null,
          resourceCollectionId);
    }

    @Override protected Child update(
        Parent parentResource,
        ResourceId childRsourceId,
        Child childResource) {

      return new Child(
          parentResource,
          ResourceMethod.UPDATE,
          childRsourceId,
          /* resourceSetId */ null);
    }

    @Override protected void delete(Parent parentResource, ResourceId resourceId) {
      calledParent = parentResource;
      calledId = resourceId;
    }
  }
}
