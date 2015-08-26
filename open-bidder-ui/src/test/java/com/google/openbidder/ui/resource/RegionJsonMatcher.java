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

package com.google.openbidder.ui.resource;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.api.services.compute.model.Quota;
import com.google.api.services.compute.model.Region;
import com.google.common.base.Objects;
import com.google.openbidder.ui.resource.support.ResourceId;
import com.google.openbidder.ui.resource.support.ResourceType;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Verifies JSON output matches the underlying {@link Region}.
 */
public class RegionJsonMatcher extends BaseMatcher<Region> {

  private final long projectId;
  private final Region region;

  public RegionJsonMatcher(long projectId, Region region) {
    this.projectId = projectId;
    this.region = checkNotNull(region);
  }

  @Override
  public boolean matches(Object other) {
    if(!(other instanceof Map<?, ?>)) {
      return false;
    }
    Map<?, ?> object = (Map<?, ?>) other;
    String resourceName = region.getName();
    ResourceId id = ResourceType.REGION.getResourceId(String.valueOf(projectId), resourceName);
    return Objects.equal(id.getResourceUri(), object.get("id"))
        && Objects.equal(resourceName, object.get("resourceName"))
        && Objects.equal(region.getDescription(), object.get("description"))
        && quotasMatch(object.get("regionalQuotaResources"));
  }

  @Override
  public void describeTo(Description description) {
    description.appendValue(region);
  }

  @SuppressWarnings("unchecked")
  private boolean quotasMatch(Object value) {
    List<Quota> allQuota = region.getQuotas();
    if (allQuota == null) {
      return value == null;
    }
    if (!(value instanceof List)) {
      return false;
    }

    List<Map<?, ?>> quotas = (List<Map<?, ?>>) value;
    List<Quota> remaining = new LinkedList<>(allQuota);
    for (Map<?, ?> quota : quotas) {
      Iterator<Quota> iter = remaining.iterator();
      boolean found = false;
      while (iter.hasNext()) {
        Quota quota1 = iter.next();
        if (Objects.equal(quota1.getLimit(), quota.get("limit"))
            && Objects.equal(quota1.getMetric(), quota.get("metric"))
            && Objects.equal(quota1.getUsage(), quota.get("usage"))) {
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
}
