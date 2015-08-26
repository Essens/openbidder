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
import com.google.openbidder.ui.util.json.InstantDeserializer;
import com.google.openbidder.ui.util.json.InstantSerializer;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.Instant;

import javax.annotation.Nullable;

/**
 * Represents a Google Compute Engine VM image.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImageResource extends ExternalResource {
  private Instant createdAt;
  private boolean hasCreatedAt;

  @JsonSerialize(using = InstantSerializer.class)
  public Instant getCreatedAt() {
    return createdAt;
  }

  @JsonDeserialize(using = InstantDeserializer.class)
  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
    hasCreatedAt = true;
  }

  public void clearCreatedAt() {
    createdAt = null;
    hasCreatedAt = false;
  }

  public boolean hasCreatedAt() {
    return hasCreatedAt;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(super.hashCode(), createdAt);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof ImageResource) || !super.equals(o))  {
      return false;
    }
    ImageResource other = (ImageResource) o;
    return Objects.equal(createdAt, other.createdAt)
        && Objects.equal(hasCreatedAt, other.hasCreatedAt);
  }

  @Override
  protected MoreObjects.ToStringHelper toStringHelper() {
    return super.toStringHelper()
        .add("createdAt", createdAt);
  }

  @Override
  public int compareTo(ExternalResource other) {
    int diff = super.compareTo(other);
    if (diff == 0 && other instanceof ImageResource) {
      return ((ImageResource)other).createdAt.compareTo(createdAt);
    }
    return diff;
  }
}
