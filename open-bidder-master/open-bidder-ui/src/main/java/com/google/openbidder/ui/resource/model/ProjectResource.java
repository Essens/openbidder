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

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.openbidder.ui.entity.support.ProjectRole;
import com.google.openbidder.ui.resource.support.ExternalResource;
import com.google.openbidder.ui.resource.support.ResourceId;
import com.google.openbidder.ui.resource.support.ResourceType;
import com.google.openbidder.ui.util.json.ProjectRoleDeserializer;
import com.google.openbidder.ui.util.json.ResourceIdDeserializer;
import com.google.openbidder.ui.util.validation.CidrList;
import com.google.openbidder.ui.util.validation.Create;
import com.google.openbidder.ui.util.validation.ResourcePathType;
import com.google.openbidder.ui.util.validation.Uri;
import com.google.openbidder.ui.util.validation.UrlCollection;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.groups.Default;

/**
 * Represents a set of resources required for an RTB setup including zones, networks,
 * firewalls, bidders, load balancers and uploads.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectResource extends ExternalResource {

  @NotNull(groups = {Create.class})
  @Size(max = 200, groups = Default.class)
  private String oauth2ClientId;

  @NotNull(groups = {Create.class})
  @Size(max = 200, groups = Default.class)
  private String oauth2ClientSecret;

  @NotNull(groups = {Create.class})
  @Size(max = 200, groups = Default.class)
  private String apiProjectId;

  @NotNull(groups = {Create.class})
  private Long apiProjectNumber;

  @ResourcePathType(type = ResourceType.ACCOUNT)
  private ResourceId adExchangeBuyerAccount;

  private String vmParameters;

  private String mainParameters;

  private List<String> bidInterceptors;

  private List<String> impressionInterceptors;

  private List<String> clickInterceptors;

  private List<String> matchInterceptors;

  @CidrList
  private List<String> whiteListedIpRanges;

  @ResourcePathType(type = ResourceType.NETWORK)
  private ResourceId network;

  private String projectUuid;

  private Boolean defaultProject;

  private ProjectRole projectRole;

  private Boolean authorized;

  private Boolean apiProjectAuthorized;

  private List<QuotaResource> quotas;

  private String bidderRequestPort;

  private String bidderAdminPort;

  private String loadBalancerRequestPort;

  private String loadBalancerStatPort;

  @Uri(scheme = "gs://", message = "must start with 'gs://'")
  private String userDistUri;

  private ResourceId bidderImage;

  private ResourceId loadBalancerImage;

  @UrlCollection(message="must be valid OAuth 2.0 scope URLs")
  private List<String> bidderOauth2Scopes;

  @UrlCollection(message="must be valid OAuth 2.0 scope URLs")
  private List<String> loadBalancerOauth2Scopes;

  private String cookieMatchUrl;

  private String cookieMatchNid;

  private Map<String, ResourceId> bidderMachineTypes;

  private Map<String, ResourceId> loadBalancerMachineTypes;

  private DoubleClickProjectResource doubleClickProjectResource;

  private String doubleClickPreferredDealsBucket;

  private Integer auctionType;

  private boolean hasOauth2ClientId;
  private boolean hasOauth2ClientSecret;
  private boolean hasApiProjectId;
  private boolean hasApiProjectNumber;
  private boolean hasAdExchangeBuyerAccount;
  private boolean hasVmParameters;
  private boolean hasMainParameters;
  private boolean hasBidInterceptors;
  private boolean hasImpressionInterceptors;
  private boolean hasClickInterceptors;
  private boolean hasMatchInterceptors;
  private boolean hasWhiteListedIpRanges;
  private boolean hasNetwork;
  private boolean hasProjectUuid;
  private boolean hasDefaultProject;
  private boolean hasProjectRole;
  private boolean hasAuthorized;
  private boolean hasApiProjectAuthorized;
  private boolean hasQuotas;
  private boolean hasBidderRequestPort;
  private boolean hasBidderAdminPort;
  private boolean hasLoadBalancerRequestPort;
  private boolean hasLoadBalancerStatPort;
  private boolean hasUserDistUri;
  private boolean hasBidderOauth2Scopes;
  private boolean hasLoadBalancerOauth2Scopes;
  private boolean hasBidderImage;
  private boolean hasLoadBalancerImage;
  private boolean hasCookieMatchUrl;
  private boolean hasCookieMatchNid;
  private boolean hasBidderMachineTypes;
  private boolean hasLoadBalancerMachineTypes;
  private boolean hasDoubleClickProjectResource;
  private boolean hasDoubleClickPreferredDealsBucket;
  private boolean hasAuctionType;

  public String getOauth2ClientId() {
    return oauth2ClientId;
  }

  public void setOauth2ClientId(String oauth2ClientId) {
    this.oauth2ClientId = oauth2ClientId;
    hasOauth2ClientId = true;
  }

  public void clearOauth2ClientId() {
    oauth2ClientId = null;
    hasOauth2ClientId = false;
  }

  public boolean hasOauth2ClientId() {
    return hasOauth2ClientId;
  }

  public String getOauth2ClientSecret() {
    return oauth2ClientSecret;
  }

  public void setOauth2ClientSecret(String oauth2ClientSecret) {
    this.oauth2ClientSecret = oauth2ClientSecret;
    hasOauth2ClientSecret = true;
  }

  public void clearOauth2ClientSecret() {
    oauth2ClientSecret = null;
    hasOauth2ClientSecret = false;
  }

  public boolean hasOauth2ClientSecret() {
    return hasOauth2ClientSecret;
  }

  public String getApiProjectId() {
    return apiProjectId;
  }

  public void setApiProjectId(String apiProjectId) {
    this.apiProjectId = apiProjectId;
    hasApiProjectId = true;
  }

  public void clearApiProjectId() {
    apiProjectId = null;
    hasApiProjectId = false;
  }

  public boolean hasApiProjectId() {
    return hasApiProjectId;
  }

  public Long getApiProjectNumber() {
    return apiProjectNumber;
  }

  public void setApiProjectNumber(Long apiProjectNumber) {
    this.apiProjectNumber = apiProjectNumber;
    hasApiProjectNumber = true;
  }

  public void clearApiProjectNumber() {
    apiProjectNumber = null;
    hasApiProjectNumber = false;
  }

  public boolean hasApiProjectNumber() {
    return hasApiProjectNumber;
  }

  public ResourceId getAdExchangeBuyerAccount() {
    return adExchangeBuyerAccount;
  }

  @JsonDeserialize(using = ResourceIdDeserializer.class)
  public void setAdExchangeBuyerAccount(ResourceId adExchangeBuyerAccount) {
    this.adExchangeBuyerAccount = adExchangeBuyerAccount;
    hasAdExchangeBuyerAccount = true;
  }

  public void clearAdExchangeBuyerAccount() {
    adExchangeBuyerAccount = null;
    hasAdExchangeBuyerAccount = false;
  }

  public boolean hasAdExchangeBuyerAccount() {
    return hasAdExchangeBuyerAccount;
  }

  public String getVmParameters() {
    return vmParameters;
  }

  public void setVmParameters(String vmParameters) {
    this.vmParameters = vmParameters;
    hasVmParameters = true;
  }

  public void clearVmParameters() {
    vmParameters = null;
    hasVmParameters = false;
  }

  public boolean hasVmParameters() {
    return hasVmParameters;
  }

  public String getMainParameters() {
    return mainParameters;
  }

  public void setMainParameters(String mainParameters) {
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

  public List<String> getBidInterceptors() {
    return bidInterceptors;
  }

  public void setBidInterceptors(List<String> bidInterceptors) {
    this.bidInterceptors = bidInterceptors;
    hasBidInterceptors = true;
  }

  public void clearBidInterceptors() {
    bidInterceptors = null;
    hasBidInterceptors = false;
  }

  public boolean hasBidInterceptors() {
    return hasBidInterceptors;
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

  public List<String> getMatchInterceptors() {
    return matchInterceptors;
  }

  public void setMatchInterceptors(List<String> matchInterceptors) {
    this.matchInterceptors = matchInterceptors;
    hasMatchInterceptors = true;
  }

  public void clearMatchInterceptors() {
    matchInterceptors = null;
    hasMatchInterceptors = false;
  }

  public boolean hasMatchInterceptors() {
    return hasMatchInterceptors;
  }

  public List<String> getWhiteListedIpRanges() {
    return whiteListedIpRanges;
  }

  public void setWhiteListedIpRanges(List<String> whiteListedIpRanges) {
    this.whiteListedIpRanges = whiteListedIpRanges;
    hasWhiteListedIpRanges = true;
  }

  public void clearWhiteListedIpRanges() {
    whiteListedIpRanges = null;
    hasWhiteListedIpRanges = false;
  }

  public boolean hasWhiteListedIpRanges() {
    return hasWhiteListedIpRanges;
  }

  public ResourceId getNetwork() {
    return network;
  }

  @JsonDeserialize(using = ResourceIdDeserializer.class)
  public void setNetwork(ResourceId network) {
    this.network = network;
    hasNetwork = true;
  }

  public void clearNetwork() {
    network = null;
    hasNetwork = false;
  }

  public boolean hasNetwork() {
    return hasNetwork;
  }

  public String getProjectUuid() {
    return projectUuid;
  }

  public void setProjectUuid(String projectUuid) {
    this.projectUuid = projectUuid;
    hasProjectUuid = true;
  }

  public void clearProjectUuid() {
    projectUuid = null;
    hasProjectUuid = false;
  }

  public boolean hasProjectUuid() {
    return hasProjectUuid;
  }

  public Boolean getDefaultProject() {
    return defaultProject;
  }

  public void setDefaultProject(Boolean defaultProject) {
    this.defaultProject = defaultProject;
    hasDefaultProject = true;
  }

  public void clearDefaultProject() {
    defaultProject = null;
    hasDefaultProject = false;
  }

  public boolean hasDefaultProject() {
    return hasDefaultProject;
  }

  public ProjectRole getProjectRole() {
    return projectRole;
  }

  @JsonDeserialize(using = ProjectRoleDeserializer.class)
  public void setProjectRole(ProjectRole projectRole) {
    this.projectRole = projectRole;
    hasProjectRole = true;
  }

  public void clearProjectRole() {
    projectRole = null;
    hasProjectRole = false;
  }

  public boolean hasProjectRole() {
    return hasProjectRole;
  }

  public Boolean getAuthorized() {
    return authorized;
  }

  public void setAuthorized(Boolean authorized) {
    this.authorized = authorized;
    hasAuthorized = true;
  }

  public void clearAuthorized() {
    authorized = null;
    hasAuthorized = false;
  }

  public boolean hasAuthorized() {
    return hasAuthorized;
  }

  public Boolean getApiProjectAuthorized() {
    return apiProjectAuthorized;
  }

  public void setApiProjectAuthorized(Boolean apiProjectAuthorized) {
    this.apiProjectAuthorized = apiProjectAuthorized;
    hasApiProjectAuthorized = true;
  }

  public void clearApiProjectAuthorized() {
    apiProjectAuthorized = null;
    hasApiProjectAuthorized = false;
  }

  public boolean hasApiProjectAuthorized() {
    return hasApiProjectAuthorized;
  }

  public List<QuotaResource> getQuotas() {
    return quotas;
  }

  public void setQuotas(List<QuotaResource> quotas) {
    this.quotas = quotas;
    hasQuotas = true;
  }

  public void clearQuotas() {
    quotas = null;
    hasQuotas = false;
  }

  public boolean hasQuotas() {
    return hasQuotas;
  }

  public String getLoadBalancerRequestPort() {
    return loadBalancerRequestPort;
  }

  public void setLoadBalancerRequestPort(String loadBalancerRequestPort) {
    this.loadBalancerRequestPort = loadBalancerRequestPort;
    hasLoadBalancerRequestPort = true;
  }

  public void clearLoadBalancerRequestPort() {
    loadBalancerRequestPort = null;
    hasLoadBalancerRequestPort = false;
  }

  public boolean hasLoadBalancerRequestPort() {
    return hasLoadBalancerRequestPort;
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

  public String getBidderAdminPort() {
    return bidderAdminPort;
  }

  public void setBidderAdminPort(String bidderAdminPort) {
    this.bidderAdminPort = bidderAdminPort;
    hasBidderAdminPort = true;
  }

  public void clearBidderAdminPort() {
    bidderAdminPort = null;
    hasBidderAdminPort = false;
  }

  public boolean hasBidderAdminPort() {
    return hasBidderAdminPort;
  }

  public String getLoadBalancerStatPort() {
    return loadBalancerStatPort;
  }

  public void setLoadBalancerStatPort(String loadBalancerStatPort) {
    this.loadBalancerStatPort = loadBalancerStatPort;
    hasLoadBalancerStatPort = true;
  }

  public void clearLoadBalancerStatPort() {
    loadBalancerStatPort = null;
    hasLoadBalancerStatPort = false;
  }

  public boolean hasLoadBalancerStatPort() {
    return hasLoadBalancerStatPort;
  }

  public String getUserDistUri() {
    return userDistUri;
  }

  public void setUserDistUri(String userDistUri) {
    this.userDistUri = userDistUri;
    hasUserDistUri = true;
  }

  public void clearUserDistUri() {
    userDistUri = null;
    hasUserDistUri = false;
  }

  public boolean hasUserDistUri() {
    return hasUserDistUri;
  }

  public List<String> getBidderOauth2Scopes() {
    return bidderOauth2Scopes;
  }

  public void setBidderOauth2Scopes(List<String> bidderOauth2Scopes) {
    this.bidderOauth2Scopes = bidderOauth2Scopes;
    hasBidderOauth2Scopes = true;
  }

  public boolean hasBidderOauth2Scopes() {
    return hasBidderOauth2Scopes;
  }

  public void clearBidderOauth2Scopes() {
    bidderOauth2Scopes = null;
    hasBidderOauth2Scopes = false;
  }

  public List<String> getLoadBalancerOauth2Scopes() {
    return loadBalancerOauth2Scopes;
  }

  public void setLoadBalancerOauth2Scopes(List<String> loadBalancerOauth2Scopes) {
    this.loadBalancerOauth2Scopes = loadBalancerOauth2Scopes;
    hasLoadBalancerOauth2Scopes = true;
  }

  public boolean hasLoadBalancerOauth2Scopes() {
    return hasLoadBalancerOauth2Scopes;
  }

  public void clearLoadBalancerOauth2Scopes() {
    loadBalancerOauth2Scopes = null;
    hasLoadBalancerOauth2Scopes = false;
  }

  public ResourceId getBidderImage() {
    return bidderImage;
  }

  @JsonDeserialize(using = ResourceIdDeserializer.class)
  public void setBidderImage(ResourceId bidderImage) {
    this.bidderImage = bidderImage;
    hasBidderImage = true;
  }

  public void clearBidderImage() {
    bidderImage = null;
    hasBidderImage = false;
  }

  public boolean hasBidderImage() {
    return hasBidderImage;
  }

  public ResourceId getLoadBalancerImage() {
    return loadBalancerImage;
  }

  @JsonDeserialize(using = ResourceIdDeserializer.class)
  public void setLoadBalancerImage(ResourceId loadBalancerImage) {
    this.loadBalancerImage = loadBalancerImage;
    hasLoadBalancerImage = true;
  }

  public void clearLoadBalancerImage() {
    loadBalancerImage = null;
    hasLoadBalancerImage = false;
  }

  public boolean hasLoadBalancerImage() {
    return hasLoadBalancerImage;
  }

  public String getCookieMatchUrl() {
    return cookieMatchUrl;
  }

  public void setCookieMatchUrl(String cookieMatchUrl) {
    this.cookieMatchUrl = cookieMatchUrl;
    hasCookieMatchUrl = true;
  }

  public void clearCookieMatchUrl() {
    cookieMatchUrl = null;
    hasCookieMatchUrl = false;
  }

  public boolean hasCookieMatchUrl() {
    return hasCookieMatchUrl;
  }

  public String getCookieMatchNid() {
    return cookieMatchNid;
  }

  public void setCookieMatchNid(String cookieMatchNid) {
    this.cookieMatchNid = cookieMatchNid;
    hasCookieMatchNid = true;
  }

  public void clearCookieMatchNid() {
    cookieMatchNid = null;
    hasCookieMatchNid = false;
  }

  public boolean hasCookieMatchNid() {
    return hasCookieMatchNid;
  }

  public Map<String, ResourceId> getBidderMachineTypes() {
    return bidderMachineTypes;
  }

  public void setBidderMachineTypes(Map<String, ResourceId> bidderMachineTypes) {
    this.bidderMachineTypes = bidderMachineTypes;
    hasBidderMachineTypes = true;
  }

  public void clearBidderMachineTypes() {
    bidderMachineTypes = null;
    hasBidderMachineTypes = false;
  }

  public boolean hasBidderMachineTypes() {
    return hasBidderMachineTypes;
  }

  public Map<String, ResourceId> getLoadBalancerMachineTypes() {
    return loadBalancerMachineTypes;
  }

  public void setLoadBalancerMachineTypes(Map<String, ResourceId> loadBalancerMachineTypes) {
    this.loadBalancerMachineTypes = loadBalancerMachineTypes;
    hasLoadBalancerMachineTypes = true;
  }

  public void clearLoadBalancerMachineTypes() {
    this.loadBalancerMachineTypes = null;
    hasLoadBalancerMachineTypes = false;
  }

  public boolean hasLoadBalancerMachineTypes() {
    return hasLoadBalancerMachineTypes;
  }

  public DoubleClickProjectResource getDoubleClickProjectResource() {
    return doubleClickProjectResource;
  }

  public void setDoubleClickProjectResource(DoubleClickProjectResource doubleClickProjectResource) {
    this.doubleClickProjectResource = doubleClickProjectResource;
    hasDoubleClickProjectResource = true;
  }

  public void clearDoubleClickProjectResource() {
    this.doubleClickProjectResource = null;
    hasDoubleClickProjectResource = false;
  }

  public boolean hasDoubleClickProjectResource() {
    return hasDoubleClickProjectResource;
  }

  public String getDoubleClickPreferredDealsBucket() {
    return doubleClickPreferredDealsBucket;
  }

  public void setDoubleClickPreferredDealsBucket(String doubleClickPreferredDealsBucket) {
    this.doubleClickPreferredDealsBucket = doubleClickPreferredDealsBucket;
    hasDoubleClickPreferredDealsBucket = true;
  }

  public void clearDoubleClickPreferredDealsBucket() {
    doubleClickPreferredDealsBucket = null;
    hasDoubleClickPreferredDealsBucket = false;
  }

  public boolean hasDoubleClickPreferredDealsBucket() {
    return hasDoubleClickPreferredDealsBucket;
  }

  public Integer getAuctionType() {
    return auctionType;
  }

  public void setAuctionType(Integer auctionType) {
    this.auctionType = auctionType;
    hasAuctionType = true;
  }

  public void clearAuctionType() {
    auctionType = null;
    hasAuctionType = false;
  }

  public boolean hasAuctionType() {
    return hasAuctionType;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        super.hashCode(),
        oauth2ClientId,
        oauth2ClientSecret,
        apiProjectId,
        apiProjectNumber,
        adExchangeBuyerAccount,
        vmParameters,
        mainParameters,
        bidInterceptors,
        impressionInterceptors,
        clickInterceptors,
        matchInterceptors,
        whiteListedIpRanges,
        network,
        projectUuid,
        defaultProject,
        projectRole,
        authorized,
        apiProjectAuthorized,
        quotas,
        bidderRequestPort,
        bidderAdminPort,
        loadBalancerRequestPort,
        loadBalancerStatPort,
        userDistUri,
        bidderMachineTypes,
        loadBalancerMachineTypes,
        bidderImage,
        loadBalancerImage,
        bidderOauth2Scopes,
        loadBalancerOauth2Scopes,
        cookieMatchUrl,
        cookieMatchNid,
        doubleClickProjectResource
    );
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof ProjectResource) || !super.equals(o)) {
      return false;
    }
    ProjectResource other = (ProjectResource) o;
    return Objects.equal(oauth2ClientId, other.oauth2ClientId)
        && Objects.equal(oauth2ClientSecret, other.oauth2ClientSecret)
        && Objects.equal(apiProjectId, other.apiProjectId)
        && Objects.equal(apiProjectNumber, other.apiProjectNumber)
        && Objects.equal(adExchangeBuyerAccount, other.adExchangeBuyerAccount)
        && Objects.equal(vmParameters, other.vmParameters)
        && Objects.equal(mainParameters, other.mainParameters)
        && Objects.equal(bidInterceptors, other.bidInterceptors)
        && Objects.equal(impressionInterceptors, other.impressionInterceptors)
        && Objects.equal(clickInterceptors, other.clickInterceptors)
        && Objects.equal(matchInterceptors, other.matchInterceptors)
        && Objects.equal(whiteListedIpRanges, other.whiteListedIpRanges)
        && Objects.equal(network, other.network)
        && Objects.equal(projectUuid, other.projectUuid)
        && Objects.equal(defaultProject, other.defaultProject)
        && Objects.equal(projectRole, other.projectRole)
        && Objects.equal(authorized, other.authorized)
        && Objects.equal(apiProjectAuthorized, other.apiProjectAuthorized)
        && Objects.equal(quotas, other.quotas)
        && Objects.equal(bidderRequestPort, other.bidderRequestPort)
        && Objects.equal(bidderAdminPort, other.bidderAdminPort)
        && Objects.equal(loadBalancerRequestPort, other.loadBalancerRequestPort)
        && Objects.equal(loadBalancerStatPort, other.loadBalancerStatPort)
        && Objects.equal(userDistUri, other.userDistUri)
        && Objects.equal(bidderMachineTypes, other.bidderMachineTypes)
        && Objects.equal(loadBalancerMachineTypes, other.loadBalancerMachineTypes)
        && Objects.equal(bidderImage, other.bidderImage)
        && Objects.equal(loadBalancerImage, other.loadBalancerImage)
        && Objects.equal(bidderOauth2Scopes, other.bidderOauth2Scopes)
        && Objects.equal(loadBalancerOauth2Scopes, other.loadBalancerOauth2Scopes)
        && Objects.equal(cookieMatchUrl, other.cookieMatchUrl)
        && Objects.equal(cookieMatchNid, other.cookieMatchNid)
        && Objects.equal(doubleClickProjectResource, other.doubleClickProjectResource)
        && Objects.equal(doubleClickPreferredDealsBucket, other.doubleClickPreferredDealsBucket)
        && Objects.equal(auctionType, other.auctionType)
        && Objects.equal(hasOauth2ClientId, other.hasOauth2ClientId)
        && Objects.equal(hasOauth2ClientSecret, other.hasOauth2ClientSecret)
        && Objects.equal(hasApiProjectId, other.hasApiProjectId)
        && Objects.equal(hasApiProjectNumber, other.hasApiProjectNumber)
        && Objects.equal(hasAdExchangeBuyerAccount, other.hasAdExchangeBuyerAccount)
        && Objects.equal(hasVmParameters, other.hasVmParameters)
        && Objects.equal(hasMainParameters, other.hasMainParameters)
        && Objects.equal(hasBidInterceptors, other.hasBidInterceptors)
        && Objects.equal(hasImpressionInterceptors, other.hasImpressionInterceptors)
        && Objects.equal(hasClickInterceptors, other.hasClickInterceptors)
        && Objects.equal(hasMatchInterceptors, other.hasMatchInterceptors)
        && Objects.equal(hasWhiteListedIpRanges, other.hasWhiteListedIpRanges)
        && Objects.equal(hasNetwork, other.hasNetwork)
        && Objects.equal(hasProjectUuid, other.hasProjectUuid)
        && Objects.equal(hasDefaultProject, other.hasDefaultProject)
        && Objects.equal(hasProjectRole, other.hasProjectRole)
        && Objects.equal(hasAuthorized, other.hasAuthorized)
        && Objects.equal(hasApiProjectAuthorized, other.hasApiProjectAuthorized)
        && Objects.equal(hasQuotas, other.hasQuotas)
        && Objects.equal(hasBidderRequestPort, other.hasBidderRequestPort)
        && Objects.equal(hasBidderAdminPort, other.hasBidderAdminPort)
        && Objects.equal(hasLoadBalancerRequestPort, other.hasLoadBalancerRequestPort)
        && Objects.equal(hasLoadBalancerStatPort, other.hasLoadBalancerStatPort)
        && Objects.equal(hasUserDistUri, other.hasUserDistUri)
        && Objects.equal(hasBidderMachineTypes, other.hasBidderMachineTypes)
        && Objects.equal(hasLoadBalancerMachineTypes, other.hasLoadBalancerMachineTypes)
        && Objects.equal(hasBidderImage, other.hasBidderImage)
        && Objects.equal(hasLoadBalancerImage, other.hasLoadBalancerImage)
        && Objects.equal(hasBidderOauth2Scopes, other.hasBidderOauth2Scopes)
        && Objects.equal(hasLoadBalancerOauth2Scopes, other.hasLoadBalancerOauth2Scopes)
        && Objects.equal(hasCookieMatchUrl, other.hasCookieMatchUrl)
        && Objects.equal(hasCookieMatchNid, other.hasCookieMatchNid)
        && Objects.equal(hasDoubleClickProjectResource, other.hasDoubleClickProjectResource)
        && Objects.equal(
        hasDoubleClickPreferredDealsBucket, other.hasDoubleClickPreferredDealsBucket)
        && Objects.equal(hasAuctionType, other.hasAuctionType());
  }

  @Override
  protected MoreObjects.ToStringHelper toStringHelper() {
    return super.toStringHelper()
        .add("apiProjectId", apiProjectId)
        .add("apiProjectNumber", apiProjectNumber)
        .add("adExchangeBuyerAccount", adExchangeBuyerAccount)
        .add("vmParameters", vmParameters)
        .add("mainParameters", mainParameters)
        .add("bidInterceptors", bidInterceptors)
        .add("impressionInterceptors", impressionInterceptors)
        .add("clickInterceptors", clickInterceptors)
        .add("matchInterceptors", matchInterceptors)
        .add("whiteListedIpRanges", whiteListedIpRanges)
        .add("network", network)
        .add("projectUuid", projectUuid)
        .add("defaultProject", defaultProject)
        .add("projectRole", projectRole)
        .add("authorized", authorized)
        .add("apiProjectAuthorized", apiProjectAuthorized)
        .add("quotas", quotas)
        .add("bidderRequestPort", bidderRequestPort)
        .add("bidderAdminPort", bidderAdminPort)
        .add("loadBalancerRequestPort", loadBalancerRequestPort)
        .add("loadBalancerStatPort", loadBalancerStatPort)
        .add("userDistUri", userDistUri)
        .add("bidderMachineTypes", bidderMachineTypes)
        .add("loadBalancerMachineTypes", loadBalancerMachineTypes)
        .add("bidderImage", bidderImage)
        .add("loadBalancerImage", loadBalancerImage)
        .add("bidderOauth2Scopes", bidderOauth2Scopes)
        .add("loadBalancerOauth2Scopes", loadBalancerOauth2Scopes)
        .add("cookieMatchUrl", cookieMatchUrl)
        .add("cookieMatchNid", cookieMatchNid)
        .add("doubleClickProjectResource", doubleClickProjectResource)
        .add("doubleClickPreferredDealsBucket", doubleClickPreferredDealsBucket)
        .add("auctionType", auctionType);
  }
}
