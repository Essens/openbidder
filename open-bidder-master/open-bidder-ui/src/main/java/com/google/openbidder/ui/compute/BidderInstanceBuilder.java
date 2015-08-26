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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.api.services.compute.model.Instance;
import com.google.api.services.compute.model.Tags;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.openbidder.system.Platform;
import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.project.ProjectUser;
import com.google.openbidder.ui.resource.model.CustomBidderResource;
import com.google.openbidder.ui.resource.support.ResourceId;
import com.google.openbidder.util.Clock;

import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Builds bidder {@link Instance}s.
 */
public class BidderInstanceBuilder extends InstanceBuilder {

  public static final String TAG = "bidder";

  public static final String METADATA_PLATFORM = "platform";
  public static final String METADATA_LISTEN_PORT = "listen_port";
  public static final String METADATA_ADMIN_PORT = "admin_port";
  public static final String METADATA_BALANCER_HOST = "load_balancer_host";
  public static final String METADATA_BALANCER_PORT = "load_balancer_port";
  public static final String METADATA_BID_INTERCEPTORS = "bid_interceptors";
  public static final String METADATA_CLICK_INTERCEPTORS = "click_interceptors";
  public static final String METADATA_IMPRESSION_INTERCEPTORS = "impression_interceptors";
  public static final String METADATA_JVM_PARAMETERS = "jvm_parameters";
  public static final String METADATA_MAIN_PARAMETERS = "main_parameters";
  public static final String METADATA_PROJECT_ID = "api_project_id";
  public static final String METADATA_PROJECT_NUMBER = "api_project_number";
  public static final String METADATA_DC_ENCRYPTION_KEY = "doubleclick_encryption_key";
  public static final String METADATA_DC_INTEGRITY_KEY = "doubleclick_integrity_key";
  public static final String METADATA_DC_MATCH_INTERCEPTORS = "doubleclick_match_interceptors";
  public static final String METADATA_DC_MATCH_NID = "doubleclick_match_nid";
  public static final String METADATA_DC_MATCH_URL = "doubleclick_match_url";

  private final BidderParameters bidderParameters;

  @Inject
  public BidderInstanceBuilder(
      Clock clock,
      BidderParameters bidderParameters,
      @Value("${OpenBidder.Project.Version}") String projectVersion) {
    super(clock, projectVersion, readScriptResource(bidderParameters.getBootstrapScript()));
    this.bidderParameters = checkNotNull(bidderParameters);
    checkArgument(!Strings.isNullOrEmpty(projectVersion));
  }

