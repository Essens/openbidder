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

import com.google.api.services.compute.model.Quota;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.openbidder.ui.resource.support.ResourceId;
import com.google.openbidder.ui.resource.support.ResourceType;

import net.minidev.json.JSONObject;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * Verifies JSON output matches the underlying {@link Quota}.
 */
public class QuotaJsonMatcher extends BaseMatcher<Quota> {

  private final long projectId;
  private final Quota quota;

  public QuotaJsonMatcher(long projectId, Quota quota) {
    this.projectId = projectId;
    this.quota = Preconditions.checkNotNull(quota);
  }

  @Override
  public boolean matches(Object other) {
    if (!(other instanceof JSONObject)) {
      return false;
    }
    JSONObject object = (JSONObject) other;
    String resourceName = quota.getMetric().toLowerCase();
    ResourceId id = ResourceType.QUOTA.getResourceId(
        Long.toString(projectId), resourceName);
    return Objects.equal(id.getResourceUri(), object.get("id"))
        && Objects.equal(ResourceType.QUOTA.getResourceType(), object.get("resourceType"))
        && Objects.equal(resourceName, object.get("resourceName"))
        && Objects.equal(quota.getMetric(), object.get("metric"))
        && Objects.equal(quota.getUsage(), object.get("usage"))
        && Objects.equal(quota.getLimit(), object.get("limit"));
  }

  @Override
  public void describeTo(Description description) {
    description.appendValue(quota);
  }
}
