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

import com.google.api.services.compute.model.Instance;
import com.google.api.services.compute.model.Zone;
import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.openbidder.ui.compute.BidderInstanceBuilder;
import com.google.openbidder.ui.compute.ComputeUtils;
import com.google.openbidder.ui.compute.InstanceBuilder;
import com.google.openbidder.ui.compute.LoadBalancerInstanceBuilder;
import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.resource.support.InstanceType;
import com.google.openbidder.ui.resource.support.ResourceId;
import com.google.openbidder.ui.resource.support.ResourceType;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.joda.time.Instant;

import java.util.Map;

/**
 * Verifies JSON output matches the underlying {@link Instance}.
 */
public class InstanceJsonMatcher extends BaseMatcher<Instance> {

  private final long projectId;
  private final String zoneName;
  private final Instance instance;

  public InstanceJsonMatcher(Project project, Zone zone, Instance instance) {
    projectId = project.getId();
    zoneName = zone.getName();
    this.instance = Preconditions.checkNotNull(instance);
  }

  @Override
  public boolean matches(Object other) {
    if (!(other instanceof JSONObject)) {
      return false;
    }
    JSONObject object = (JSONObject) other;
    String resourceName = instance.getName();
    ResourceId id = ResourceType.INSTANCE.getResourceId(
        Long.toString(projectId), zoneName, resourceName);
    ImmutableMap<String, String> metadata = ComputeUtils.toMap(instance.getMetadata());
    InstanceType instanceType = InstanceType.fromInstanceType((String) object.get("instanceType"));
    return Objects.equal(id.getResourceUri(), object.get("id"))
        && Objects.equal(ResourceType.INSTANCE.getResourceType(), object.get("resourceType"))
        && Objects.equal(resourceName, object.get("resourceName"))
        && Objects.equal(InstanceType.fromInstance(instance), instanceType)
        && Objects.equal(instance.getStatus(), object.get("status"))
        && Objects.equal(getNetworkUri(instance), object.get("network"))
        && Objects.equal(getZoneUri(instance), object.get("zone"))
        && Objects.equal(getMachineTypeUri(instance, zoneName), object.get("machineType"))
        && Objects.equal(ComputeUtils.getInternalIp(instance), object.get("internalIp"))
        && Objects.equal(ComputeUtils.getExternalIp(instance), object.get("externalIp"))
        && Objects.equal(parseDate(instance.getCreationTimestamp()), object.get("createdAt"))
        && Objects.equal(metadata.get(InstanceBuilder.METADATA_USER_DIST), object.get("userDistUri"))
        && (instanceType != InstanceType.BIDDER || matchBidderParameters(object, metadata))
        && (instanceType != InstanceType.BALANCER || matchBalancerParameters(object, metadata));
  }

  @Override
  public void describeTo(Description description) {
    description.appendValue(instance);
  }

  private boolean matchBidderParameters(JSONObject object, Map<String, String> metadata) {
    return Objects.equal(metadata.get(BidderInstanceBuilder.METADATA_LISTEN_PORT),
            object.get("requestPort"))
        && Objects.equal(metadata.get(BidderInstanceBuilder.METADATA_ADMIN_PORT),
            object.get("adminPort"))
        && Objects.equal(BidderInstanceBuilder.getLoadBalancerUri(metadata),
            object.get("callbackUrl"))
        && Objects.equal(ComputeUtils.getParametersFromMetadata(
            instance, BidderInstanceBuilder.METADATA_JVM_PARAMETERS), object.get("jvmParameters"))
        && Objects.equal(MoreObjects.firstNonNull(
            metadata.get(BidderInstanceBuilder.METADATA_BID_INTERCEPTORS), ""),
            Joiner.on(',').join((JSONArray) object.get("interceptors")))
        && Objects.equal(MoreObjects.firstNonNull(
            metadata.get(BidderInstanceBuilder.METADATA_IMPRESSION_INTERCEPTORS), ""),
            Joiner.on(',').join((JSONArray) object.get("impressionInterceptors")))
        && Objects.equal(MoreObjects.firstNonNull(
            metadata.get(BidderInstanceBuilder.METADATA_CLICK_INTERCEPTORS), ""),
            Joiner.on(',').join((JSONArray) object.get("clickInterceptors")));
  }

  private boolean matchBalancerParameters(JSONObject object, Map<String, String> metadata) {
    return Objects.equal(metadata.get(LoadBalancerInstanceBuilder.METADATA_BIDDER_REQUEST_PORT),
            object.get("bidderRequestPort"))
        && Objects.equal(metadata.get(LoadBalancerInstanceBuilder.METADATA_REQUEST_PORT),
            object.get("requestPort"))
        && Objects.equal(metadata.get(LoadBalancerInstanceBuilder.METADATA_HAPROXY_STATS_PORT),
            object.get("haProxyStatPort"));
  }

  private String getNetworkUri(Instance instance) {
    return getResourceUri(ResourceType.NETWORK, ComputeUtils.getNetwork(instance));
  }

  private String getZoneUri(Instance instance) {
    return getResourceUri(ResourceType.ZONE, instance.getZone());
  }

  private String getMachineTypeUri(Instance instance, String zoneName) {
    return getResourceUri(ResourceType.MACHINE_TYPE, zoneName, instance.getMachineType());
  }

  private String getResourceUri(ResourceType resourceType, String computeResourceUrl) {
    return ComputeUtils.toResourceUri(projectId, resourceType, computeResourceUrl);
  }

  private String getResourceUri(
      ResourceType resourceType,
      String zoneName,
      String computeResourceUrl) {
    return ComputeUtils.toResourceUri(
        projectId, zoneName, resourceType, computeResourceUrl);
  }

  private static Long parseDate(String text) {
    return text == null ? null : Instant.parse(text).getMillis();
  }
}