  /**
   * @return Bidder {@link Instance} for the {@link Project} and the zone.
   */
  public Instance build(
      ProjectUser projectUser,
      ResourceId zoneId,
      String instanceName,
      @Nullable CustomBidderResource customBidderResource) {
    Project project = projectUser.getProject();
    Instance instance = new Instance();
    String zoneName = zoneId.getResourceName();
    if (!Strings.isNullOrEmpty(project.getBidderMachineType(zoneName))) {
      ResourceName machineTypeName = ComputeResourceType.MACHINE_TYPE.buildName(
          project, zoneName, project.getBidderMachineType(zoneName));
      instance.setMachineType(machineTypeName.getResourceUrl());
    }

    String zoneUrl = ResourceName.buildName(projectUser, zoneId).getResourceUrl();
    instance.setName(instanceName);
    instance.setDescription(buildDescription(projectUser));
    instance.setZone(zoneUrl);
    instance.setTags(new Tags().setItems(Arrays.asList(TAG)));
    instance.setNetworkInterfaces(buildInstanceNetwork(project,
        bidderParameters.isEnableExternalIps()));
    Set<String> bidderOauth2Scopes = new HashSet<>();
    bidderOauth2Scopes.addAll(project.getBidderOauth2Scopes());
    bidderOauth2Scopes.addAll(bidderParameters.getDefaultOauth2Scopes());
    instance.setServiceAccounts(buildServiceAccounts(ImmutableList.copyOf(bidderOauth2Scopes)));

    Joiner joiner = Joiner.on(",");
    ImmutableMap.Builder<String, String> metadata = createMetadata(project, zoneId)
        .put(METADATA_PLATFORM, Platform.GOOGLE_COMPUTE.name())
        .put(METADATA_LISTEN_PORT, project.getBidderRequestPort())
        .put(METADATA_ADMIN_PORT, project.getBidderAdminPort())
        .put(METADATA_JVM_PARAMETERS, project.getVmParameters().replaceAll("\\s+", " "))
        .put(METADATA_PROJECT_ID, project.getApiProjectId())
        .put(METADATA_IMAGE, project.getBidderImage())
        .put(METADATA_IMAGE_TYPE, project.getIsBidderImageDefault() ? IMAGE_DEFAULT : IMAGE_CUSTOM);
    if (project.getApiProjectNumber() != null) {
      metadata.put(METADATA_PROJECT_NUMBER, project.getApiProjectNumber().toString());
    }
    if (customBidderResource != null && customBidderResource.hasMainParameters()) {
      metadata.put(METADATA_MAIN_PARAMETERS,
          customBidderResource.getMainParameters().replaceAll("\\s+", " "));
    } else {
      metadata.put(METADATA_MAIN_PARAMETERS, project.getMainParameters().replaceAll("\\s+", " "));
    }
    if (project.getBidInterceptors() != null && !project.getBidInterceptors().isEmpty()) {
      metadata.put(METADATA_BID_INTERCEPTORS, joiner.join(project.getBidInterceptors()));
    }
    if (project.getImpressionInterceptors() != null
        && !project.getImpressionInterceptors().isEmpty()) {
      metadata.put(METADATA_IMPRESSION_INTERCEPTORS,
          joiner.join(project.getImpressionInterceptors()));
    }
    if (project.getClickInterceptors() != null && !project.getClickInterceptors().isEmpty()) {
      metadata.put(METADATA_CLICK_INTERCEPTORS, joiner.join(project.getClickInterceptors()));
    }
    String host = project.getZoneHost(zoneId.getResourceName());
    if (!Strings.isNullOrEmpty(host)) {
      metadata.put(METADATA_BALANCER_HOST, host);
      String port = project.getLoadBalancerRequestPort();
      if (!Strings.isNullOrEmpty(port)) {
        metadata.put(METADATA_BALANCER_PORT, port);
      }
    }
    // TODO(opinali): set METADATA_IMPRESSION_URL, METADATA_CLICK_URL (need ui)
    if (!Strings.isNullOrEmpty(project.getEncryptionKey())) {
      metadata.put(METADATA_DC_ENCRYPTION_KEY, project.getEncryptionKey());
    }
    if (!Strings.isNullOrEmpty(project.getIntegrityKey())) {
      metadata.put(METADATA_DC_INTEGRITY_KEY, project.getIntegrityKey());
    }
    if (!Strings.isNullOrEmpty(project.getCookieMatchNid()) &&
        !Strings.isNullOrEmpty(project.getCookieMatchUrl())) {
      if (project.getMatchInterceptors() != null && !project.getMatchInterceptors().isEmpty()) {
        metadata.put(METADATA_DC_MATCH_INTERCEPTORS, joiner.join(project.getMatchInterceptors()));
      }
      metadata.put(METADATA_DC_MATCH_NID, project.getCookieMatchNid());
      metadata.put(METADATA_DC_MATCH_URL, project.getCookieMatchUrl());
    }
    instance.setMetadata(buildMetadata(metadata.build()));
    return instance;
  }

  public static @Nullable String getLoadBalancerUri(Map<String, String> metadata) {
    String host = metadata.get(BidderInstanceBuilder.METADATA_BALANCER_HOST);
    if (Strings.isNullOrEmpty(host)) {
      return null;
    }
    String port = metadata.get(BidderInstanceBuilder.METADATA_BALANCER_PORT);
    URIBuilder uri = new URIBuilder().setScheme("http").setHost(host);
    if (!Strings.isNullOrEmpty(port)) {
      uri.setPort(Integer.parseInt(port));
    }
    return uri.toString();
  }
}
