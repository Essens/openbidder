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

package com.google.openbidder.ui.compute;

import com.google.api.services.compute.model.Instance;
import com.google.api.services.compute.model.Tags;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.project.ProjectUser;
import com.google.openbidder.ui.resource.support.ResourceId;
import com.google.openbidder.util.Clock;

import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.Set;

import javax.inject.Inject;

/**
 * Builds load balancer {@link com.google.api.services.compute.model.Instance}
 * for the Google Compute API.
 */
public class LoadBalancerInstanceBuilder extends InstanceBuilder {

  public static final String TAG = "load-balancer";

  public static final String METADATA_BIDDER_REQUEST_PORT = "bidder_request_port";
  public static final String METADATA_REQUEST_PORT = "request_port";
  public static final String METADATA_HAPROXY_STATS_PORT = "haproxy_stat_port";

  private final LoadBalancerParameters loadBalancerParameters;

  @Inject
  public LoadBalancerInstanceBuilder(
      Clock clock,
      LoadBalancerParameters loadBalancerParameters,
      @Value("${OpenBidder.Project.Version}") String projectVersion) {
    super(clock, projectVersion, readScriptResource(loadBalancerParameters.getBootstrapScript()));
    this.loadBalancerParameters = loadBalancerParameters;
  }

  /**
   * @return LoadBalancer {@link Instance} for the {@link Project} and the zone.
   */
  public Instance build(ProjectUser projectUser, ResourceId zoneId, String instanceName) {
    Project project = projectUser.getProject();
    Instance instance = new Instance();
    instance.setName(instanceName);
    instance.setDescription(buildDescription(projectUser));
    String zoneName = zoneId.getResourceName();
    if (!Strings.isNullOrEmpty(project.getLoadBalancerMachineType(zoneName))) {
      ResourceName machineTypeName = ComputeResourceType.MACHINE_TYPE.buildName(
          project, zoneName, project.getLoadBalancerMachineType(zoneName));
      instance.setMachineType(machineTypeName.getResourceUrl());
    }

    instance.setZone(ResourceName.buildName(projectUser, zoneId).getResourceUrl());
    instance.setNetworkInterfaces(ImmutableList.copyOf(buildInstanceNetwork(project, true)));
    Set<String> loadBalancerOauth2Scopes = Sets.union(
        ImmutableSet.copyOf(project.getLoadBalancerOauth2Scopes()),
        ImmutableSet.copyOf(loadBalancerParameters.getDefaultOauth2Scopes()));
    instance.setServiceAccounts(buildServiceAccounts(
        ImmutableList.copyOf(loadBalancerOauth2Scopes)));
    instance.setTags(new Tags().setItems(Arrays.asList(TAG)));

    ImmutableMap.Builder<String, String> builder = createMetadata(project, zoneId)
        .put(METADATA_BIDDER_REQUEST_PORT, project.getBidderRequestPort())
        .put(METADATA_REQUEST_PORT, project.getLoadBalancerRequestPort())
        .put(METADATA_HAPROXY_STATS_PORT, project.getLoadBalancerStatPort())
        .put(METADATA_IMAGE, project.getLoadBalancerImage())
        .put(METADATA_IMAGE_TYPE,
            project.getIsLoadBalancerImageDefault() ? IMAGE_DEFAULT : IMAGE_CUSTOM);
    instance.setMetadata(buildMetadata(builder.build()));
    return instance;
  }
}
