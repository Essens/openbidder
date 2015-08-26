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

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.openbidder.ui.adexchangebuyer.DoubleClickUtils;
import com.google.openbidder.ui.cloudstorage.BucketContents;
import com.google.openbidder.ui.cloudstorage.BucketObject;
import com.google.openbidder.ui.cloudstorage.StorageService;
import com.google.openbidder.ui.project.ProjectService;
import com.google.openbidder.ui.project.ProjectUser;
import com.google.openbidder.ui.resource.ReportResourceService;
import com.google.openbidder.ui.resource.model.ReportResource;
import com.google.openbidder.ui.resource.support.AbstractProjectResourceService;
import com.google.openbidder.ui.resource.support.ResourceCollectionId;
import com.google.openbidder.ui.resource.support.ResourceMethod;
import com.google.openbidder.ui.resource.support.ResourceType;
import com.google.openbidder.util.Clock;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.inject.Inject;

/**
 * {@link com.google.openbidder.ui.resource.ResourceService}
 * for {@link com.google.openbidder.ui.resource.model.ReportResource}s.
 */
public class ReportResourceServiceImpl
    extends AbstractProjectResourceService<ReportResource, StorageService>
    implements ReportResourceService {

  public static final String REPORT_DATE = "reportDate";

  private static final String INPUT_DATE_FORMAT = "MM/dd/yyyy";
  private static final String PERF_REPORT_TYPE = "perfReport";
  private static final String SNIPPET_REPORT_TYPE =  "snippetReport";

  // Filters performance reports for those that list data points in increasing order.
  private static final Predicate<BucketObject> PERF_REPORT_ORDER_FILTER =
      new Predicate<BucketObject>() {
        @Override
        public boolean apply(BucketObject reportObject) {
          return DoubleClickUtils.isPerfReport(reportObject.getName());
        }
      };

  // Filters snippet status reports for only those in text format.
  private static final Predicate<BucketObject> SNIPPET_STATUS_TEXT_FILTER =
      new Predicate<BucketObject>() {
        @Override
        public boolean apply(BucketObject reportObject) {
          return DoubleClickUtils.isSnippetStatusReport(reportObject.getName());
        }
      };

  private final StorageService storageService;
  private final Clock clock;

  @Inject
  protected ReportResourceServiceImpl(
      ProjectService projectService,
      StorageService storageService,
      Clock clock) {

    super(ResourceType.REPORT,
        EnumSet.of(ResourceMethod.LIST),
        projectService);
    this.storageService = Preconditions.checkNotNull(storageService);
    this.clock = Preconditions.checkNotNull(clock);
  }

  @Override
  protected final StorageService getService(ProjectUser projectUser) {
    return storageService;
  }

  @Override
  protected List<? extends ReportResource> list(
      StorageService service,
      ProjectUser projectUser,
      ResourceCollectionId resourceCollectionId,
      Multimap<String, String> params) {

    LocalDate localDate = new LocalDate(clock.now());
    String dateStr = Iterables.getFirst(params.get(REPORT_DATE), null);
    if (!Strings.isNullOrEmpty(dateStr)) {
      DateTimeFormatter formatter = DateTimeFormat.forPattern(INPUT_DATE_FORMAT);
      localDate = LocalDate.parse(dateStr, formatter);
    }

    BucketContents perfContents = storageService.listAllObjectsInBucket(
        projectUser,
        projectUser.getProject().getDoubleClickReportingBucket(),
        DoubleClickUtils.getPerfReportPrefix(localDate));

    BucketContents snippetContents = storageService.listAllObjectsInBucket(
        projectUser,
        projectUser.getProject().getDoubleClickReportingBucket(),
        DoubleClickUtils.getSnippetReportPrefix(localDate));

    return build(perfContents, snippetContents);
  }

  private List<ReportResource> build(
      BucketContents perfContents,
      BucketContents snippetContents) {

    BucketContents filteredPerfContents = filterBucketContents(
        perfContents, PERF_REPORT_ORDER_FILTER);
    BucketContents filteredSnippetContents = filterBucketContents(
        snippetContents, SNIPPET_STATUS_TEXT_FILTER);

    List<ReportResource> reportResources = new ArrayList<>();
    reportResources = addReports(
        reportResources, filteredPerfContents.getBucketObjects(), PERF_REPORT_TYPE);
    reportResources = addReports(
        reportResources, filteredSnippetContents.getBucketObjects(), SNIPPET_REPORT_TYPE);
    return reportResources;
  }

  private List<ReportResource> addReports(List<ReportResource> reportResources,
      List<BucketObject> reportObjects, String reportType) {
    for (BucketObject reportObject : reportObjects) {
      ReportResource reportResource = new ReportResource();
      reportResource.setDescription(reportObject.getName());
      reportResource.setLastModified(reportObject.getLastModified());
      reportResource.setSize(reportObject.getSize());
      reportResource.setReportType(reportType);
      reportResources.add(reportResource);
    }
    return reportResources;
  }

  private BucketContents filterBucketContents(
      BucketContents contents,
      Predicate<BucketObject> filter) {
    return new BucketContents(
        contents.getBucketName(), Iterables.filter(contents.getBucketObjects(), filter));
  }
}
