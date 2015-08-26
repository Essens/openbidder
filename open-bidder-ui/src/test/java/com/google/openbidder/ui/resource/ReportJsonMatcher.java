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

package com.google.openbidder.ui.resource;


import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;
import com.google.openbidder.ui.resource.model.ReportResource;
import com.google.openbidder.ui.resource.support.ResourceId;
import com.google.openbidder.ui.resource.support.ResourceType;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.util.Map;

/**
 * Verifies that a {@link com.google.openbidder.ui.resource.model.ReportResource}
 * matches the expected API JSON output.
 */
public class ReportJsonMatcher extends BaseMatcher<ReportResource> {
  private final long projectId;
  private final ReportResource reportResource;

  public ReportJsonMatcher(long projectId, ReportResource reportResource) {
    this.projectId = projectId;
    this.reportResource = checkNotNull(reportResource);
  }

  @Override
  public boolean matches(Object other) {
    if (!(other instanceof Map<?, ?>)) {
      return false;
    }
    Map<?, ?> object = (Map<?, ?>) other;
    String resourceName = reportResource.getDescription();
    ResourceId id = ResourceType.REPORT.getResourceId(Long.toString(projectId), resourceName);
    return Objects.equal(id.getResourceUri(), object.get("id"))
        && Objects.equal(ResourceType.REPORT.getResourceType(), object.get("resourceType"))
        && Objects.equal(resourceName, object.get("resourceName"))
        && Objects.equal(reportResource.getLastModified().getMillis(), object.get("lastModified"))
        && Objects.equal(reportResource.getReportType(), object.get("reportType"))
        && Objects.equal(reportResource.getSize(), object.get("size"));
  }

  @Override
  public void describeTo(Description description) {
    description.appendValue(projectId).appendValue(reportResource);
  }
}
