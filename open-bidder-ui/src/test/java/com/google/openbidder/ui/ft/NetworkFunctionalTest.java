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

import static com.google.openbidder.ui.resource.ResourceMatchers.network;
import static com.google.openbidder.ui.resource.ResourceMatchers.networks;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.google.api.services.compute.model.Network;
import com.google.common.collect.ImmutableList;
import com.google.openbidder.ui.compute.ComputeResourceType;
import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.resource.model.NetworkResource;
import com.google.openbidder.ui.resource.support.ResourceType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Collection;

import javax.annotation.Nullable;

/**
 * Network resource tests.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
    locations = {
        "file:src/main/webapp/WEB-INF/applicationContext.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-security.xml",
        "file:src/main/webapp/WEB-INF/ui-servlet.xml",
        "classpath:/bean-overrides.xml"
    })
@WebAppConfiguration
public class NetworkFunctionalTest extends OpenBidderFunctionalTestCase {

  private static final ImmutableList<String> WHITE_LISTED_IP_RANGES =
      ImmutableList.of("192.168.0.0/16", "10.0.0.0/8");

  @Test
  public void get_noAccess_notFound() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    expectNotFound(get(networkIdUri(PROJECT1, "network-1")));
  }

  @Test
  public void get_noCredentials_unauthorized() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    expectUnauthorized(get(networkIdUri(PROJECT2, "network-1")));
  }

  @Test
  public void get_noNetwork_notFound() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    authorizeComputeService(PROJECT2, EMAIL_OWNER_PROJECT_2);
    expectNotFound(get(networkIdUri(PROJECT2, NETWORK_NAME1)));
  }

  @Test
  public void get_networkNotFound_notFound() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    authorizeComputeService(PROJECT1, EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectNotFound(get(networkIdUri(PROJECT1, NETWORK_NAME1)));
  }

  @Test
  public void get_networkFound_ok() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    authorizeComputeService(PROJECT1, EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    Network network = addNetwork(PROJECT1, NETWORK_NAME1);
    expectJson(get(networkIdUri(PROJECT1, NETWORK_NAME1)),
        jsonPath("$", network(project1.getId(), network)));
  }

  @Test
  public void list_noAccess_notFound() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    expectNotFound(get(networkCollectionUri(PROJECT1)));
  }

  @Test
  public void list_noCredentials_unauthorized() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectUnauthorized(get(networkCollectionUri(PROJECT1)));
  }

  @Test
  public void list_noNetwork_ok() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    authorizeComputeService(PROJECT2, EMAIL_OWNER_PROJECT_2);
    expectJson(get(networkCollectionUri(PROJECT2)),
        jsonPath("$").isArray(), jsonPath("$", hasSize(0)));
  }

  @Test
  public void list_networkDoesNotExist_ok() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    authorizeComputeService(PROJECT1, EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectJson(get(networkCollectionUri(PROJECT1)),
        jsonPath("$").isArray(), jsonPath("$", hasSize(0)));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  public void list_networkExists_ok() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    authorizeComputeService(PROJECT1, EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    Network network = addNetwork(PROJECT1, NETWORK_NAME1);
    expectJson(get(networkCollectionUri(PROJECT1)),
        jsonPath("$").isArray(),
        jsonPath("$", containsInAnyOrder(
            (Collection) networks(project1.getId(), network))));
  }

  @Test
  public void postJson_readAccess_forbidden() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    authorizeComputeService(PROJECT2, EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectForbidden(postObjectJson(networkCollectionUri(PROJECT2), emptyRequest()));
  }

  @Test
  public void postJson_noCredentials_unauthorized() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectUnauthorized(postObjectJson(networkCollectionUri(PROJECT1), emptyRequest()));
  }

  @Test
  public void postJson_noNetworkAsOwner_ok() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    authorizeComputeService(PROJECT2, EMAIL_OWNER_PROJECT_2);
    NetworkResource actualNetworkResource = expectJson(
        postObjectJson(networkCollectionUri(PROJECT2), completeRequest()),
        NetworkResource.class);
    verifyNetwork(PROJECT2, EMAIL_OWNER_PROJECT_2, actualNetworkResource);
  }

  @Test
  public void postJson_networkDoesNotExistWithReadWrite_ok() {
    standardFixtures();
    login(EMAIL_READ_WRITE_PROJECT_1);
    authorizeComputeService(PROJECT1, EMAIL_READ_WRITE_PROJECT_1);
    NetworkResource actualNetworkResource = expectJson(
        postObjectJson(networkCollectionUri(PROJECT1), completeRequest()),
        NetworkResource.class,
        jsonPath("$.firewalls", hasSize(2)));
    verifyNetwork(PROJECT1, EMAIL_READ_WRITE_PROJECT_1, actualNetworkResource);
  }

  @Test
  public void postJson_networkQuotaExceeded_conflict() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    authorizeComputeService(PROJECT1, EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    addNetwork(PROJECT1, "dummy-network");
    getComputeClient(PROJECT1).setQuota(ComputeResourceType.NETWORK, 1);
    expectConflict(postObjectJson(networkCollectionUri(PROJECT1), completeRequest()));
  }

  @Test
  public void postJson_networkExistsAsOwner_ok() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    authorizeComputeService(PROJECT1, EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    addNetwork(PROJECT1);
    NetworkResource actualNetworkResource = expectJson(
        postObjectJson(networkCollectionUri(PROJECT1), completeRequest()),
        NetworkResource.class);
    verifyNetwork(PROJECT1, EMAIL_OWNER_PROJECT_1_READ_PROJECT_2, actualNetworkResource);
    project1 = getEntity(project1.getKey());
    assertEquals(WHITE_LISTED_IP_RANGES, project1.getWhiteListedIpRanges());
  }

  @Test
  public void postForm_readAccess_forbidden() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    authorizeComputeService(PROJECT2, EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectForbidden(post(networkCollectionUri(PROJECT2)));
  }

  @Test
  public void postForm_noCredentials_unauthorized() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectUnauthorized(post(networkCollectionUri(PROJECT1)));
  }

  @Test
  public void put_loggedIn_methodNotAllowed() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectMethodNotAllowed(putObjectJson(networkIdUri(PROJECT1, "network-1"), emptyRequest()));
  }

  @Test
  public void delete_readAccess_forbidden() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    authorizeComputeService(PROJECT2, EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectForbidden(delete(networkIdUri(PROJECT2, "network-1")));
  }

  @Test
  public void delete_noNetworkOnProject_notFound() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    authorizeComputeService(PROJECT2, EMAIL_OWNER_PROJECT_2);
    expectNotFound(delete(networkIdUri(PROJECT2, "network-1")));
  }

  @Test
  public void delete_noCredentials_unauthorized() {
    standardFixtures();
    login(EMAIL_READ_WRITE_PROJECT_1);
    expectUnauthorized(delete(networkIdUri(PROJECT1, "network-1")));
  }

  @Test
  public void delete_doesNotMatchProjectNetwork_notFound() {
    standardFixtures();
    login(EMAIL_READ_WRITE_PROJECT_1);
    authorizeComputeService(PROJECT1, EMAIL_READ_WRITE_PROJECT_1);
    expectNotFound(delete(networkIdUri(PROJECT1, "network-1")));
  }

  @Test
  public void delete_projectNetworkDoesNotExist_notFound() {
    standardFixtures();
    login(EMAIL_READ_WRITE_PROJECT_1);
    authorizeComputeService(PROJECT1, EMAIL_READ_WRITE_PROJECT_1);
    expectNotFound(delete(networkIdUri(PROJECT1, NETWORK_NAME1)));
  }

  @Test
  public void delete_networkExists_notFound() {
    standardFixtures();
    login(EMAIL_READ_WRITE_PROJECT_1);
    authorizeComputeService(PROJECT1, EMAIL_READ_WRITE_PROJECT_1);
    expectNotFound(delete(networkIdUri(PROJECT1, NETWORK_NAME1)));
    verifyDeleteNetwork(PROJECT1);
  }

  private String networkIdUri(String projectName, String networkName) {
    return networkIdUri(getProject(projectName).getId(), networkName);
  }

  private String networkIdUri(long projectId, String networkName) {
    return ResourceType.NETWORK
        .getResourceId(Long.toString(projectId), networkName)
        .getResourceUri();
  }

  private String networkCollectionUri(String projectName) {
    return networkCollectionUri(getProject(projectName).getId());
  }

  private String networkCollectionUri(long projectId) {
    return ResourceType.NETWORK
        .getResourceCollectionId(Long.toString(projectId))
        .getResourceUri();
  }

  private Network buildNetwork(String apiProjectId, String name) {
    Network network = new Network();
    network.setName(name);
    network.setCreationTimestamp(clock.now().toString());
    network.setDescription("Created in functional test");
    network.setSelfLink(ComputeResourceType.NETWORK.buildName(apiProjectId, name).getResourceUrl());
    network.setIPv4Range("10.0.0.0/8");
    return network;
  }

  private NetworkResource completeRequest() {
    NetworkResource request = new NetworkResource();
    request.setId(ResourceType.NETWORK.getResourceId(Long.toString(project1.getId()), "foobar"));
    request.setDescription("There's a hole in my bucket");
    request.setIpv4Range("224.0.0.0/8");
    request.setCreatedAt(clock.now());
    request.setWhiteListedIpRanges(WHITE_LISTED_IP_RANGES);
    return request;
  }

  private Network addNetwork(String projectName) {
    return addNetwork(projectName, /* network name */ null);
  }

  private Network addNetwork(String projectName, @Nullable String networkName) {
    Project project = getProject(projectName);
    String goodNetworkName = (networkName == null) ? project.getNetworkName() : networkName;
    Network network = buildNetwork(project.getApiProjectId(), goodNetworkName);
    getComputeClient(project).addNetwork(network);
    return network;
  }

  private void verifyNetwork(String projectName, String email,
      NetworkResource actualNetworkResource) {
    Project actualProject = getEntity(Project.key(getProject(projectName).getId()));
    assertEquals(ResourceType.NETWORK, actualNetworkResource.getId().getResourceType());
    assertEquals(actualProject.getNetworkName(), actualNetworkResource.getId().getResourceName());
    assertTrue(
        String.format("Description '%s' should contain email '%s'",
            actualNetworkResource.getDescription(),
            email),
        actualNetworkResource.getDescription().contains(email));
    assertNotNull(actualNetworkResource.getIpv4Range());
    assertNotNull(actualNetworkResource.getCreatedAt());
  }

  private void verifyDeleteNetwork(String projectName) {
    Project project = getProject(projectName);
    assertNull(getComputeClient(project).getNetworkDirect(project.getNetworkName()));
  }
}
