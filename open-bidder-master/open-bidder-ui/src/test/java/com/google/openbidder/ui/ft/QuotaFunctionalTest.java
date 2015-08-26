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

import static com.google.openbidder.ui.resource.ResourceMatchers.quotas;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.google.api.services.compute.model.Project;
import com.google.api.services.compute.model.Quota;
import com.google.openbidder.ui.resource.support.ResourceType;
import com.google.openbidder.ui.util.WebContextLoader;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Collection;

/**
 * Project quota resource tests.
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
public class QuotaFunctionalTest extends OpenBidderFunctionalTestCase {

  @Test
  public void get_loggedIn_methodNotAllowed() {
    login(EMAIL_OWNER_PROJECT_2);
    expectMethodNotAllowed(get(quotaIdUri(1234, "cpus")));
  }

  @Test
  public void list_noAccess_notFound() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    expectNotFound(get(quotaCollectionUri(PROJECT1)));
  }

  @Test
  public void list_noCredentials_unauthorized() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectUnauthorized(get(quotaCollectionUri(PROJECT1)));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  public void list_quotas_ok() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    authorizeComputeService(PROJECT1, EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    Project project = new Project();
    Quota visibleQuota = buildQuota("CPUS", 3, 11);
    Quota filteredQuota = buildQuota("TEAPOTS", 7, 9);
    project.setQuotas(Arrays.asList(visibleQuota, filteredQuota));
    getComputeClient(PROJECT1).setProject(project);
    expectJson(get(quotaCollectionUri(PROJECT1),
        jsonPath("$").isArray(),
        jsonPath("$", hasSize(1)),
        jsonPath("$", containsInAnyOrder((Collection) quotas(project1.getId(), visibleQuota)))));
  }

  @Test
  public void postJson_loggedIn_methodNotAllowed() {
    login(EMAIL_OWNER_PROJECT_2);
    expectMethodNotAllowed(postObjectJson(quotaCollectionUri(1234), emptyRequest()));
  }

  @Test
  public void put_loggedIn_methodNotAllowed() {
    login(EMAIL_OWNER_PROJECT_2);
    expectMethodNotAllowed(putObjectJson(quotaIdUri(1234, "memory"), emptyRequest()));
  }

  @Test
  public void delete_loggedIn_methodNotAllowed() {
    login(EMAIL_OWNER_PROJECT_2);
    expectMethodNotAllowed(delete(quotaIdUri(1234, "cpus")));
  }

  private String quotaIdUri(long projectId, String quotaName) {
    return ResourceType.QUOTA
        .getResourceId(Long.toString(projectId), quotaName)
        .getResourceUri();
  }

  private String quotaCollectionUri(String projectName) {
    return quotaCollectionUri(getProject(projectName).getId());
  }

  private String quotaCollectionUri(long projectId) {
    return ResourceType.QUOTA
        .getResourceCollectionId(Long.toString(projectId))
        .getResourceUri();
  }

  private Quota buildQuota(String metric, double usage, double limit) {
    Quota quota = new Quota();
    quota.setMetric(metric);
    quota.setUsage(usage);
    quota.setLimit(limit);
    return quota;
  }
}
