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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.api.services.compute.model.Instance;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.openbidder.ui.compute.BidderInstanceBuilder;
import com.google.openbidder.ui.compute.LoadBalancerInstanceBuilder;
import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.resource.model.BalancerResource;
import com.google.openbidder.ui.resource.model.BidderResource;
import com.google.openbidder.ui.resource.model.InstanceResource;

import com.fasterxml.jackson.annotation.JsonValue;

import javax.annotation.Nullable;

/**
 * Instance type.
 */
public enum InstanceType {
  BIDDER("bidder") {
    @Override public BidderResource build(
        Project project, ResourceCollectionId resourceCollectionId, Instance instance) {
      return BidderResource.buildBidder(project, resourceCollectionId, instance);
    }
  },
  BALANCER("balancer") {
    @Override public BalancerResource build(
        Project project, ResourceCollectionId resourceCollectionId, Instance instance) {
      return BalancerResource.buildBalancer(project, resourceCollectionId, instance);
    }
  },
  UNKNOWN("unknown");

  private static final ImmutableMap<String, InstanceType> LOOKUP = ImmutableMap.copyOf(
      Maps.uniqueIndex(ImmutableList.copyOf(values()), new Function<InstanceType, String>() {
        @Override public String apply(InstanceType instanceType) {
          return instanceType.getInstanceType();
        }}));

  private final String instanceType;

  private InstanceType(String instanceType) {
    this.instanceType = instanceType;
  }

  @JsonValue
  public String getInstanceType() {
    return instanceType;
  }

  @Nullable
  public static InstanceType fromInstanceType(String text) {
    return LOOKUP.get(text);
  }

  public static InstanceType fromInstance(Instance instance) {
    if (isBidder(instance)) {
      return BIDDER;
    } else if (isLoadBalancer(instance)) {
      return BALANCER;
    } else {
      return UNKNOWN;
    }
  }

  public static boolean isBidder(Instance instance) {
    return hasTag(instance, BidderInstanceBuilder.TAG);
  }

  public static boolean isLoadBalancer(Instance instance) {
    return hasTag(instance, LoadBalancerInstanceBuilder.TAG);
  }

  public static boolean hasTag(Instance instance, String tag) {
    return  instance.getTags() != null
        && instance.getTags().getItems() != null
        && instance.getTags().getItems().contains(checkNotNull(tag));
  }

  public InstanceResource build(
      Project project, ResourceCollectionId resourceCollectionId, Instance instance) {
    return InstanceResource.buildInstance(project, resourceCollectionId, instance);
  }
}
