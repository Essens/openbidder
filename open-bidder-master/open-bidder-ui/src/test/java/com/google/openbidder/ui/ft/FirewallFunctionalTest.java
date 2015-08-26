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

import static com.google.openbidder.ui.resource.ResourceMatchers.firewall;
import static com.google.openbidder.ui.resource.ResourceMatchers.firewalls;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.google.api.services.compute.model.Firewall;
import com.google.openbidder.ui.compute.BidderInstanceBuilder;
import com.google.openbidder.ui.compute.ComputeResourceType;
import com.google.openbidder.ui.compute.FakeComputeClient;
import com.google.openbidder.ui.compute.ResourceName;
import com.google.openbidder.ui.resource.support.ResourceType;
import com.google.openbidder.ui.util.WebContextLoader;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Collection;

/**
 * Firewall resource tests.
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
public class FirewallFunctionalTest extends OpenBidderFunctionalTestCase {

  @Test
  public void get_noAccess_notFound() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    expectNotFound(get(firewallIdUri(PROJECT1, "firewall-1234")));
  }

  @Test
  public void get_noCredentials_unauthorized() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    expectUnauthorized(get(firewallIdUri(PROJECT2, "firewall-1234")));
  }

  @Test
  public void get_noNetwork_notFound() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    authorizeComputeService(PROJECT2, EMAIL_OWNER_PROJECT_2);
    expectNotFound(get(firewallIdUri(PROJECT2, "firewall-1234")));
  }

  @Test
  public void get_firewallNotFound_notFound() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    authorizeComputeService(PROJECT1, EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectNotFound(get(firewallIdUri(PROJECT1, FIREWALL_NAME1)));
  }

  @Test
  public void get_firewallDoesNotMatchNetwork_notFound() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    authorizeComputeService(PROJECT1, EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    Firewall firewall = buildFirewall(API_PROJECT1, NETWORK_NAME2, FIREWALL_NAME1);
    addFirewalls(PROJECT1, firewall);
    expectNotFound(get(firewallIdUri(PROJECT1, FIREWALL_NAME1)));
  }

  @Test
  public void get_firewallFound_ok() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    authorizeComputeService(PROJECT1, EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    Firewall firewall = buildFirewall(API_PROJECT1, NETWORK_NAME1, FIREWALL_NAME1);
    addFirewalls(PROJECT1, firewall);
    expectJson(get(firewallIdUri(PROJECT1, FIREWALL_NAME1)),
        jsonPath("$", firewall(project1, firewall)));
  }

  @Test
  public void list_noAccess_notFound() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    expectNotFound(get(firewallCollectionUri(PROJECT1)));
  }

  @Test
  public void list_noCredentials_unauthorized() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectUnauthorized(get(firewallCollectionUri(PROJECT1)));
  }

  @Test
  public void list_noNetwork_ok() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    authorizeComputeService(PROJECT2, EMAIL_OWNER_PROJECT_2);
    expectJson(get(firewallCollectionUri(PROJECT2)),
        jsonPath("$").isArray(), jsonPath("$", hasSize(0)));
  }

  @Test
  public void list_noFirewalls_ok() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    authorizeComputeService(PROJECT1, EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectJson(get(firewallCollectionUri(PROJECT1)),
        jsonPath("$").isArray(), jsonPath("$", hasSize(0)));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  public void list_twoFirewalls_ok() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    authorizeComputeService(PROJECT1, EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    Firewall firewall1 = buildFirewall(API_PROJECT1, NETWORK_NAME1, FIREWALL_NAME1);
    Firewall firewall2 = buildFirewall(API_PROJECT1, NETWORK_NAME1, FIREWALL_NAME2);
    addFirewalls(PROJECT1, firewall1, firewall2);
    expectJson(get(firewallCollectionUri(PROJECT1)),
        jsonPath("$").isArray(),
        jsonPath("$", containsInAnyOrder(
            (Collection) firewalls(project1, firewall1, firewall2))));
  }

  @Test
  public void postJson_loggedIn_methodNotAllowed() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectMethodNotAllowed(postObjectJson(firewallCollectionUri(PROJECT1), emptyRequest()));
  }

  @Test
  public void postForm_loggedIn_methodNotAllowed() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectMethodNotAllowed(post(firewallCollectionUri(PROJECT1)));
  }

  @Test
  public void put_loggedIn_methodNotAllowed() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectMethodNotAllowed(putObjectJson(firewallIdUri(PROJECT1, "firewall-1234"), emptyRequest()));
  }

  @Test
  public void delete_readAccess_forbidden() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    authorizeComputeService(PROJECT2, EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectForbidden(delete(firewallIdUri(PROJECT2, "firewall-1234")));
  }

  @Test
  public void delete_noNetworkOnProject_notFound() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    authorizeComputeService(PROJECT2, EMAIL_OWNER_PROJECT_2);
    expectNotFound(delete(firewallIdUri(PROJECT2, "firewall-1234")));
  }

  @Test
  public void delete_noCredentials_unauthorized() {
    standardFixtures();
    login(EMAIL_READ_WRITE_PROJECT_1);
    expectUnauthorized(delete(firewallIdUri(PROJECT1, "firewall-1234")));
  }

  @Test
  public void delete_doesNotMatchProjectNetwork_notFound() {
    standardFixtures();
    login(EMAIL_READ_WRITE_PROJECT_1);
    authorizeComputeService(PROJECT1, EMAIL_READ_WRITE_PROJECT_1);
    Firewall firewall = buildFirewall(API_PROJECT1, NETWORK_NAME2, FIREWALL_NAME1);
    addFirewalls(PROJECT1, firewall);
    expectNotFound(delete(firewallIdUri(PROJECT1, FIREWALL_NAME1)));
  }

  @Test
  public void delete_firewallExists_ok() {
    standardFixtures();
    login(EMAIL_READ_WRITE_PROJECT_1);
    authorizeComputeService(PROJECT1, EMAIL_READ_WRITE_PROJECT_1);
    Firewall firewall = buildFirewall(API_PROJECT1, NETWORK_NAME1, FIREWALL_NAME1);
    addFirewalls(PROJECT1, firewall);
    expectOk(delete(firewallIdUri(PROJECT1, FIREWALL_NAME1)));
    verifyDeleteFirewall(PROJECT1, FIREWALL_NAME1);
  }

  private String firewallIdUri(String projectName, String firewallName) {
    return firewallIdUri(getProject(projectName).getId(), firewallName);
  }

  private String firewallIdUri(long projectId, String firewallName) {
    return ResourceType.FIREWALL
        .getResourceId(Long.toString(projectId), firewallName)
        .getResourceUri();
  }

  private String firewallCollectionUri(String projectName) {
    return firewallCollectionUri(getProject(projectName).getId());
  }

  private String firewallCollectionUri(long projectId) {
    return ResourceType.FIREWALL
        .getResourceCollectionId(Long.toString(projectId))
        .getResourceUri();
  }

  private Firewall buildFirewall(
      String apiProjectId,
      String networkName,
      String firewallName) {

    Firewall firewall = new Firewall();
    firewall.setName(firewallName);
    firewall.setDescription("Created in functional test");
    firewall.setCreationTimestamp(clock.now().toString());
    firewall.setSelfLink(ComputeResourceType.FIREWALL.buildName(
        apiProjectId, firewallName).getResourceUrl());
    ResourceName resourceName = ComputeResourceType.NETWORK.buildName(
        apiProjectId, networkName);
    firewall.setNetwork(resourceName.getResourceUrl());
    firewall.setSourceRanges(Arrays.asList("203.59.0.0/16"));
    firewall.setTargetTags(Arrays.asList(BidderInstanceBuilder.TAG));
    Firewall.Allowed allowed = new Firewall.Allowed();
    allowed.setIPProtocol("tcp");
    allowed.setPorts(Arrays.asList("80", "443", "9876"));
    firewall.setAllowed(Arrays.asList(allowed));
    return firewall;
  }

  private void addFirewalls(String projectName, Firewall... firewalls) {
    getComputeClient(projectName).addAllFirewalls(firewalls);
  }

  private void verifyDeleteFirewall(String projectName, String firewallName) {
    FakeComputeClient computeClient = getComputeClient(projectName);
    assertNull(computeClient.getFirewallDirect(firewallName));
  }
}
