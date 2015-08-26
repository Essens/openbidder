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
import com.google.openbidder.ui.compute.BidderInstanceBuilder;
import com.google.openbidder.ui.compute.ComputeUtils;
import com.google.openbidder.ui.compute.InstanceBuilder;
import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.resource.support.InstanceType;
import com.google.openbidder.ui.resource.support.ResourceCollectionId;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Represents a project-specific bidder.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BidderResource extends InstanceResource {

  private String requestPort;
  private String adminPort;
  private String callbackUrl;
  private List<String> interceptors;
  private List<String> impressionInterceptors;
  private List<String> clickInterceptors;
  private List<String> jvmParameters;
  private List<String> mainParameters;
  private String zoneHost;

  private boolean hasRequestPort;
  private boolean hasAdminPort;
  private boolean hasCallbackUrl;
  private boolean hasInterceptors;
  private boolean hasImpressionInterceptors;
  private boolean hasClickInterceptors;
  private boolean hasJvmParameters;
  private boolean hasMainParameters;
  private boolean hasZoneHost;

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

  public String getAdminPort() {
    return adminPort;
  }

  public void setAdminPort(String adminPort) {
    this.adminPort = adminPort;
    hasAdminPort = true;
  }

  public void clearAdminPort() {
    adminPort = null;
    hasAdminPort = false;
  }

  public boolean hasAdminPort() {
    return hasAdminPort;
  }

  public String getCallbackUrl() {
    return callbackUrl;
  }

  public void setCallbackUrl(String callbackUrl) {
    this.callbackUrl = callbackUrl;
    hasCallbackUrl = true;
  }

  public void clearCallbackUrl() {
    callbackUrl = null;
    hasCallbackUrl = false;
  }

  public boolean hasCallbackUrl() {
    return hasCallbackUrl;
  }

  public List<String> getInterceptors() {
    return interceptors;
  }

  public void setInterceptors(List<String> interceptors) {
    this.interceptors = interceptors;
    hasInterceptors = true;
  }

  public void clearInterceptors() {
    interceptors = null;
    hasInterceptors = false;
  }

  public boolean hasInterceptors() {
    return hasInterceptors;
  }

  public List<String> getImpressionInterceptors() {
    return impressionInterceptors;
  }

  public void setImpressionInterceptors(List<String> impressionInterceptors) {
    this.impressionInterceptors = impressionInterceptors;
    hasImpressionInterceptors = true;
  }

  public void clearImpressionInterceptors() {
    impressionInterceptors = null;
    hasImpressionInterceptors = false;
  }

  public boolean hasImpressionInterceptors() {
    return hasImpressionInterceptors;
  }

  public List<String> getClickInterceptors() {
    return clickInterceptors;
  }

  public void setClickInterceptors(List<String> clickInterceptors) {
    this.clickInterceptors = clickInterceptors;
    hasClickInterceptors = true;
  }

  public void clearClickInterceptors() {
    clickInterceptors = null;
    hasClickInterceptors = false;
  }

  public boolean hasClickInterceptors() {
    return hasClickInterceptors;
  }

  public List<String> getJvmParameters() {
    return jvmParameters;
  }

  public void setJvmParameters(List<String> jvmParameters) {
    this.jvmParameters = jvmParameters;
    hasJvmParameters = true;
  }

  public void clearJvmParameters() {
    jvmParameters = null;
    hasJvmParameters = false;
  }

  public boolean hasJvmParameters() {
    return hasJvmParameters;
  }

  public List<String> getMainParameters() {
    return mainParameters;
  }

  public void setMainParameters(List<String> mainParameters) {
    this.mainParameters = mainParameters;
    hasMainParameters = true;
  }

  public void clearMainParameters() {
    mainParameters = null;
    hasMainParameters = false;
  }

  public boolean hasMainParameters() {
    return hasMainParameters;
  }

  public String getZoneHost() {
    return zoneHost;
  }

  public void setZoneHost(String zoneHost) {
    this.zoneHost = zoneHost;
    hasZoneHost = true;
  }

  public boolean hasZoneHost() {
    return hasZoneHost;
  }

  public void clearZoneHost() {
    hasZoneHost = false;
    this.zoneHost = null;
  }

  public static BidderResource buildBidder(
      Project project,
      ResourceCollectionId resourceCollectionId,
      Instance instance) {

    BidderResource bidderResource = new BidderResource();
    configureInstance(
        project,
        bidderResource,
        resourceCollectionId,
        instance);
    if (instance.getMetadata() != null) {
      ImmutableMap<String, String> metadataMap = ComputeUtils.toMap(instance.getMetadata());
      bidderResource.setUserDistUri(metadataMap.get(InstanceBuilder.METADATA_USER_DIST));
      bidderResource.setRequestPort(metadataMap.get(BidderInstanceBuilder.METADATA_LISTEN_PORT));
      bidderResource.setAdminPort(metadataMap.get(BidderInstanceBuilder.METADATA_ADMIN_PORT));
      String callbackUri = BidderInstanceBuilder.getLoadBalancerUri(metadataMap);
      if (callbackUri != null) {
        bidderResource.setCallbackUrl(callbackUri);
      }
      bidderResource.setJvmParameters(ComputeUtils.getParametersFromMetadata(
          instance, BidderInstanceBuilder.METADATA_JVM_PARAMETERS));
      bidderResource.setMainParameters(ComputeUtils.getParametersFromMetadata(
          instance, BidderInstanceBuilder.METADATA_MAIN_PARAMETERS));
      bidderResource.setInterceptors(getInterceptors(metadataMap,
          BidderInstanceBuilder.METADATA_BID_INTERCEPTORS));
      bidderResource.setImpressionInterceptors(getInterceptors(metadataMap,
          BidderInstanceBuilder.METADATA_IMPRESSION_INTERCEPTORS));
      bidderResource.setClickInterceptors(getInterceptors(metadataMap,
          BidderInstanceBuilder.METADATA_CLICK_INTERCEPTORS));
      bidderResource.setZoneHost(metadataMap.get(InstanceBuilder.METADATA_ZONE_HOST));
      bidderResource.setImage(getImageId(project.getId(), metadataMap,
          InstanceBuilder.METADATA_IMAGE, InstanceBuilder.METADATA_IMAGE_TYPE));
    }
    bidderResource.setInstanceType(InstanceType.BIDDER);
    return bidderResource;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        super.hashCode(),
        requestPort,
        adminPort,
        interceptors,
        impressionInterceptors,
        clickInterceptors,
        jvmParameters,
        mainParameters,
        zoneHost
    );
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof BidderResource) || !super.equals(o)) {
      return false;
    }
    BidderResource other = (BidderResource) o;
    return Objects.equal(requestPort, other.requestPort)
        && Objects.equal(adminPort, other.adminPort)
        && Objects.equal(callbackUrl, other.callbackUrl)
        && Objects.equal(interceptors, other.interceptors)
        && Objects.equal(impressionInterceptors, other.impressionInterceptors)
        && Objects.equal(clickInterceptors, other.clickInterceptors)
        && Objects.equal(jvmParameters, other.jvmParameters)
        && Objects.equal(mainParameters, other.mainParameters)
        && Objects.equal(zoneHost, other.zoneHost)
        && Objects.equal(hasRequestPort, other.hasRequestPort)
        && Objects.equal(hasAdminPort, other.hasAdminPort)
        && Objects.equal(hasCallbackUrl, other.hasCallbackUrl)
        && Objects.equal(hasInterceptors, other.hasInterceptors)
        && Objects.equal(hasImpressionInterceptors, other.hasImpressionInterceptors)
        && Objects.equal(hasClickInterceptors, other.hasClickInterceptors)
        && Objects.equal(hasJvmParameters, other.hasJvmParameters)
        && Objects.equal(hasMainParameters, other.hasMainParameters)
        && Objects.equal(hasZoneHost, other.hasZoneHost);
  }

  @Override
  protected MoreObjects.ToStringHelper toStringHelper() {
    return super.toStringHelper()
        .add("requestPort", requestPort)
        .add("adminPort", adminPort)
        .add("interceptors", interceptors)
        .add("impressionInterceptors", impressionInterceptors)
        .add("clickInterceptors", clickInterceptors)
        .add("jvmParameters", jvmParameters)
        .add("mainParameters", mainParameters)
        .add("zoneHost", zoneHost);
  }
}
