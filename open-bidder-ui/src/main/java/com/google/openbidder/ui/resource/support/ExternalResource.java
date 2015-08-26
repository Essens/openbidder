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

package com.google.openbidder.ui.resource.support;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Objects;
import com.google.openbidder.ui.util.json.ResourceIdDeserializer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import javax.annotation.Nullable;
import javax.validation.constraints.Size;

/**
 * Common metadata for all resources returned by the API.
 */
public class ExternalResource implements Comparable<ExternalResource> {
  private ResourceId id;

  @Size(min = 5, max = 200)
  private String description;

  private boolean hasId;
  private boolean hasDescription;

  public ResourceId getId() {
    return id;
  }

  @JsonDeserialize(using = ResourceIdDeserializer.class)
  public void setId(ResourceId id) {
    this.id = id;
    hasId = true;
  }

  public void clearId() {
    this.id = null;
    hasId = false;
  }

  public boolean hasId() {
    return hasId;
  }

  public ResourceType getResourceType() {
    return id == null ? null : id.getResourceType();
  }

  public String getResourceName() {
    return id == null ? null : id.getResourceName();
  }

  @JsonIgnore
  public String getResourceUri() {
    return id == null ? null : id.getResourceUri();
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
    hasDescription = true;
  }

  public void clearDescription() {
    this.description = null;
    hasDescription = false;
  }

  public boolean hasDescription() {
    return hasDescription;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        id,
        description
    );
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof ExternalResource)) {
      return false;
    }
    ExternalResource other = (ExternalResource) o;
    return Objects.equal(id, other.id)
        && Objects.equal(description, other.description)
        && Objects.equal(hasId, other.hasId)
        && Objects.equal(hasDescription, other.hasDescription);
  }

  @Override
  public final String toString() {
    return toStringHelper().toString();
  }

  protected ToStringHelper toStringHelper() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("id", id)
        .add("description", description);
  }

  @Override
  public int compareTo(ExternalResource other) {
    return id.compareTo(other.id);
  }
}
