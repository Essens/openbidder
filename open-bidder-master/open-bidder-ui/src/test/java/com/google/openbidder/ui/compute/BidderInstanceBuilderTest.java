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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.api.services.compute.model.Instance;
import com.google.api.services.compute.model.NetworkInterface;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.openbidder.config.server.LoadBalancerPort;
import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.entity.support.ProjectRole;
import com.google.openbidder.ui.entity.support.UserRole;
import com.google.openbidder.ui.project.ProjectUser;
import com.google.openbidder.ui.resource.support.ResourceId;
import com.google.openbidder.ui.resource.support.ResourceType;
import com.google.openbidder.ui.util.LocalAppEngineTestCase;
import com.google.openbidder.ui.util.MockResource;
import com.google.openbidder.util.Clock;
import com.google.openbidder.util.testing.FakeClock;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link BidderInstanceBuilder}.
 */
public class BidderInstanceBuilderTest extends LocalAppEngineTestCase {

  private static final String PROJECT_VERSION = "0.1.2-SNAPSHOT";
  private static final String REDIRECT_URL = "http://cm.g.doubleclick.net";

  private LoadBalancerParameters loadBalancerParameters;
  private Clock clock;

  @Before
  public void startUp() {
    loadBalancerParameters = new LoadBalancerParameters(new MockResource(""),
        ImmutableList.<String>of("https://scope1"), "19090");
    clock = new FakeClock();
  }

  @Test
  public void testBuildNoExternalIp() {
    BidderParameters bidderParameters = new BidderParameters(
        "-Dfoo=bar",
        "--listen_port=18081 --admin_port=18082",
        ImmutableList.<String>of("com.google.openbidder.DefaultInterceptor"), // bid interceptors
        ImmutableList.<String>of(), // impression interceptors
        ImmutableList.<String>of(), // click interceptors
        ImmutableList.<String>of(), // match interceptors
        loadBalancerParameters.getDefaultOauth2Scopes(),
        false,
        new MockResource("#!/bin/bash!"),
        REDIRECT_URL);

    BidderInstanceBuilder builder = newInstanceBuilder(bidderParameters);
    ProjectUser projectUser = buildBasicProject(bidderParameters);
    ResourceId zoneId = ResourceType.ZONE.getResourceId("123", "us-east-a");
    Instance instance = builder.build(projectUser, zoneId, "some name", null);
    assertEquals(
        ImmutableSet.of(BidderInstanceBuilder.TAG),
        ImmutableSet.copyOf(instance.getTags().getItems()));
    assertNotNull(instance.getDescription());
    assertTrue(instance.getDescription().contains(projectUser.getEmail()));
    assertEquals(1, instance.getNetworkInterfaces().size());
    NetworkInterface network = instance.getNetworkInterfaces().get(0);
    assertNotNull(network.getNetwork());
    assertNull(network.getAccessConfigs());

    ImmutableMap<String, String> metadata = ComputeUtils.toMap(instance.getMetadata());
    assertEquals("-Dfoo=bar", metadata.get(BidderInstanceBuilder.METADATA_JVM_PARAMETERS));
  }

  @Test
  public void testBuildExternalIpExternalJar() {
     BidderParameters bidderParameters = new BidderParameters(
        "-Dfoo=bar",
        "--listen_port=18081 --admin_port=18082",
        ImmutableList.<String>of(), // bid interceptors
        ImmutableList.<String>of(), // impression interceptors
        ImmutableList.<String>of(), // click interceptors
        ImmutableList.<String>of(), // match interceptors
        loadBalancerParameters.getDefaultOauth2Scopes(),
        true,
        new MockResource("#!/bin/bash!"),
        REDIRECT_URL);

    BidderInstanceBuilder builder = newInstanceBuilder(bidderParameters);
    ProjectUser projectUser = buildBasicProject(bidderParameters);
    Project project = projectUser.getProject();
    project.setUserDistUri("gs://my-bucket");

    ResourceId zoneId = ResourceType.ZONE.getResourceId("123", "us-east-a");
    Instance instance = builder.build(projectUser, zoneId, "some name", null);
    assertEquals(
        ImmutableSet.of(BidderInstanceBuilder.TAG),
        ImmutableSet.copyOf(instance.getTags().getItems()));
    assertEquals(1, instance.getNetworkInterfaces().size());
    NetworkInterface network = instance.getNetworkInterfaces().get(0);
    assertNotNull(network.getNetwork());
    assertNotNull(network.getAccessConfigs());

    ImmutableMap<String, String> metadata = ComputeUtils.toMap(instance.getMetadata());
    assertEquals("gs://my-bucket", metadata.get(InstanceBuilder.METADATA_USER_DIST));
    assertEquals("-Dfoo=bar", metadata.get(BidderInstanceBuilder.METADATA_JVM_PARAMETERS));

    project.setUserDistUri("gs://my-bucket///");
    instance = builder.build(projectUser, zoneId, "some name", null);
    metadata = ComputeUtils.toMap(instance.getMetadata());
    assertEquals("gs://my-bucket", metadata.get(InstanceBuilder.METADATA_USER_DIST));
  }

