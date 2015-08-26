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

package com.google.openbidder.ui.resource.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.openbidder.ui.resource.support.ExternalResource;
import com.google.openbidder.ui.util.json.InstantDeserializer;
import com.google.openbidder.ui.util.json.InstantSerializer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.joda.time.Instant;

import javax.annotation.Nullable;

/**
 * Represents a project-specific report.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportResource extends ExternalResource {

  private Instant lastModified;
  private String reportType;
  private Long size;

  private boolean hasLastModified;
  private boolean hasReportType;
  private boolean hasSize;

  /**
   * @return Last modified timestamp of the report in GMT string format.
   */
  @JsonSerialize(using = InstantSerializer.class)
  public Instant getLastModified() {
    return lastModified;
  }

  @JsonDeserialize(using = InstantDeserializer.class)
  public void setLastModified(Instant lastModified) {
    this.lastModified = lastModified;
    hasLastModified = true;
  }

  public void clearLastModified() {
    lastModified = null;
    hasLastModified = false;
  }

  public boolean hasLastModified() {
    return hasLastModified;
  }

  /**
   * @return Type of the report.
   * See https://developers.google.com/ad-exchange/rtb/report-guide for available types.
   */
  public String getReportType() {
    return reportType;
  }

  public void setReportType(String reportType) {
    this.reportType = reportType;
    hasReportType = true;
  }

  public void clearReportType() {
    reportType = null;
    hasReportType = false;
  }

  public boolean hasReportType() {
    return hasReportType;
  }

  /**
   * @return Size of the report.
   */
  public Long getSize() {
    return size;
  }

  public void setSize(Long size) {
    this.size = size;
    hasSize = true;
  }

  public void clearSize() {
    size = null;
    hasSize = false;
  }

  public boolean hasSize() {
    return hasSize;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        super.hashCode(),
        lastModified,
        reportType,
        size
    );
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof ReportResource) || !super.equals(o)) {
      return false;
    }
    ReportResource other = (ReportResource) o;
    return Objects.equal(lastModified, other.lastModified)
        && Objects.equal(reportType, other.reportType)
        && Objects.equal(size, other.size)
        && Objects.equal(hasLastModified, other.hasLastModified)
        && Objects.equal(hasReportType, other.hasReportType)
        && Objects.equal(hasSize, other.hasSize);
  }

  @Override
  protected MoreObjects.ToStringHelper toStringHelper() {
    return super.toStringHelper()
        .add("lastModified", lastModified)
        .add("reportType", reportType)
        .add("size", size);
  }
}
