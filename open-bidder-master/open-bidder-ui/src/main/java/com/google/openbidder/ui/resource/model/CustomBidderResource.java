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

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.annotation.Nullable;

/**
 * Represents a set of resources for a customized bidder.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomBidderResource {

  private String mainParameters;

  private boolean hasMainParameters;

  public String getMainParameters() {
    return mainParameters;
  }

  public void setMainParameters(String mainParameters) {
    this.mainParameters = mainParameters;
    hasMainParameters = true;
  }

  public void clearMainParameters() {
    mainParameters = null;
    hasMainParameters = false;
  }

  public boolean hasMainParameters() {
    return hasMainParameters;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(mainParameters);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CustomBidderResource other = (CustomBidderResource) o;
    return Objects.equal(mainParameters, other.mainParameters)
        && Objects.equal(hasMainParameters, other.hasMainParameters);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("mainParameters", mainParameters)
        .toString();
  }
}