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
 * Represents a project-specific buyer account.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountResource extends ExternalResource {

  private String cookieMatchNid;
  private String cookieMatchUrl;
  private Integer maximumTotalQps;

  private boolean hasCookieMatchNid;
  private boolean hasCookieMatchUrl;
  private boolean hasMaximumTotalQps;

  public String getCookieMatchNid() {
    return cookieMatchNid;
  }

  public void setCookieMatchNid(String cookieMatchNid) {
    this.cookieMatchNid = cookieMatchNid;
    hasCookieMatchNid = true;
  }

  public void clearCookieMatchNid() {
    cookieMatchNid = null;
    hasCookieMatchNid = false;
  }

  public boolean hasCookieMatchNid() {
    return hasCookieMatchNid;
  }

  public String getCookieMatchUrl() {
    return cookieMatchUrl;
  }

  public void setCookieMatchUrl(String cookieMatchUrl) {
    this.cookieMatchUrl = cookieMatchUrl;
    hasCookieMatchUrl = true;
  }

  public void clearCookieMatchUrl() {
    cookieMatchUrl = null;
    hasCookieMatchUrl = false;
  }

  public boolean hasCookieMatchUrl() {
    return hasCookieMatchUrl;
  }

  public Integer getMaximumTotalQps() {
    return maximumTotalQps;
  }

  public void setMaximumTotalQps(Integer maximumTotalQps) {
    this.maximumTotalQps = maximumTotalQps;
    hasMaximumTotalQps = true;
  }

  public void clearMaximumTotalQps() {
    maximumTotalQps = null;
    hasMaximumTotalQps = false;
  }

  public boolean hasMaximumTotalQps() {
    return hasMaximumTotalQps;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        super.hashCode(),
        cookieMatchNid,
        cookieMatchUrl,
        maximumTotalQps
    );
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof AccountResource) || !super.equals(o)) {
      return false;
    }
    AccountResource other = (AccountResource) o;
    return Objects.equal(cookieMatchNid, other.cookieMatchNid)
        && Objects.equal(cookieMatchUrl, other.cookieMatchUrl)
        && Objects.equal(maximumTotalQps, other.maximumTotalQps)
        && Objects.equal(hasCookieMatchNid, other.hasCookieMatchNid)
        && Objects.equal(hasCookieMatchUrl, other.hasCookieMatchUrl)
        && Objects.equal(hasMaximumTotalQps, other.hasMaximumTotalQps);
  }

  @Override
  protected MoreObjects.ToStringHelper toStringHelper() {
    return super.toStringHelper()
        .add("cookieMatchNid", cookieMatchNid)
        .add("cookieMatchUrl", cookieMatchUrl)
        .add("maximumTotalQps", maximumTotalQps);
  }
}
