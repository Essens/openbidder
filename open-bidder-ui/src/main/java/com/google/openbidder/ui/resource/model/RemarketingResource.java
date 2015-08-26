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

import javax.annotation.Nullable;

/**
 * Remarketing within the context of a given {@link ExternalResource}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RemarketingResource extends ExternalResource {

  // TODO(danielgur): add custom validation for these fields
  private String actionId;
  private String description;
  private Boolean isEnabled;
  private Long maxCpm;
  private String clickThroughUrl;
  private String creative;

  private boolean hasActionId;
  private boolean hasDescription;
  private boolean hasIsEnabled;
  private boolean hasMaxCpm;
  private boolean hasClickThroughUrl;
  private boolean hasCreative;

  public String getActionId() {
    return actionId;
  }

  public void setActionId(String actionId) {
    this.actionId = actionId;
    hasActionId = true;
  }

  public void clearActionId() {
    actionId = null;
    hasActionId = false;
  }

  public boolean hasActionId() {
    return hasActionId;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public void setDescription(String description) {
    this.description = description;
    hasDescription = true;
  }

  @Override
  public void clearDescription() {
    description = null;
    hasDescription = false;
  }

  @Override
  public boolean hasDescription() {
    return hasDescription;
  }

  public Boolean getIsEnabled() {
    return isEnabled;
  }

  public void setIsEnabled(Boolean isEnabled) {
    this.isEnabled = isEnabled;
    hasIsEnabled = true;
  }

  public void clearIsEnabled() {
    isEnabled = null;
    hasIsEnabled = false;
  }

  public boolean hasIsEnabled() {
    return hasIsEnabled;
  }

  public Long getMaxCpm() {
    return maxCpm;
  }

  public void setMaxCpm(Long maxCpm) {
    this.maxCpm = maxCpm;
    hasMaxCpm = true;
  }

  public void clearMaxCpm() {
    maxCpm = null;
    hasMaxCpm = false;
  }

  public boolean hasMaxCpm() {
    return hasMaxCpm;
  }

  public String getClickThroughUrl() {
    return clickThroughUrl;
  }

  public void setClickThroughUrl(String clickThroughUrl) {
    this.clickThroughUrl = clickThroughUrl;
    hasClickThroughUrl = true;
  }

  public void clearClickThroughUrl() {
    clickThroughUrl = null;
    hasClickThroughUrl= false;
  }

  public boolean hasClickThroughUrl() {
    return hasClickThroughUrl;
  }

  public String getCreative() {
    return creative;
  }

  public void setCreative(String creative) {
    this.creative = creative;
    hasCreative = true;
  }

  public void clearCreative() {
    creative = null;
    hasCreative = false;
  }

  public boolean hasCreative() {
    return hasCreative;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        super.hashCode(),
        actionId,
        description,
        isEnabled,
        maxCpm,
        clickThroughUrl,
        creative
    );
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof RemarketingResource) || !super.equals(o)) {
      return false;
    }
    RemarketingResource other = (RemarketingResource) o;
    return Objects.equal(actionId, other.actionId)
        && Objects.equal(description, other.description)
        && Objects.equal(isEnabled, other.isEnabled)
        && Objects.equal(maxCpm, other.maxCpm)
        && Objects.equal(clickThroughUrl, other.clickThroughUrl)
        && Objects.equal(creative, other.creative);
  }

  @Override
  protected MoreObjects.ToStringHelper toStringHelper() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("actionId", actionId)
        .add("description", description)
        .add("isEnabled", isEnabled)
        .add("maxCpm", maxCpm)
        .add("clickThroughUrl", clickThroughUrl)
        .add("creative", creative);
  }
}