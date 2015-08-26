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

package com.google.openbidder.ui.resource;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.api.services.compute.model.Zone;
import com.google.common.base.Objects;
import com.google.openbidder.ui.resource.model.ZoneResource;
import com.google.openbidder.ui.resource.support.ResourceId;
import com.google.openbidder.ui.resource.support.ResourceType;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.joda.time.Instant;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Verifies JSON output matches the underlying {@link Zone}.
 */
public class ZoneJsonMatcher extends BaseMatcher<ZoneResource> {

  private final long projectId;
  private final Zone zone;
  private final String zoneHost;

  public ZoneJsonMatcher(long projectId,  Zone zone) {
    this(projectId, zone, /* zone host */ null);
  }

  public ZoneJsonMatcher(long projectId,  Zone zone, String zoneHost) {
    this.projectId = projectId;
    this.zone = checkNotNull(zone);
    this.zoneHost = zoneHost;
  }

  @Override
  public boolean matches(Object other) {
    if (!(other instanceof Map<?, ?>)) {
      return false;
    }
    Map<?, ?> object = (Map<?, ?>) other;
    String resourceName = zone.getName();
    ResourceId id = ResourceType.ZONE.getResourceId(
        Long.toString(projectId), resourceName);
    return Objects.equal(id.getResourceUri(), object.get("id"))
        && Objects.equal(ResourceType.ZONE.getResourceType(), object.get("resourceType"))
        && Objects.equal(resourceName, object.get("resourceName"))
        && Objects.equal(resourceName, object.get("description"))
        && matchesMaintenanceWindow(object.get("scheduledOutages"))
        && Objects.equal(zoneHost, object.get("hostName"));
  }

  @Override
  public void describeTo(Description description) {
    description.appendValue(zone);
  }

  @SuppressWarnings("unchecked")
  private boolean matchesMaintenanceWindow(Object value) {
    List<Zone.MaintenanceWindows> maintenanceWindows = zone.getMaintenanceWindows();
    if (maintenanceWindows == null) {
      return value == null;
    }
    if (!(value instanceof List)) {
      return false;
    }
    List<Map<?, ?>> outages = (List<Map<?, ?>>) value;
    List<Zone.MaintenanceWindows> remaining = new LinkedList<>(maintenanceWindows);
    for (Map<?, ?> outage : outages) {
      Iterator<Zone.MaintenanceWindows> iter = remaining.iterator();
      boolean found = false;
      while (iter.hasNext()) {
        Zone.MaintenanceWindows window = iter.next();
        if (Objects.equal(timeAsLong(window.getBeginTime()), outage.get("beginTime"))
            && Objects.equal(timeAsLong(window.getEndTime()), outage.get("endTime"))
            && Objects.equal(window.getName(), outage.get("name"))
            && Objects.equal(window.getDescription(), outage.get("description"))) {

          iter.remove();
          found = true;
          break;
        }
      }
      if (!found) {
        return false;
      }
    }
    return remaining.isEmpty();
  }

  private long timeAsLong(String time) {
    return Instant.parse(time).getMillis();
  }
}
