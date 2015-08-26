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

import com.google.api.services.compute.model.Zone;
import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.openbidder.ui.util.json.InstantSerializer;
import com.google.openbidder.ui.util.web.WebUtils;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.Instant;

import javax.annotation.Nullable;

/**
 * Represents a scheduled outage in Google Compute Engine.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScheduledOutage {

  public static final Function<Zone.MaintenanceWindows, ScheduledOutage> FROM_MAINTENANCE_WINDOW =
      new Function<Zone.MaintenanceWindows, ScheduledOutage>() {
        @Override
        public ScheduledOutage apply(Zone.MaintenanceWindows maintenanceWindow) {
          return build(maintenanceWindow);
        }
      };

  private String name;
  private String description;
  private Instant beginTime;
  private Instant endTime;

  private boolean hasName;
  private boolean hasDescription;
  private boolean hasBeginTime;
  private boolean hasEndTime;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
    hasName = true;
  }

  public void clearName() {
    name = null;
    hasName = false;
  }

  public boolean hasName() {
    return hasName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
    hasDescription = true;
  }

  public void clearDescription() {
    description = null;
    hasDescription = false;
  }

  public boolean hasDescription() {
    return hasDescription;
  }

  @JsonSerialize(using = InstantSerializer.class)
  public Instant getBeginTime() {
    return beginTime;
  }

  public void setBeginTime(Instant beginTime) {
    this.beginTime = beginTime;
    hasBeginTime = true;
  }

  public void clearBeginTime() {
    beginTime = null;
    hasBeginTime = false;
  }

  public boolean hasBeginTime() {
    return hasBeginTime;
  }

  @JsonSerialize(using = InstantSerializer.class)
  public Instant getEndTime() {
    return endTime;
  }

  public void setEndTime(Instant endTime) {
    this.endTime = endTime;
    hasEndTime = true;
  }

  public void clearEndTime() {
    endTime = null;
    hasEndTime = false;
  }

  public boolean hasEndTime() {
    return hasEndTime;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        name,
        description,
        beginTime,
        endTime
    );
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof ScheduledOutage)) {
      return false;
    }
    ScheduledOutage other = (ScheduledOutage) o;
    return Objects.equal(name, other.name)
        && Objects.equal(description, other.description)
        && Objects.equal(beginTime, other.beginTime)
        && Objects.equal(endTime, other.endTime)
        && Objects.equal(hasName, other.hasName)
        && Objects.equal(hasDescription, other.hasDescription)
        && Objects.equal(hasBeginTime, other.hasBeginTime)
        && Objects.equal(hasEndTime, other.hasEndTime);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("name", name)
        .add("description", description)
        .add("beginTime", beginTime)
        .add("endTime", endTime)
        .toString();
  }

  public static ScheduledOutage build(Zone.MaintenanceWindows maintenanceWindows) {
    ScheduledOutage scheduledOutage = new ScheduledOutage();
    scheduledOutage.setName(maintenanceWindows.getName());
    scheduledOutage.setDescription(maintenanceWindows.getDescription());
    scheduledOutage.setBeginTime(WebUtils.parse8601(maintenanceWindows.getBeginTime()));
    scheduledOutage.setEndTime(WebUtils.parse8601(maintenanceWindows.getEndTime()));
    return scheduledOutage;
  }
}
