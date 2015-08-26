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

package com.google.openbidder.ui.resource.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.openbidder.ui.resource.support.ExternalResource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Represents a Google Compute Engine region resource.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegionResource extends ExternalResource {

  private List<QuotaResource> regionalQuotaResources;

  private boolean hasRegionalQuotaResources;

  public List<QuotaResource> getRegionalQuotaResources() {
    return regionalQuotaResources;
  }

  public void setRegionalQuotaResources(List<QuotaResource> regionalQuotaResources) {
    this.regionalQuotaResources = regionalQuotaResources;
    hasRegionalQuotaResources = true;
  }

  public void clearRegionalQuotaResources() {
    regionalQuotaResources = null;
    hasRegionalQuotaResources = false;
  }

  public boolean hasRegionalQuotaResources () {
    return hasRegionalQuotaResources;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        super.hashCode(),
        regionalQuotaResources
    );
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof RegionResource) || !super.equals(o)) {
      return false;
    }
    RegionResource other = (RegionResource) o;
    return Objects.equal(regionalQuotaResources, other.regionalQuotaResources)
        && Objects.equal(hasRegionalQuotaResources, other.hasRegionalQuotaResources);
  }

  @Override
  protected MoreObjects.ToStringHelper toStringHelper() {
    return super.toStringHelper()
        .add("regionalQuotaResources", regionalQuotaResources);
  }
}