  @Test
  public void testOpenBidderVersionMacro_noExpansion() {
    BidderParameters bidderParameters = new BidderParameters(
        "-Dfoo=bar",
        "--listen_port=18081 --admin_port=18082",
        ImmutableList.<String>of(), // bid interceptors
        ImmutableList.<String>of(), // impression interceptors
        ImmutableList.<String>of(), // click interceptors
        ImmutableList.<String>of(), // match interceptors
        loadBalancerParameters.getDefaultOauth2Scopes(),
        true,
        new MockResource("#!/bin/bash!"),
        REDIRECT_URL);

    BidderInstanceBuilder builder = newInstanceBuilder(bidderParameters);
    ProjectUser projectUser = buildBasicProject(bidderParameters);
    projectUser.getProject().setUserDistUri("gs://foo");
    ResourceId zoneId = ResourceType.ZONE.getResourceId("123", "us-east-a");
    Instance instance = builder.build(projectUser, zoneId, "some name", null);

    ImmutableMap<String, String> metadata = ComputeUtils.toMap(instance.getMetadata());
    assertEquals("gs://foo", metadata.get(InstanceBuilder.METADATA_USER_DIST));
  }

  @Test
  public void testInterceptors() {
    BidderParameters bidderParameters = new BidderParameters(
        "-Dfoo=bar",
        "--listen_port=18081 --admin_port=18082",
        ImmutableList.<String>of(), // bid interceptors
        ImmutableList.<String>of(), // impression interceptors
        ImmutableList.<String>of(), // click interceptors
        ImmutableList.<String>of(), // match interceptors
        loadBalancerParameters.getDefaultOauth2Scopes(),
        true,
        new MockResource("#!/bin/bash!"),
        REDIRECT_URL);

    BidderInstanceBuilder builder = newInstanceBuilder(bidderParameters);
    ProjectUser projectUser = buildBasicProject(bidderParameters);
    Project project = projectUser.getProject();
    project.setBidInterceptors(ImmutableList.of("A", "B"));
    project.setImpressionInterceptors(ImmutableList.of("C", "D"));
    project.setZoneHost("us-east-a", "myhost");

    ResourceId zoneId = ResourceType.ZONE.getResourceId("123", "us-east-a");
    Instance instance = builder.build(projectUser, zoneId, "some name", null);
    assertEquals(
        ImmutableSet.of(BidderInstanceBuilder.TAG),
        ImmutableSet.copyOf(instance.getTags().getItems()));

    ImmutableMap<String, String> metadata = ComputeUtils.toMap(instance.getMetadata());
    assertEquals("A,B", metadata.get(BidderInstanceBuilder.METADATA_BID_INTERCEPTORS));
    assertEquals("C,D", metadata.get(BidderInstanceBuilder.METADATA_IMPRESSION_INTERCEPTORS));
    assertEquals("E,F", metadata.get(BidderInstanceBuilder.METADATA_CLICK_INTERCEPTORS));
    assertEquals("myhost", metadata.get(BidderInstanceBuilder.METADATA_BALANCER_HOST));
    assertEquals(
        String.valueOf(LoadBalancerPort.DEFAULT),
        metadata.get(BidderInstanceBuilder.METADATA_BALANCER_PORT));
  }

  private ProjectUser buildBasicProject(BidderParameters bidderParameters) {
    Project project = new Project();
    project.setApiProjectId("com:google-testproject");
    project.setUserDistUri("gs://open-bidder-user");
    project.setApiProjectNumber(Long.valueOf(123));
    project.setBidderImage("debian-7-wheezy-v20131014");
    project.setVmParameters(bidderParameters.getDefaultJvmParameters());
    project.setMainParameters(bidderParameters.getDefaultMainParameters());
    project.setClickInterceptors(ImmutableList.of("E", "F"));
    project.setNetworkName("network-12345678");
    project.setBidderRequestPort(bidderParameters.getRequestPort());
    project.setBidderAdminPort(bidderParameters.getAdminPort());
    project.setLoadBalancerRequestPort(loadBalancerParameters.getRequestPort());
    project.setLoadBalancerStatPort(loadBalancerParameters.getStatPort());
    project.setBidderOauth2Scopes(bidderParameters.getDefaultOauth2Scopes());
    return new ProjectUser(project, new UserRole(1, ProjectRole.OWNER), "foo@bar.com", true);
  }

  private BidderInstanceBuilder newInstanceBuilder(BidderParameters bidderParameters) {
    return new BidderInstanceBuilder(clock, bidderParameters, PROJECT_VERSION);
  }
}
