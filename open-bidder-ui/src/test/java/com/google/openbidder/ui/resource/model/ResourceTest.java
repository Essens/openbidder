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

package com.google.openbidder.ui.resource.model;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.openbidder.ui.resource.support.ExternalResource;
import com.google.openbidder.ui.resource.support.ResourceId;
import com.google.openbidder.ui.resource.support.ResourceType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tests to ensure {@link com.google.openbidder.ui.resource.support.ExternalResource}
 * classes satisfy the contract regarding mandatory properties as well as flagging when
 * fields are set and not set. This flag is for detecting partial responses and requests
 * in JSON marshaling and unmarshaling.
 */
@RunWith(Parameterized.class)
public class ResourceTest {

  private static final ImmutableSet<String> MANDATORY_PROPERTIES = ImmutableSet.of(
      "id",
      "resourceName",
      "resourceType",
      "resourceUri"
  );

  private static final ImmutableSet<String> IGNORE_PROPERTIES = ImmutableSet.of(
      "class"
  );

  private static final ImmutableSet<String> READ_ONLY_PROPERTIES = ImmutableSet.of(
      "resourceName",
      "resourceType",
      "resourceUri"
  );

  private AtomicInteger counter;

  private final Class<? extends ExternalResource> resourceClass;
  private final String className;
  private final ResourceId resourceId;

  public ResourceTest(
      Class<? extends ExternalResource> resourceClass,
      ResourceId resourceId) {

    this.resourceClass = checkNotNull(resourceClass);
    this.className = resourceClass.getSimpleName();
    this.resourceId = resourceId;
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return asList(new Object[][]{
        {ProjectResource.class, ResourceType.PROJECT.getResourceId("1234")},
        {RegionResource.class, ResourceType.REGION.getResourceId("1234", "rtb-us-west1")},
        {ZoneResource.class, ResourceType.ZONE.getResourceId("2345", "rtb-us-east1-a")},
        {QuotaResource.class, ResourceType.QUOTA.getResourceId("2345", "cpus")},
        {NetworkResource.class, ResourceType.NETWORK.getResourceId("2345", "network-23986723872")},
        {FirewallResource.class, ResourceType.FIREWALL.getResourceId("2345", "firewall-298672342")},
        {MachineTypeResource.class, ResourceType.MACHINE_TYPE.getResourceId(
            "2345", "rtb-us-east1", "std-1-cpu")},
        {ImageResource.class, ResourceType.DEFAULT_IMAGE.getResourceId("2345", "default-image-1")},
        {InstanceResource.class, ResourceType.INSTANCE.getResourceId(
            "2345", "rtb-us-east1", "bidder-2345")},
        {BidderResource.class, ResourceType.INSTANCE.getResourceId(
            "2345", "rtb-us-east1", "bidder-2345")},
        {BalancerResource.class, ResourceType.INSTANCE.getResourceId(
            "2345", "rtb-us-east1", "bidder-2345")},
        {UserResource.class, ResourceType.USER.getResourceId("223344", "user@example.com")},
        {ReportResource.class, ResourceType.REPORT.getResourceId("12345", "report-12345.csv")},
        {AccountResource.class, ResourceType.ACCOUNT.getResourceId("2323212", "42004742")},
        {ScheduledOutage.class, /* resourceId */ null},
        {DoubleClickProjectResource.class, /* resourceId */ null},
        {CustomBidderResource.class, /* resourceId */ null},
    });
  }

  @Before
  public void setUp() {
    counter = new AtomicInteger(1123 + (int) (System.currentTimeMillis() % 10000));
  }

  @Test
  public void noArgsConstructor() throws Exception {
    resourceClass.newInstance();
  }

  @Test
  public void ignoreUnknownProperties() {
    JsonIgnoreProperties ignoreProperties = AnnotationUtils.findAnnotation(
        resourceClass, JsonIgnoreProperties.class);
    assertNotNull("Class " + className + " has no @JsonIgnoreProperties annotation",
        ignoreProperties);
    assertTrue("Class " + className + " not set to ignore unknown properties",
        ignoreProperties.ignoreUnknown());
  }

  @Test
  public void mandatoryProperties() {
    if (resourceId != null) {
      Map<String, Property> properties = getProperties();
      for (String propertyName : MANDATORY_PROPERTIES) {
        assertTrue(String.format("[%s.%s] not found", className, propertyName),
            properties.containsKey(propertyName));
      }
    }
  }

