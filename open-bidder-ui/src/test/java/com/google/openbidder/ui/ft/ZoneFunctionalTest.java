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

import static com.google.openbidder.ui.resource.ResourceMatchers.zone;
import static com.google.openbidder.ui.resource.ResourceMatchers.zones;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.google.api.services.compute.model.Zone;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.openbidder.ui.compute.ComputeResourceType;
import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.resource.support.ResourceType;
import com.google.openbidder.ui.util.RegionMatcher;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Tests for {@link com.google.openbidder.ui.controller.ZoneController}.
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
public class ZoneFunctionalTest extends OpenBidderFunctionalTestCase {

  private static final String BEGIN_TIME = "2013-01-20T08:00:00.000";
  private static final String END_TIME = "2013-02-03T08:00:00.000";
  private static final String WINDOW_NAME = "downtime #1";
  private static final String WINDOW_DESCRIPTION = "planned maintenance";
  private static final String ZONE_HOST = "www.example.com";

  @Test
  public void get_projectDoesNotExist_notFound() {
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectNotFound(get(zoneIdUri(1234, ZONE1)));
  }

  @Test
  public void get_noCredentials_unauthorized() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectUnauthorized(get(zoneIdUri(PROJECT1, ZONE1)));
  }

  @Test
  public void get_zoneDoesNotExist_ok() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    authorizeComputeService(PROJECT1, EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectNotFound(get(zoneIdUri(PROJECT1, ZONE1)));
  }

  @Test
  public void get_zoneExists_ok() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    authorizeComputeService(PROJECT1, EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    Zone zone = buildZone(API_PROJECT1, ZONE1);
    addZones(PROJECT1, zone);
    expectJson(get(zoneIdUri(PROJECT1, ZONE1)),
        jsonPath("$", zone(project1.getId(), zone)));
  }

  @Test
  public void get_zoneWithMaintenanceWindow_ok() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    authorizeComputeService(PROJECT1, EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    Zone zone = buildZone(API_PROJECT1, ZONE1, standardWindow());
    addZones(PROJECT1, zone);
    expectJson(get(zoneIdUri(PROJECT1, ZONE1)),
        jsonPath("$", zone(project1.getId(), zone)));
  }

  @Test
  public void list_projectDoesNotExist_notFound() {
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectNotFound(get(zoneCollectionUri(1234)));
  }

  @Test
  public void list_noCredentials_unauthorized() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectUnauthorized(get(zoneCollectionUri(PROJECT1)));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  public void list_twoZones_ok() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    authorizeComputeService(PROJECT1, EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    List<Zone> zones = buildZones(API_PROJECT1, ZONE1, ZONE2);
    addZones(PROJECT1, zones);
    expectJson(get(zoneCollectionUri(PROJECT1)),
        jsonPath("$").isArray(),
        jsonPath("$", hasSize(zones.size())),
        jsonPath("$", containsInAnyOrder((Collection) zones(project1.getId(), zones))));
  }

  @Test
  public void postJson_loggedIn_methodNotAllowed() {
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectMethodNotAllowed(postObjectJson(zoneCollectionUri(3456), emptyRequest()));
  }

  @Test
  public void postForm_loggedIn_methodNotAllowed() {
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectMethodNotAllowed(post(zoneCollectionUri(3456)));
  }

  @Test
  public void put_unknownProject_notFound() {
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectNotFound(putObjectJson(zoneIdUri(1234, ZONE1), emptyRequest()));
  }

  @Test
  public void put_noProjectAccess_notFound() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    expectNotFound(putObjectJson(zoneIdUri(PROJECT1, ZONE1), emptyRequest()));
  }

  @Test
  public void put_noCredentials_unauthorized() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    expectUnauthorized(putObjectJson(zoneIdUri(PROJECT2, ZONE1), emptyRequest()));
  }

  @Test
  public void put_readAccess_forbidden() {
    standardFixtures();
    authorizeComputeService(PROJECT2, EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectForbidden(putObjectJson(zoneIdUri(PROJECT2, ZONE1), emptyRequest()));
  }

  @Test
  public void put_withWrite_ok() {
    standardFixtures();
    authorizeComputeService(PROJECT2, EMAIL_OWNER_PROJECT_2);
    login(EMAIL_OWNER_PROJECT_2);
    Zone zone = buildZone(API_PROJECT2, ZONE1);
    addZones(PROJECT2, zone);
    ImmutableMap<String, Object> request = ImmutableMap.<String, Object>of(
        "hostName", ZONE_HOST);
    expectJson(putObjectJson(zoneIdUri(PROJECT2, ZONE1), request),
        jsonPath("$", zone(project2.getId(), zone, ZONE_HOST)));
    Project updatedProject = getEntity(project2.getKey());
    assertEquals(ZONE_HOST, updatedProject.getZoneHost(ZONE1));
  }

  @Test
  public void delete_loggedIn_methodNotAllowed() {
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectMethodNotAllowed(delete(zoneIdUri(2345, ZONE1)));
  }

  @Test
  public void test_RegionMatcher_ok() {
    assertEquals("ASIA", RegionMatcher.mapGceToDoubleClickRegion("rtb-asia-east1-a"));
    assertEquals("ASIA", RegionMatcher.mapGceToDoubleClickRegion("rtb-asia-east2-a"));
    assertEquals("ASIA", RegionMatcher.mapGceToDoubleClickRegion("rtb-asia-east2-b"));
    assertEquals("EUROPE", RegionMatcher.mapGceToDoubleClickRegion("rtb-europe-west1-a"));
    assertEquals("EUROPE", RegionMatcher.mapGceToDoubleClickRegion("rtb-europe-west1-b"));
    assertEquals("US_EAST", RegionMatcher.mapGceToDoubleClickRegion("rtb-us-east1-a"));
    assertEquals("US_EAST", RegionMatcher.mapGceToDoubleClickRegion("rtb-us-east2-a"));
    assertEquals("US_EAST", RegionMatcher.mapGceToDoubleClickRegion("rtb-us-east3-a"));
    assertEquals("US_EAST", RegionMatcher.mapGceToDoubleClickRegion("rtb-us-east2-b"));
    assertEquals("US_WEST", RegionMatcher.mapGceToDoubleClickRegion("rtb-us-west1-a"));
    assertEquals("US_WEST", RegionMatcher.mapGceToDoubleClickRegion("rtb-us-west1-b"));
    assertEquals("US_EAST", RegionMatcher.mapGceToDoubleClickRegion("us/east/a"));
    assertEquals("US_EAST", RegionMatcher.mapGceToDoubleClickRegion("us/central/b"));
  }

  private String zoneIdUri(String projectName, String zoneName) {
    return zoneIdUri(getProject(projectName).getId(), zoneName);
  }

  private String zoneIdUri(long projectId, String zoneName) {
    return ResourceType.ZONE
        .getResourceId(Long.toString(projectId), zoneName)
        .getResourceUri();
  }

  private String zoneCollectionUri(String projectName) {
    return zoneCollectionUri(getProject(projectName).getId());
  }

  private String zoneCollectionUri(long projectId) {
    return ResourceType.ZONE
        .getResourceCollectionId(Long.toString(projectId))
        .getResourceUri();
  }

  private List<Zone> buildZones(final String apiProjectId, String... names) {
    return Lists.transform(asList(names), new Function<String, Zone>() {
      @Override public Zone apply(String name) {
        return buildZone(apiProjectId, name);
      }});
  }

  private Zone buildZone(String apiProjectId, String name) {
    return buildZone(apiProjectId, name, /* maintenance window */ null);
  }

  private Zone buildZone(
      String apiProjectId,
      String name,
      @Nullable Zone.MaintenanceWindows maintenanceWindows) {

    Zone zone = new Zone();
    zone.setName(name);
    zone.setDescription(name);
    zone.setSelfLink(ComputeResourceType.ZONE.buildName(apiProjectId, name).getResourceUrl());
    if (maintenanceWindows != null) {
      zone.setMaintenanceWindows(Collections.singletonList(maintenanceWindows));
    }
    return zone;
  }

  private Zone.MaintenanceWindows standardWindow() {
    Zone.MaintenanceWindows maintenanceWindow = new Zone.MaintenanceWindows();
    maintenanceWindow.setName(WINDOW_NAME);
    maintenanceWindow.setDescription(WINDOW_DESCRIPTION);
    maintenanceWindow.setBeginTime(BEGIN_TIME);
    maintenanceWindow.setEndTime(END_TIME);
    return maintenanceWindow;
  }

  private void addZones(String projectName, Zone... zones) {
    getComputeClient(projectName).addAllZones(zones);
  }

  private void addZones(String projectName, Iterable<Zone> zones) {
    getComputeClient(projectName).addAllZones(zones);
  }
}
