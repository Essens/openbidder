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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.openbidder.ui.resource.ResourceMatchers.reports;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.google.openbidder.ui.adexchangebuyer.DoubleClickUtils;
import com.google.openbidder.ui.cloudstorage.BucketObject;
import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.resource.model.ReportResource;
import com.google.openbidder.ui.resource.support.ResourceType;

import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Collection;

/**
 * Report resource tests.
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
public class ReportFunctionalTest extends OpenBidderFunctionalTestCase {
  private static final long REPORT_SIZE = 100L;

  @Test
  public void list_projectNotExist_notFound() {
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectNotFound(get(reportCollectionUri(1234)));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  public void list_reports_ok() {
    standardFixtures();
    project2.setDoubleClickReportingBucket("reporting-bucket");
    project2 = putAndGet(project2);
    login(EMAIL_OWNER_PROJECT_2);
    authorizeComputeService(PROJECT2, EMAIL_OWNER_PROJECT_2);
    ReportResource reportResource1 = addReportToBucket(PROJECT2, "perf1", REPORT_SIZE, NOW, true);
    ReportResource reportResource2 =
        addReportToBucket(PROJECT2, "snippet1", REPORT_SIZE, NOW, false);
    expectJson(get(reportCollectionUri(PROJECT2)),
        jsonPath("$").isArray(),
        jsonPath("$", hasSize(2)));
        jsonPath("$", containsInAnyOrder((Collection) reports(project2.getId(), reportResource1,
            reportResource2)));
  }

  @Test
  public void postJson_loggedIn_methodNotAllowed() {
    login(EMAIL_NO_PROJECTS);
    expectMethodNotAllowed(postObjectJson(reportCollectionUri(1234), emptyRequest()));
  }

  @Test
  public void put_loggedIn_methodNotAllowed() {
    login(EMAIL_NO_PROJECTS);
    expectMethodNotAllowed(putObjectJson(reportIdUri(1234, "report-1234"), emptyRequest()));
  }

  @Test
  public void delete_loggedIn_methodNotAllowed() {
    login(EMAIL_NO_PROJECTS);
    expectMethodNotAllowed(delete(reportIdUri(1234, "report-1234")));
  }

  private String reportIdUri(long projectId, String reportName) {
    return ResourceType.REPORT
        .getResourceId(Long.toString(projectId), reportName)
        .getResourceUri();
  }

  private String reportCollectionUri(String projectName) {
    return reportCollectionUri(getProject(projectName).getId());
  }

  private String reportCollectionUri(long projectId) {
    return ResourceType.REPORT
        .getResourceCollectionId(Long.toString(projectId))
        .getResourceUri();
  }

  private ReportResource buildReport(String reportName, long reportSize, long lastModified) {
    ReportResource reportResource = new ReportResource();
    reportResource.setDescription(reportName);
    reportResource.setSize(reportSize);
    reportResource.setLastModified(new Instant(lastModified));
    return reportResource;
  }

  private ReportResource addReportToBucket(
      String projectName,
      String reportSuffix,
      long reportSize,
      long lastModified,
      boolean perfReport) {

    Project project = getProject(projectName);
    checkNotNull(project.getDoubleClickReportingBucket(),
        "Reporting bucket not set for %s", project);
    String reportName = perfReport
        ? DoubleClickUtils.getPerfReportPrefix(new LocalDate(clock.now())) + reportSuffix
            + DoubleClickUtils.PERF_REPORT_SUFFIX
        : DoubleClickUtils.getSnippetReportPrefix(new LocalDate(clock.now())) + reportSuffix
            + DoubleClickUtils.SNIPPET_STATUS_REPORT_SUFFIX;
    ReportResource reportResource = buildReport(reportName, reportSize, lastModified);
    storageService.putBucketObject(
        project.getDoubleClickReportingBucket(),
        new BucketObject(reportName, reportSize, new Instant(lastModified)));
    return reportResource;
  }
}
