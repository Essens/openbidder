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

package com.google.openbidder.ui.ft;

import static com.google.openbidder.ui.resource.ResourceMatchers.project;
import static com.google.openbidder.ui.resource.ResourceMatchers.projects;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.google.api.services.compute.model.Firewall;
import com.google.api.services.compute.model.Instance;
import com.google.api.services.compute.model.Network;
import com.google.api.services.compute.model.Zone;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.openbidder.ui.compute.BidderParameters;
import com.google.openbidder.ui.compute.ComputeResourceType;
import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.resource.model.DoubleClickProjectResource;
import com.google.openbidder.ui.resource.model.ProjectResource;
import com.google.openbidder.ui.resource.support.ResourceType;
import com.google.openbidder.ui.util.WebContextLoader;

import com.googlecode.objectify.NotFoundException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

/**
 * Tests for {@link com.google.openbidder.ui.controller.ProjectController}.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
    locations = {
        "file:src/main/webapp/WEB-INF/applicationContext.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-security.xml",
        "file:src/main/webapp/WEB-INF/ui-servlet.xml",
        "classpath:/bean-overrides.xml"
    },
    loader = WebContextLoader.class)
public class ProjectFunctionalTest extends OpenBidderFunctionalTestCase {

  public static final String PROJECT_NAME = "Test Project #3";
  public static final String OAUTH_2_CLIENT_ID = "qwerty";
  public static final String OAUTH_2_CLIENT_SECRET = "1/qwerty";
  public static final String API_PROJECT_ID = "example.com:open-bidder";
  public static final long API_PROJECT_NUMBER = 89722342423L;
  public static final String BIDDER_MACHINE_TYPE = "shiny";
  public static final String LOAD_BALANCER_MACHINE_TYPE = "dangerous";
  public static final String VM_PARAMETERS = "-Xmx512m";
  public static final String MAIN_PARAMETERS = "--listen_port=18080";
  public static final List<String> BIDDER_INTERCEPTORS = Arrays.asList("one", "two", "three");
  public static final String SERVER_WAR_URI = "gs://example.com/server";
  public static final String JAR_BUCKET_URI = "bucket";
  public static final List<String> WHITE_LISTED_IP_RANGES = Arrays
      .asList("192.168.0.0/16", "10.0.0.0/8");
  public static final String NETWORK_NAME = "network name";
  public static final String PROJECT_UUID = "blah";
  public static final String STATUS = "I'm a teapot";
  public static final String PROJECT_ID = "1234";
  public static final String DOUBLE_CLICK_REPORTING_BUCKET = "reporting-bucket-123";
  public static final String DOUBLE_CLICK_PREFERRED_DEALS_BUCKET = "preferred-deals-123";

  @Inject
  private BidderParameters bidderParameters;

  @Test
  public void get_user2project1_notFound() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    expectNotFound(get(projectIdUri(PROJECT1)));
  }

  @Test
  public void get_user1project1unauthorized_ok() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectJson(get(projectIdUri(PROJECT1)), jsonPath("$", project(project1, false)));
  }

  @Test
  public void get_user1project1noNetwork_ok() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    authorizeComputeService(PROJECT1, EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectJson(get(projectIdUri(PROJECT1)), jsonPath("$", project(project1, false)));
  }

  @Test
  public void get_user1project1authorizedWithNetwork_ok_() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    authorizeComputeService(PROJECT1, EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    getComputeClient(PROJECT1).addNetwork(buildNetwork(PROJECT1));
    expectJson(get(projectIdUri(PROJECT1)), jsonPath("$", project(project1, true)));
  }

  @Test
  public void list_user1_2projects() {
    testListProjects(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2, PROJECT1, PROJECT2);
  }

  @Test
  public void list_user2_1project() {
    testListProjects(EMAIL_OWNER_PROJECT_2, PROJECT2);
  }

  @Test
  public void list_user3_0projects() {
    testListProjects(EMAIL_NO_PROJECTS);
  }

  @Test
  public void postJson_missingRequiredFields_badRequest() {
    ProjectResource projectResource = new ProjectResource();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectBadRequest(postObjectJson(projectCollectionUri(), projectResource));
  }

  @Test
  public void postForm_missingRequiredFields_badRequest() {
    ProjectResource projectResource = new ProjectResource();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectBadRequest(postObjectUrlEncoded(projectCollectionUri(), projectResource));
  }

  @Test
  public void postJson_minimalRequiredFields_ok() {
    ImmutableMap<String, Object> projectRequest = ImmutableMap.<String, Object>of(
        "apiProjectId", API_PROJECT_ID,
        "apiProjectNumber", API_PROJECT_NUMBER,
        "oauth2ClientId", OAUTH_2_CLIENT_ID,
        "oauth2ClientSecret", OAUTH_2_CLIENT_SECRET,
        "userDistUri", USER_DIST_URI);
    ProjectResource expectedProject = new ProjectResource();
    expectedProject.setApiProjectId(API_PROJECT_ID);
    expectedProject.setApiProjectNumber(API_PROJECT_NUMBER);
    expectedProject.setOauth2ClientId(OAUTH_2_CLIENT_ID);
    expectedProject.setOauth2ClientSecret(OAUTH_2_CLIENT_SECRET);
    expectedProject.setVmParameters(bidderParameters.getDefaultJvmParameters());
    expectedProject.setMainParameters(bidderParameters.getDefaultMainParameters());
    expectedProject.setBidInterceptors(bidderParameters.getDefaultBidInterceptors());
    expectedProject.setImpressionInterceptors(bidderParameters.getDefaultImpressionInterceptors());
    expectedProject.setClickInterceptors(bidderParameters.getDefaultClickInterceptors());
    expectedProject.setMatchInterceptors(bidderParameters.getDefaultMatchInterceptors());
    expectedProject.setUserDistUri(USER_DIST_URI);
    expectedProject.setWhiteListedIpRanges(Collections.<String>emptyList());
    login(EMAIL_NO_PROJECTS);
    ProjectResource actualProject = expectJson(
        postObjectJson(projectCollectionUri(), projectRequest),
        ProjectResource.class);
    verifyResponse(expectedProject, actualProject, false);
  }

  @Test
  public void postJson_hasAllRequiredFields_ok() {
    ProjectResource projectRequest = completeProject();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    ProjectResource actualProject = expectJson(
        postObjectJson(projectCollectionUri(), projectRequest),
        ProjectResource.class);
    ProjectResource expectedProject = completeProject();
    verifyResponse(expectedProject, actualProject, false);
  }

  @Test
  public void postSetAsDefault_unknownProject_notFound() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectNotFound(post(setAsDefaultUri(12345)));
  }

  @Test
  public void postSetAsDefault_noAccess_notFound() {
    standardFixtures();
    login(EMAIL_NO_PROJECTS);
    expectNotFound(post(setAsDefaultUri(project2.getId())));
  }

  @Test
  public void postSetAsDefault_readAccess_ok() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectOk(post(setAsDefaultUri(project2.getId())));
  }

  @Test
  public void put_unknownProject_notFound() {
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectNotFound(putObjectJson(projectIdUri(12345), emptyRequest()));
  }

  @Test
  public void put_readAccess_forbidden() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectForbidden(putObjectJson(projectIdUri(PROJECT2), emptyRequest()));
  }

  @Test
  public void put_completeProjectAsOwner_ok() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    ProjectResource projectResource = completeProject();
    ProjectResource actualProject = expectJson(
        putObjectJson(projectIdUri(PROJECT1), projectResource),
        ProjectResource.class);
    ProjectResource expectedProject = completeProject();
    expectedProject.setApiProjectId(project1.getApiProjectId());
    verifyResponse(expectedProject, actualProject, false);
  }

  @Test
  public void put_singleFieldAsReadWrite_updatesOnlyOneField() {
    standardFixtures();
    login(EMAIL_READ_WRITE_PROJECT_1);
    ImmutableMap<String, Object> updateRequest = ImmutableMap.<String, Object>of(
        "projectUuid", "foo",
        "invalidField", "bar",
        "description", "New Project Name");

    Project expectedProject = project1;
    expectedProject.setProjectName("New Project Name");
    expectJson(putObjectJson(projectIdUri(PROJECT1), updateRequest),
        jsonPath("$", project(expectedProject)));
  }

  @Test
  public void delete_unknownProject_notFound() {
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectNotFound(delete(projectIdUri(3456)));
  }

  @Test
  public void delete_readAccess_forbidden() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectForbidden(delete(projectIdUri(PROJECT2)));
  }

  @Test
  public void delete_readWriteAccess_forbidden() {
    standardFixtures();
    login(EMAIL_READ_WRITE_PROJECT_1);
    expectForbidden(delete(projectIdUri(PROJECT1)));
  }

  @Test(expected = NotFoundException.class)
  public void delete_owner_ok() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    authorizeComputeService(PROJECT2, EMAIL_OWNER_PROJECT_2);
    expectOk(delete(projectIdUri(PROJECT2)));
    getEntity(project2.getKey());
  }

  @Test(expected = NotFoundException.class)
  public void delete_noNetworkAndNoCredentials_ok() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    expectOk(delete(projectIdUri(PROJECT2)));
    getEntity(project2.getKey());
  }

  @Test
  public void delete_withRunningInstanceAndNoCredentials_unauthorized() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    Instance bidder = new Instance();
    bidder.setName(BIDDER1);
    addInstance(PROJECT1, bidder);
    expectUnauthorized(delete(projectIdUri(PROJECT1)));
  }

  @Test
  public void delete_withRunningInstance_badRequest() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    authorizeComputeService(PROJECT1, EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    Zone zone = buildZone(API_PROJECT1, ZONE1);
    addZone(PROJECT1, zone);
    Instance bidder = new Instance();
    bidder.setName(BIDDER1);
    bidder.setZone(ZONE1);
    addInstance(PROJECT1, bidder);
    expectBadRequest(delete(projectIdUri(PROJECT1)));
  }

  @Test(expected = NotFoundException.class)
  public void delete_withFirewallAndNetwork_ok() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    authorizeComputeService(PROJECT1, EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    addNetwork(PROJECT1, NETWORK_NAME1);
    Firewall firewall = new Firewall();
    firewall.setName(FIREWALL_NAME1);
    addFirewall(PROJECT1, firewall);
    expectOk(delete(projectIdUri(PROJECT1)));
    getEntity(project1.getKey());
  }

  private void addInstance(String projectName, Instance instance) {
    getComputeClient(projectName).addInstance(instance);
  }

  private void addZone(String projectName,Zone zone) {
    getComputeClient(projectName).addZone(zone);
  }

  private void addNetwork(String projectName, String networkName) {
    Project project = getProject(projectName);
    Network network = new Network();
    network.setName(networkName);
    getComputeClient(project).addNetwork(network);
  }

  private void addFirewall(String projectName, Firewall firewall) {
    getComputeClient(projectName).addFirewall(firewall);
  }

  private String projectCollectionUri() {
    return ResourceType.PROJECT.getResourceCollectionId().getResourceUri();
  }

  private String projectIdUri(String projectName) {
    return projectIdUri(getProject(projectName).getId());
  }

  private String projectIdUri(long projectId) {
    return ResourceType.PROJECT
        .getResourceId(Long.toString(projectId))
        .getResourceUri();
  }

  private String setAsDefaultUri(long projectId) {
    return projectIdUri(projectId) + "/defaultProject";
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void testListProjects(String email, String... projectNames) {
    standardFixtures();
    login(email);
    final Map<String, Project> projectMap = getAllProjects();
    Collection<Project> projects = Collections2.transform(Arrays.asList(projectNames),
        new Function<String, Project>() {
          @Override
          public Project apply(String projectName) {
            return Preconditions.checkNotNull(projectMap.get(projectName),
                "Project '%s' not found", projectName);
          }
        });
    expectJson(get(projectCollectionUri()),
        jsonPath("$").isArray(),
        jsonPath("$", hasSize(projectNames.length)),
        // There seems to be no "safe" way of calling the Collection rather than T... items
        // overloaded containsInAnyOrder().
        // See http://code.google.com/p/hamcrest/issues/detail?id=188
        jsonPath("$", containsInAnyOrder((Collection) projects(projects))));
  }

  private void verifyResponse(
      ProjectResource expected,
      ProjectResource actual,
      boolean networkExists) {

    assertNotNull(actual.getId());
    assertEquals(ResourceType.PROJECT, actual.getId().getResourceType());
    long projectId = Long.parseLong(actual.getId().getResourceName());
    Project project = getEntity(Project.key(projectId));
    assertEquals(ResourceType.PROJECT, actual.getResourceType());
    assertEquals(expected.getDescription(), actual.getDescription());
    assertEquals(expected.getDescription(), project.getProjectName());
    assertEquals(expected.getOauth2ClientId(), actual.getOauth2ClientId());
    assertEquals(expected.getOauth2ClientId(), project.getOauth2ClientId());
    assertEquals(expected.getOauth2ClientSecret(), actual.getOauth2ClientSecret());
    assertEquals(expected.getOauth2ClientSecret(), project.getOauth2ClientSecret());
    assertEquals(expected.getApiProjectId(), actual.getApiProjectId());
    assertEquals(expected.getApiProjectId(), project.getApiProjectId());
    assertEquals(expected.getApiProjectNumber(), actual.getApiProjectNumber());
    assertEquals(expected.getApiProjectNumber(), project.getApiProjectNumber());
    assertEquals(expected.getVmParameters(), actual.getVmParameters());
    assertEquals(expected.getVmParameters(), project.getVmParameters());
    assertEquals(expected.getMainParameters(), actual.getMainParameters());
    assertEquals(expected.getMainParameters(), project.getMainParameters());
    assertEquals(expected.getBidInterceptors(), actual.getBidInterceptors());
    assertEquals(expected.getBidInterceptors(), project.getBidInterceptors());
    assertEquals(expected.getUserDistUri(), actual.getUserDistUri());
    assertEquals(expected.getUserDistUri(), project.getUserDistUri());
    if (Strings.isNullOrEmpty(project.getNetworkName()) || !networkExists) {
      assertNull(actual.getNetwork());
    } else {
      assertEquals(actual.getNetwork(),
          actual.getId().getChildResource(ResourceType.NETWORK, project.getNetworkName()));
    }
    assertEquals(actual.getProjectUuid(), project.getProjectUuid());
    assertNotNull(project.getProjectUuid());
    if (expected.hasDoubleClickProjectResource()) {
      assertEquals(expected.getDoubleClickProjectResource().getDoubleClickReportingBucket(),
          project.getDoubleClickReportingBucket());
      assertEquals(expected.getDoubleClickProjectResource().getDoubleClickReportingBucket(),
          project.getDoubleClickReportingBucket());
    }
  }

  private Network buildNetwork(String projectName) {
    Project project = getProject(projectName);
    Network network = new Network();
    network.setName(project.getNetworkName());
    network.setCreationTimestamp(clock.now().toString());
    network.setDescription("Created in functional test");
    network.setSelfLink(ComputeResourceType.NETWORK.buildName(
        project.getApiProjectId(), project.getNetworkName()).getResourceUrl());
    network.setIPv4Range("10.0.0.0/8");
    return network;
  }


  private Zone buildZone(String apiProjectId, String name) {
    Zone zone = new Zone();
    zone.setName(name);
    zone.setDescription(name);
    zone.setSelfLink(ComputeResourceType.ZONE.buildName(apiProjectId, name).getResourceUrl());
    return zone;
  }

  private static ProjectResource completeProject() {
    ProjectResource projectResource = new ProjectResource();
    projectResource.setId(ResourceType.PROJECT.getResourceId(PROJECT_ID));
    projectResource.setDescription(PROJECT_NAME);
    projectResource.setOauth2ClientId(OAUTH_2_CLIENT_ID);
    projectResource.setOauth2ClientSecret(OAUTH_2_CLIENT_SECRET);
    projectResource.setApiProjectId(API_PROJECT_ID);
    projectResource.setApiProjectNumber(API_PROJECT_NUMBER);
    projectResource.setVmParameters(VM_PARAMETERS);
    projectResource.setMainParameters(MAIN_PARAMETERS);
    projectResource.setBidInterceptors(BIDDER_INTERCEPTORS);
    projectResource.setUserDistUri(USER_DIST_URI);
    projectResource.setWhiteListedIpRanges(WHITE_LISTED_IP_RANGES);
    projectResource.setNetwork(ResourceType.NETWORK.getResourceId("123", "network-123"));
    projectResource.setProjectUuid(PROJECT_UUID);
    projectResource.setDoubleClickPreferredDealsBucket(DOUBLE_CLICK_PREFERRED_DEALS_BUCKET);
    DoubleClickProjectResource doubleClickProjectResource = new DoubleClickProjectResource();
    doubleClickProjectResource.setDoubleClickReportingBucket(DOUBLE_CLICK_REPORTING_BUCKET);
    projectResource.setDoubleClickProjectResource(doubleClickProjectResource);
    return projectResource;
  }
}