  @Test
  public void readOnlyProperties() {
    Map<String, Property> properties = getProperties();
    for (String propertyName : READ_ONLY_PROPERTIES) {
      Property property = properties.get(propertyName);
      if (property != null) {
        assertNull(String.format("[%s.%s] has a set method", className, property.name),
            property.value.getWriteMethod());
        assertNull(String.format("[%s.%s] has a clear method", className, property.name),
            property.clear);
        assertNull(String.format("[%s.%s] has a status method", className, property.name),
            property.status);
      }
    }
  }

  @Test
  public void mutableProperties() {
    Map<String, Property> properties = getProperties();
    for (Property property : properties.values()) {
      if (!READ_ONLY_PROPERTIES.contains(property.name)) {
        assertNotNull(String.format("[%s.%s] is not readable", className, property.name),
            property.value.getReadMethod());
        assertNotNull(String.format("[%s.%s] is not writable", className, property.name),
            property.value.getWriteMethod());
        assertNotNull(String.format("[%s.%s] has no clear method", className, property.name),
            property.clear);
        assertNotNull(String.format("[%s.%s] is not readable", className, property.name),
            property.status);
      }
    }
  }

  @Test
  public void id() {
    if (resourceId != null) {
      ExternalResource resource = (ExternalResource) newResource();
      assertNull(String.format("[%s.id] initially not null", className), resource.getId());
      assertFalse(String.format("[%s.id] initially set", className), resource.hasId());
      assertNull(String.format("[%s.resourceName] initially set", className),
          resource.getResourceName());
      assertNull(String.format("[%s.resourceType] initially set", className),
          resource.getResourceType());
      assertNull(String.format("[%s.resourceUri] initially set", className),
          resource.getResourceUri());

      resource.setId(resourceId);
      assertEquals(String.format("[%s.id] should be %s", className, resourceId),
          resourceId, resource.getId());
      assertTrue(String.format("[%s.id] not set", className), resource.hasId());
      assertEquals(String.format("[%s.resourceName] should be %s", className,
          resourceId.getResourceName()),
          resourceId.getResourceName(), resource.getResourceName());
      assertEquals(String.format("[%s.resourceType] should be %s", className,
          resourceId.getResourceType()),
          resourceId.getResourceType(), resource.getResourceType());
      assertEquals(String.format("[%s.resourceUri] should be %s", className,
          resourceId.getResourceUri()),
          resourceId.getResourceUri(), resource.getResourceUri());

      resource.clearId();
      assertNull(String.format("[%s.id] not null", className), resource.getId());
      assertFalse(String.format("[%s.id] set", className), resource.hasId());
      assertNull(String.format("[%s.resourceName] set", className),
          resource.getResourceName());
      assertNull(String.format("[%s.resourceType] set", className),
          resource.getResourceType());
      assertNull(String.format("[%s.resourceUri] set", className),
          resource.getResourceUri());
    }
  }

  @Test
  public void propertyStatus() {
    Map<String, Property> properties = getProperties();
    Object resource = newResource();
    for (Property property : properties.values()) {
      if (!READ_ONLY_PROPERTIES.contains(property.name)) {
        assertNull(String.format("[%s.%s] initially not null", className, property.name),
            property.get(resource));
        assertFalse(String.format("[%s.%s] initially set", className, property.name),
            property.has(resource));

        Object newValue = buildValue(property.value.getPropertyType());
        property.set(resource, newValue);
        assertEquals(String.format("[%s.%s] should be %s", className, property.name, newValue),
            newValue, property.get(resource));
        assertTrue(String.format("[%s.%s] not set", className, property.name),
            property.has(resource));

        property.clear(resource);
        assertNull(String.format("[%s.%s] not null", className, property.name),
            property.get(resource));
        assertFalse(String.format("[%s.%s] set", className, property.name),
            property.has(resource));
      }
    }
  }

