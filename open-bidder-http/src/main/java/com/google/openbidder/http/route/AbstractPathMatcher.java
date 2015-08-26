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

package com.google.openbidder.http.route;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;

abstract class AbstractPathMatcher implements PathMatcher {
  protected final String operand;
  protected final String pathSpec;

  protected AbstractPathMatcher(String operand, String pathSpec) {
    this.operand = checkNotNull(operand);
    this.pathSpec = checkNotNull(pathSpec);
  }

  public final String getOperand() {
    return operand;
  }

  @Override
  public final String getPathSpec() {
    return pathSpec;
  }

  @Override
  public int compareTo(PathMatcher other) {
    if (other == this) {
      return 0;
    }
    if (getMatchType() != other.getMatchType()) {
      // eg EXACT = 0, PREFIX = 1. This results in -1 because ABSOLUTE first and vice versa
      return getMatchType().ordinal() - other.getMatchType().ordinal();
    }
    return -getPathSpec().compareTo(other.getPathSpec());
  }

  @Override
  public int hashCode() {
    return pathSpec.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (!(obj instanceof PathMatcher)) {
      return false;
    }
    PathMatcher other = (PathMatcher) obj;
    return pathSpec.equals(other.getPathSpec());
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("pathSpec", pathSpec)
        .add("matchType", getMatchType())
        .toString();
  }
}
