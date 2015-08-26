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

import com.google.api.services.compute.model.Instance;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.openbidder.ui.compute.ComputeUtils;
import com.google.openbidder.ui.compute.InstanceBuilder;
import com.google.openbidder.ui.compute.LoadBalancerInstanceBuilder;
import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.resource.support.InstanceType;
import com.google.openbidder.ui.resource.support.ResourceCollectionId;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.annotation.Nullable;

/**
 * Represents a project-specific load balancer.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BalancerResource extends InstanceResource {

  private String haProxyStatPort;
  private String requestPort;
  private String bidderRequestPort;

  private boolean hasHaProxyStatPort;
  private boolean hasRequestPort;
  private boolean hasBidderRequestPort;

  public String getHaProxyStatPort() {
    return haProxyStatPort;
  }

  public void setHaProxyStatPort(String haProxyStatPort) {
    this.haProxyStatPort = haProxyStatPort;
    hasHaProxyStatPort = true;
  }

  public void clearHaProxyStatPort() {
    haProxyStatPort = null;
    hasHaProxyStatPort = false;
  }

  public boolean hasHaProxyStatPort() {
    return hasHaProxyStatPort;
  }

  public String getRequestPort() {
    return requestPort;
  }

  public void setRequestPort(String requestPort) {
    this.requestPort = requestPort;
    hasRequestPort = true;
  }

  public void clearRequestPort() {
    requestPort = null;
    hasRequestPort = false;
  }

  public boolean hasRequestPort() {
    return hasRequestPort;
  }

  public String getBidderRequestPort() {
    return bidderRequestPort;
  }

  public void setBidderRequestPort(String bidderRequestPort) {
    this.bidderRequestPort = bidderRequestPort;
    hasBidderRequestPort = true;
  }

  public void clearBidderRequestPort() {
    bidderRequestPort = null;
    hasBidderRequestPort = false;
  }

  public boolean hasBidderRequestPort() {
    return hasBidderRequestPort;
  }

  public static BalancerResource buildBalancer(
      Project project,
      ResourceCollectionId resourceCollectionId,
      Instance instance) {

    BalancerResource balancerResource = new BalancerResource();
    configureInstance(
        project,
        balancerResource,
        resourceCollectionId,
        instance);
    if (instance.getMetadata() != null) {
      ImmutableMap<String, String> metadataMap = ComputeUtils.toMap(instance.getMetadata());
      balancerResource.setUserDistUri(metadataMap.get(InstanceBuilder.METADATA_USER_DIST));
      balancerResource.setBidderRequestPort(
          metadataMap.get(LoadBalancerInstanceBuilder.METADATA_BIDDER_REQUEST_PORT));
      balancerResource.setRequestPort(metadataMap.get(
          LoadBalancerInstanceBuilder.METADATA_REQUEST_PORT));
      balancerResource.setHaProxyStatPort(metadataMap.get(
          LoadBalancerInstanceBuilder.METADATA_HAPROXY_STATS_PORT));
      balancerResource.setImage(getImageId(project.getId(), metadataMap,
          InstanceBuilder.METADATA_IMAGE, InstanceBuilder.METADATA_IMAGE_TYPE));
    }
    balancerResource.setInstanceType(InstanceType.BALANCER);
    return balancerResource;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        super.hashCode(),
        haProxyStatPort,
        requestPort,
        bidderRequestPort
    );
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof BalancerResource) || !super.equals(o)) {
      return false;
    }
    BalancerResource other = (BalancerResource) o;
    return Objects.equal(haProxyStatPort, other.haProxyStatPort)
        && Objects.equal(requestPort, other.requestPort)
        && Objects.equal(bidderRequestPort, other.bidderRequestPort)
        && Objects.equal(hasHaProxyStatPort, other.hasHaProxyStatPort)
        && Objects.equal(hasRequestPort, other.hasRequestPort)
        && Objects.equal(hasBidderRequestPort, other.hasBidderRequestPort);
  }

  @Override
  protected MoreObjects.ToStringHelper toStringHelper() {
    return super.toStringHelper()
        .add("haProxyStatPort", haProxyStatPort)
        .add("requestPort", requestPort)
        .add("bidderRequestPort", bidderRequestPort);
  }
}
