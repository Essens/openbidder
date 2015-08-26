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
import static com.google.common.base.Preconditions.checkState;

import com.google.api.services.compute.model.Firewall;
import com.google.common.base.Objects;
import com.google.openbidder.ui.compute.ResourceName;
import com.google.openbidder.ui.entity.Project;
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
 * Verifies JSON output matches the underlying {@link Firewall}.
 */
public class FirewallJsonMatcher extends BaseMatcher<Firewall> {

  private final long projectId;
  private final String apiProjectId;
  private final Firewall firewall;
  private final ResourceId projectResourceId;

  public FirewallJsonMatcher(Project project, Firewall firewall) {
    projectId = project.getId();
    apiProjectId = project.getApiProjectId();
    this.firewall = checkNotNull(firewall);
    projectResourceId = ResourceType.PROJECT.getResourceId(Long.toString(projectId));
  }

  @Override
  public boolean matches(Object other) {
    if (!(other instanceof Map<?, ?>)) {
      return false;
    }
    Map<?, ?> object = (Map<?, ?>) other;
    String resourceName = firewall.getName();
    ResourceId id = ResourceType.FIREWALL.getResourceId(
        Long.toString(projectId), resourceName);
    ResourceName networkName = ResourceName.parseResource(firewall.getNetwork());
    checkState(apiProjectId.equals(networkName.getApiProjectId()),
        "Expected API project ID of %s in, found %s in network %s",
        apiProjectId,
        networkName.getApiProjectId(),
        networkName);
    String network = projectResourceId.getChildCollection(ResourceType.NETWORK)
        .getResourceId(networkName.getResourceName())
        .getResourceUri();
    return Objects.equal(id.getResourceUri(), object.get("id"))
        && Objects.equal(ResourceType.FIREWALL.getResourceType(), object.get("resourceType"))
        && Objects.equal(resourceName, object.get("resourceName"))
        && Objects.equal(firewall.getDescription(), object.get("description"))
        && Objects.equal(parseDate(firewall.getCreationTimestamp()), object.get("createdAt"))
        && Objects.equal(network, object.get("network"))
        && Objects.equal(firewall.getSourceRanges(), object.get("sourceRanges"))
        && Objects.equal(firewall.getSourceTags(), object.get("sourceTags"))
        && Objects.equal(firewall.getTargetTags(), object.get("targetTags"))
        && matchesAllowed(object.get("allowed"));
  }

  @Override
  public void describeTo(Description description) {
    description.appendValue(firewall);
  }

  @SuppressWarnings("unchecked")
  private boolean matchesAllowed(Object value) {
    List<Firewall.Allowed> allAllowed = firewall.getAllowed();
    if (allAllowed == null) {
      return value == null;
    }
    if (!(value instanceof List)) {
      return false;
    }
    List<Map<?, ?>> outages = (List<Map<?, ?>>) value;
    List<Firewall.Allowed> remaining = new LinkedList<>(allAllowed);
    for (Map<?, ?> outage : outages) {
      Iterator<Firewall.Allowed> iter = remaining.iterator();
      boolean found = false;
      while (iter.hasNext()) {
        Firewall.Allowed allowed = iter.next();
        if (Objects.equal(allowed.getIPProtocol(), outage.get("protocol"))
            && Objects.equal(allowed.getPorts(), outage.get("ports"))) {

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

  private static long parseDate(String text) {
    return Instant.parse(text).getMillis();
  }
}
