/*
 * Copyright 2013 Google Inc. All Rights Reserved.
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

import static com.google.openbidder.ui.resource.ResourceMatchers.region;
import static com.google.openbidder.ui.resource.ResourceMatchers.regions;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.google.api.services.compute.model.Quota;
import com.google.api.services.compute.model.Region;
import com.google.openbidder.ui.compute.ComputeResourceType;
import com.google.openbidder.ui.compute.ResourceName;
import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.resource.support.ResourceType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Collection;

/**
 * Region resource test.
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
public class RegionFunctionalTest extends OpenBidderFunctionalTestCase {

  @Test
  public void get_noAccess_noFound() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    expectNotFound(get(regionIdUri(PROJECT1, "region-1")));
  }

  @Test
  public void get_noCredentials_unauthorized() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    expectUnauthorized(get(regionIdUri(PROJECT2, REGION1)));
  }

  @Test
  public void get_notFound_notFound() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    authorizeComputeService(PROJECT2, EMAIL_OWNER_PROJECT_2);
    expectNotFound(get(regionIdUri(PROJECT2, REGION1)));
  }

  @Test
  public void get_found_ok() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    authorizeComputeService(PROJECT2, EMAIL_OWNER_PROJECT_2);
    Region region = addRegion(PROJECT2, REGION1);
    expectJson(get(regionIdUri(PROJECT2, REGION1)),
        jsonPath("$", region(project2.getId(), region)));
  }

  @Test
  public void list_noAccess_notFound() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    expectNotFound(get(regionCollectionUri(PROJECT1)));
  }

  @Test
  public void list_noCredentials_unauthorized() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    expectUnauthorized(get(regionCollectionUri(PROJECT2)));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  public void list_found_ok() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    authorizeComputeService(PROJECT2, EMAIL_OWNER_PROJECT_2);
    Region region1 = buildRegion(API_PROJECT2, REGION1);
    Region region2 = buildRegion(API_PROJECT2, REGION2);
    addRegions(PROJECT2, region1, region2);
    expectJson(get(regionCollectionUri(PROJECT2)),
        jsonPath("$").isArray(),
        jsonPath("$", hasSize(2)),
        jsonPath("$", containsInAnyOrder(
            (Collection) regions(project2.getId(), region1, region2))));
  }

  @Test
  public void postJson_loggedIn_methodNotAllowed() {
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectMethodNotAllowed(postObjectJson(regionCollectionUri(1234), emptyRequest()));
  }

  @Test
  public void postForm_loggedIn_methodNotAllowed() {
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectMethodNotAllowed(post(regionCollectionUri(1234)));
  }

  @Test
  public void put_loggedIn_methodNotAllowed() {
    login(EMAIL_OWNER_PROJECT_2);
    expectMethodNotAllowed(putObjectJson(regionIdUri(1234, REGION1), emptyRequest()));
  }

  @Test
  public void delete_loggedIn_methodNotAllowed() {
    login(EMAIL_OWNER_PROJECT_2);
    expectMethodNotAllowed(delete(regionIdUri(1234, REGION2)));
  }

  private String regionIdUri(String projectName, String regionName) {
    return regionIdUri(getProject(projectName).getId(), regionName);
  }

  private String regionIdUri(long projectId, String regionName) {
    return ResourceType.REGION
        .getResourceId(Long.toString(projectId), regionName)
        .getResourceUri();
  }

  private String regionCollectionUri(String projectName) {
    return regionCollectionUri(getProject(projectName).getId());
  }

  private String regionCollectionUri(long projectId) {
    return ResourceType.REGION
        .getResourceCollectionId(Long.toString(projectId))
        .getResourceUri();
  }

  private Region buildRegion(String apiProjectId, String regionName) {
    Region region = new Region();
    region.setName(regionName);
    region.setDescription("Test region");
    Quota visibleQuota = buildQuota("CPUS", 3, 11);
    region.setQuotas(asList(visibleQuota));
    ResourceName resourceName = ComputeResourceType.REGION.buildName(apiProjectId, regionName);
    region.setSelfLink(resourceName.getResourceUrl());
    return region;
  }

  private Region addRegion(String projectName, String regionName) {
    Project project = getProject(projectName);
    Region region = buildRegion(project.getApiProjectId(), regionName);
    getComputeClient(project).addRegion(region);
    return region;
  }

  private void addRegions(String projectName, Region... regions) {
    getComputeClient(projectName).addAllRegions(regions);
  }

  private Quota buildQuota(String metric, double usage, double limit) {
    Quota quota = new Quota();
    quota.setMetric(metric);
    quota.setUsage(usage);
    quota.setLimit(limit);
    return quota;
  }
}
