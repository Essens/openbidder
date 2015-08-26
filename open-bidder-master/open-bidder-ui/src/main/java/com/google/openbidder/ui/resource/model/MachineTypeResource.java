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

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.annotation.Nullable;

/**
 * Represents a project-specific machine type.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MachineTypeResource extends ExternalResource {

  private Integer guestCpus;
  private Integer imageSpaceGb;
  private Integer memoryMb;

  private boolean hasGuestCpus;
  private boolean hasImageSpaceGb;
  private boolean hasMemoryMb;

  public Integer getGuestCpus() {
    return guestCpus;
  }

  public void setGuestCpus(Integer guestCpus) {
    this.guestCpus = guestCpus;
    hasGuestCpus = true;
  }

  public void clearGuestCpus() {
    guestCpus = null;
    hasGuestCpus = false;
  }

  public boolean hasGuestCpus() {
    return hasGuestCpus;
  }

  public Integer getImageSpaceGb() {
    return imageSpaceGb;
  }

  public void setImageSpaceGb(Integer imageSpaceGb) {
    this.imageSpaceGb = imageSpaceGb;
    hasImageSpaceGb = true;
  }

  public void clearImageSpaceGb() {
    imageSpaceGb = null;
    hasImageSpaceGb = false;
  }

  public boolean hasImageSpaceGb() {
    return hasImageSpaceGb;
  }

  public Integer getMemoryMb() {
    return memoryMb;
  }

  public void setMemoryMb(Integer memoryMb) {
    this.memoryMb = memoryMb;
    hasMemoryMb = true;
  }

  public void clearMemoryMb() {
    this.memoryMb = null;
    hasMemoryMb = false;
  }

  public boolean hasMemoryMb() {
    return hasMemoryMb;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        super.hashCode(),
        guestCpus,
        imageSpaceGb,
        memoryMb
    );
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof MachineTypeResource) || !super.equals(o)) {
      return false;
    }
    MachineTypeResource other = (MachineTypeResource) o;
    return Objects.equal(guestCpus, other.guestCpus)
        && Objects.equal(imageSpaceGb, other.imageSpaceGb)
        && Objects.equal(memoryMb, other.memoryMb)
        && Objects.equal(hasGuestCpus, other.hasGuestCpus)
        && Objects.equal(hasImageSpaceGb, other.hasImageSpaceGb)
        && Objects.equal(hasMemoryMb, other.hasMemoryMb);
  }

  @Override
  protected MoreObjects.ToStringHelper toStringHelper() {
    return super.toStringHelper()
        .add("guestCpus", guestCpus)
        .add("imageSpaceGb", imageSpaceGb)
        .add("memoryMb", memoryMb);
  }
}
