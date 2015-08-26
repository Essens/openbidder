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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.resource.support.ResourceId;
import com.google.openbidder.ui.resource.support.ResourceType;

import net.minidev.json.JSONObject;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * Verifies that a {@link Project} matches the expected API JSON output.
 */
public class ProjectJsonMatcher extends BaseMatcher<Project> {

  private final Project project;
  private final boolean networkExists;

  public ProjectJsonMatcher(Project project) {
    this(project, false);
  }

  public ProjectJsonMatcher(Project project, boolean networkExists) {
    this.project = Preconditions.checkNotNull(project);
    this.networkExists = networkExists;
  }

  @Override
  public boolean matches(Object item) {
    if (!(item instanceof JSONObject)) {
      return false;
    }
    JSONObject object = (JSONObject) item;
    String resourceName = Long.toString(project.getId());
    ResourceId id = ResourceType.PROJECT.getResourceId(resourceName);

    // a check is made to Compute to see if the project's network exists. If it does not then
    // the JSON response will have this field as null
    String thisNetworkName = networkExists ? project.getNetworkName() : null;

    String otherNetworkUrl = (String) object.get("network");
    String otherNetworkName = otherNetworkUrl == null
        ? null
        : Iterables.getLast(Splitter.on('/').split(otherNetworkUrl));
    return Objects.equal(id.getResourceUri(), object.get("id"))
        && Objects.equal(ResourceType.PROJECT.getResourceType(), object.get("resourceType"))
        && Objects.equal(resourceName, object.get("resourceName"))
        && Objects.equal(project.getProjectName(), object.get("description"))
        && Objects.equal(project.getOauth2ClientId(), object.get("oauth2ClientId"))
        && Objects.equal(project.getOauth2ClientSecret(), object.get("oauth2ClientSecret"))
        && Objects.equal(project.getApiProjectId(), object.get("apiProjectId"))
        && Objects.equal(project.getApiProjectNumber().longValue(),
            ((Number) object.get("apiProjectNumber")).longValue())
        && Objects.equal(project.getAdExchangeBuyerAccountId(),
            object.get("adExchangeBuyerAccount"))
        && Objects.equal(project.getBidderImage(), object.get("bidderImage"))
        && Objects.equal(project.getLoadBalancerImage(), object.get("loadBalancerImage"))
        && Objects.equal(project.getVmParameters(), object.get("vmParameters"))
        && Objects.equal(project.getMainParameters(), object.get("mainParameters"))
        && Objects.equal(project.getBidInterceptors(), object.get("bidInterceptors"))
        && Objects.equal(project.getImpressionInterceptors(), object.get("impressionInterceptors"))
        && Objects.equal(project.getClickInterceptors(), object.get("clickInterceptors"))
        && Objects.equal(project.getMatchInterceptors(), object.get("matchInterceptors"))
        && Objects.equal(project.getEncryptionKey(), object.get("encryptionKey"))
        && Objects.equal(project.getIntegrityKey(), object.get("integrityKey"))
        && Objects.equal(project.getUserDistUri(), object.get("userDistUri"))
        && Objects.equal(project.getWhiteListedIpRanges(), object.get("whiteListedIpRanges"))
        && Objects.equal(thisNetworkName, otherNetworkName)
        && Objects.equal(project.getProjectUuid(), object.get("projectUuid"))
        && Objects.equal(project.getBidderOauth2Scopes(), object.get("bidderOauth2Scopes"))
        && Objects.equal(project.getLoadBalancerOauth2Scopes(),
            object.get("loadBalancerOauth2Scopes"));
  }

  @Override
  public void describeTo(Description description) {
    description.appendValue(project);
  }
}
