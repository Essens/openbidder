/*
 * Copyright 2014 Google Inc. All Rights Reserved.
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

package com.google.openbidder.http.cookie;

import static com.google.common.base.Objects.equal;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.openbidder.http.Cookie;

import javax.annotation.Nullable;

/**
 * Base implementation of {@link Cookie}.
 */
public abstract class AbstractCookie implements Cookie {

  @Override
  public Cookie.Builder toBuilder() {
    return createBuilder()
        .setName(getName())
        .setValue(getValue())
        .setDomain(getDomain())
        .setPath(getPath())
        .setSecure(isSecure())
        .setComment(getComment())
        .setMaxAge(getMaxAge())
        .setVersion(getVersion());
  }

  protected Cookie.Builder createBuilder() {
    return StandardCookie.newBuilder();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getName(), getValue());
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (obj == this) {
      return true;
    } else if (!(obj instanceof Cookie)) {
      return false;
    }

    Cookie other = (Cookie) obj;
    return equal(getName(), other.getName())
        && equal(getValue(), other.getValue())
        && equal(getDomain(), other.getDomain())
        && equal(getPath(), other.getPath())
        && equal(isSecure(), other.isSecure())
        && equal(getComment(), other.getComment())
        && equal(getMaxAge(), other.getMaxAge())
        && equal(getVersion(), other.getVersion());
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("name", getName())
        .add("value", getValue())
        .add("domain", getDomain())
        .add("path", getPath())
        .add("secure", isSecure())
        .add("comment", getComment())
        .add("maxAge", getMaxAge())
        .add("version", getVersion())
        .toString();
  }
}