  @Test
  public void equalsAndHashCode() {
    Map<String, Property> properties = getProperties();
    Object emptyResource = newResource();
    Object resource = newResource();
    assertFalse(String.format("[%s] equal to null", className), emptyResource.equals(null));
    assertEquals(String.format("[%s] not equal", className), emptyResource, resource);
    assertEquals(String.format("[%s] hash codes not equal", className),
        emptyResource.hashCode(), resource.hashCode());
    for (Property property : properties.values()) {
      if (!READ_ONLY_PROPERTIES.contains(property.name)) {
        resource = newResource();
        property.set(resource, null);
        assertThat(String.format("[%s.%s] equal %s", className, property.name, resource),
            emptyResource, not(equalTo(resource)));
        Object newValue = buildValue(property.value.getPropertyType());
        property.set(resource, newValue);
        assertThat(String.format("[%s.%s] equal %s", className, property.name, resource),
            emptyResource, not(equalTo(resource)));
      }
    }
  }

  private Map<String, Property> getProperties() {
    Map<String, Property> properties = new HashMap<>();
    BeanWrapper beanWrapper = new BeanWrapperImpl(resourceClass);
    for (PropertyDescriptor propertyDescriptor : beanWrapper.getPropertyDescriptors()) {
      String propertyName = propertyDescriptor.getName();
      if (IGNORE_PROPERTIES.contains(propertyName)) {
        continue;
      }
      Property property = new Property();
      property.name = propertyName;
      property.value = propertyDescriptor;
      String nameUpper = Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
      property.clear = ReflectionUtils.findMethod(
          beanWrapper.getWrappedClass(), "clear" + nameUpper);
      property.status = ReflectionUtils.findMethod(
          beanWrapper.getWrappedClass(), "has" + nameUpper);
      properties.put(propertyName, property);
    }
    return properties;
  }

  private Object newResource() {
    try {
      return resourceClass.newInstance();
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException(e);
    }
  }

  private Object buildValue(Class<?> klass) {
    int nextVal = counter.getAndIncrement();
    if (klass == String.class) {
      return "string " + nextVal;
    } else if (klass == List.class || klass == Collection.class) {
      return ImmutableList.of(
          "string1" + nextVal,
          "string1" + nextVal,
          "string1" + nextVal);
    } else if (klass == Map.class) {
      return ImmutableMap.<String, Integer>of(
          "foo", 3,
          "bar", 4);
    } else if (klass == Instant.class) {
      return new Instant();
    } else if (klass == Integer.class) {
      return 7;
    } else if (klass == Double.class) {
      return 3.14;
    } else if (klass == Long.class) {
      return 100L;
    } else if (klass == BigInteger.class) {
      return BigInteger.valueOf(123);
    } else if (klass == BigDecimal.class) {
      return BigDecimal.valueOf(456);
    } else if (klass == Boolean.class) {
      return Boolean.FALSE;
    } else if (klass == ResourceId.class) {
      return new ResourceId(ResourceType.PROJECT, "abcd");
    } else if (klass.isEnum()) {
      return klass.getEnumConstants()[0];
    } else if (klass == DoubleClickProjectResource.class) {
      return new DoubleClickProjectResource();
    } else if (klass == CustomBidderResource.class) {
      return new CustomBidderResource();
    }
    throw new IllegalStateException("Unknown type " + klass);
  }

  private static class Property {

    public String name;

    /**
     * {@link PropertyDescriptor} for the property itself.
     */
    public PropertyDescriptor value;

    /**
     * {@link Method} to clear a property value.
     */
    public Method clear;

    /**
     * {@link Method} to test if the property is present.
     */
    public Method status;

    public boolean has(Object obj) {
      checkState(status != null, "no status method");
      try {
        return (Boolean) status.invoke(obj);
      } catch (ReflectiveOperationException e) {
        throw new IllegalStateException("Error reading status for property " + status.getName(), e);
      }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Object obj) {
      checkState(value != null, "no value method");
      checkState(value.getReadMethod() != null, "no getter on value method");
      try {
        return (T) value.getReadMethod().invoke(obj);
      } catch (ReflectiveOperationException e) {
        throw new IllegalStateException("Error reading property " + status.getName(), e);
      }
    }

    public <T> void set(Object obj, T newValue) {
      checkState(value != null, "no value method");
      checkState(value.getWriteMethod() != null, "no setter on value method");
      try {
        value.getWriteMethod().invoke(obj, newValue);
      } catch (ReflectiveOperationException e) {
        throw new IllegalStateException("Error reading property " + status.getName(), e);
      }
    }

    public void clear(Object obj) {
      checkState(clear != null, "no clear method");
      try {
        clear.invoke(obj);
      } catch (ReflectiveOperationException e) {
        throw new IllegalStateException("No clear method", e);
      }
    }
  }
}
