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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.annotation.Nullable;

/**
 * Represents a project Compute Engine quota.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuotaResource extends ExternalResource {

  private String metric;
  private Double usage;
  private Double limit;

  private boolean hasMetric;
  private boolean hasUsage;
  private boolean hasLimit;

  public String getMetric() {
    return metric;
  }

  public void setMetric(String metric) {
    this.metric = metric;
    hasMetric = true;
  }

  public void clearMetric() {
    metric = null;
    hasMetric = false;
  }

  public boolean hasMetric() {
    return hasMetric;
  }

  public Double getUsage() {
    return usage;
  }

  public void setUsage(Double usage) {
    this.usage = usage;
    hasUsage = true;
  }

  public void clearUsage() {
    usage = null;
    hasUsage = false;
  }

  public boolean hasUsage() {
    return hasUsage;
  }

  public Double getLimit() {
    return limit;
  }

  public void setLimit(Double limit) {
    this.limit = limit;
    hasLimit = true;
  }

  public void clearLimit() {
    limit = null;
    hasLimit = false;
  }

  public boolean hasLimit() {
    return hasLimit;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        super.hashCode(),
        metric,
        usage,
        limit
    );
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof QuotaResource) || !super.equals(o)) {
      return false;
    }
    QuotaResource other = (QuotaResource) o;
    return Objects.equal(metric, other.metric)
        && Objects.equal(usage, other.usage)
        && Objects.equal(limit, other.limit)
        && Objects.equal(hasMetric, other.hasMetric)
        && Objects.equal(hasUsage, other.hasUsage)
        && Objects.equal(hasLimit, other.hasLimit);
  }

  @Override
  protected MoreObjects.ToStringHelper toStringHelper() {
    return super.toStringHelper()
        .add("metric", metric)
        .add("usage", usage)
        .add("limit", limit);
  }
}
