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

package com.google.openbidder.ui.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.openbidder.ui.resource.support.ResourceId;
import com.google.openbidder.ui.resource.support.ResourceType;
import com.google.openbidder.ui.util.validation.CidrList;
import com.google.openbidder.ui.util.validation.Uri;
import com.google.openbidder.ui.util.validation.UrlCollection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.OnSave;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * A project represents a single client project with associated bidders, DoubleClick Ad
 * Exchange RTB ad units, Google APIs and Cloud Storage access and so on. Potentially there
 * may be several projects within the same AppEngine instance and a user may belong to more
 * than one project in the future.
 */
@Entity
public class Project {

  @Id
  @Index
  private Long id;

  // Human-readable description
  @NotNull
  @Size(min = 5, max = 200)
  @Index
  private String projectName;

  // OAuth2 access to Google APIs
  @JsonIgnore
  @NotNull
  @Size(max = 200)
  private String oauth2ClientId;

  @JsonIgnore
  @NotNull
  @Size(max = 200)
  private String oauth2ClientSecret;

  @JsonIgnore
  @NotNull
  @Size(max = 200)
  private String apiProjectId;

  @JsonIgnore
  @NotNull
  private Long apiProjectNumber;

  @JsonIgnore
  @NotNull
  private String vmParameters;

  @JsonIgnore
  @NotNull
  private String mainParameters;

  private List<String> bidInterceptors = new ArrayList<>();

  private List<String> impressionInterceptors = new ArrayList<>();

  private List<String> clickInterceptors = new ArrayList<>();

  private List<String> matchInterceptors = new ArrayList<>();

  private String encryptionKey;

  private String integrityKey;

  private List<Zone> zones = new ArrayList<>();

  @NotNull
  @Uri(scheme = "gs://", message = "must start with 'gs://'")
  private String userDistUri;

  @CidrList
  private List<String> whiteListedIpRanges = new ArrayList<>();

  @Index
  private String networkName;

  @Index
  private String projectUuid;

  @Index
  private String adExchangeBuyerAccountId;

  @Index
  private String doubleClickReportingBucket;

  @Index
  private String doubleClickPreferredDealsBucket;

  private String bidderRequestPort;

  private String bidderAdminPort;

  private String loadBalancerRequestPort;

  private String loadBalancerStatPort;

  private String cookieMatchUrl;

  private String cookieMatchNid;

  private String bidderImage;

  private boolean isBidderImageDefault;

  private String loadBalancerImage;

  private boolean isLoadBalancerImageDefault;

  private Integer auctionType;

  @UrlCollection(message="must be valid OAuth 2.0 scope URLs")
  private List<String> bidderOauth2Scopes = new ArrayList<>();

  @UrlCollection(message="must be valid OAuth 2.0 scope URLs")
  private List<String> loadBalancerOauth2Scopes = new ArrayList<>();

  @JsonIgnore
  public Key<Project> getKey() {
    return key(id);
  }

  public Long getId() {
    return id;
  }

  /**
   * Get project description. The description is purely for convenience when selecting a project
   * and is not part of any global namespace nor unique.
   */
  public String getProjectName() {
    return projectName;
  }

  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  /**
   * OAuth 2.0 client ID. See https://developers.google.com/accounts/docs/OAuth2.
   */
  public String getOauth2ClientId() {
    return oauth2ClientId;
  }

  public void setOauth2ClientId(String oauth2ClientId) {
    this.oauth2ClientId = oauth2ClientId;
  }

  /**
   * OAuth 2.0 client secret. See https://developers.google.com/accounts/docs/OAuth2.
   */
  public String getOauth2ClientSecret() {
    return oauth2ClientSecret;
  }

  public void setOauth2ClientSecret(String oauth2ClientSecret) {
    this.oauth2ClientSecret = oauth2ClientSecret;
  }

  /**
   * Google API project ID. Used here for Google Compute.
   */
  public String getApiProjectId() {
    return apiProjectId;
  }

  public void setApiProjectId(String apiProjectId) {
    this.apiProjectId = apiProjectId;
  }

  public Long getApiProjectNumber() {
    return apiProjectNumber;
  }

  public void setApiProjectNumber(Long apiProjectNumber) {
    this.apiProjectNumber = apiProjectNumber;
  }

  /**
   * DoubleClick Ad Exchange Buyer account id.
   */
  public String getAdExchangeBuyerAccountId() {
    return adExchangeBuyerAccountId;
  }

  public void setAdExchangeBuyerAccountId(String adExchangeBuyerAccountId) {
    this.adExchangeBuyerAccountId = adExchangeBuyerAccountId;
  }

  /**
   * Custom JVM parameters for project's bidder processes.
   */
  public String getVmParameters() {
    return vmParameters;
  }

  public void setVmParameters(String vmParameters) {
    this.vmParameters = vmParameters;
  }

  /**
   * Custom main program parameters for project's bidder processes.
   */
  public String getMainParameters() {
    return mainParameters;
  }

  public void setMainParameters(String mainParameters) {
    this.mainParameters = mainParameters;
  }

  /**
   * A list of bidder interceptors.
   */
  public List<String> getBidInterceptors() {
    return bidInterceptors;
  }

  public void setBidInterceptors(List<String> bidInterceptors) {
    this.bidInterceptors = bidInterceptors;
  }

  /**
   * A list of impression tracking interceptors.
   */
  public List<String> getImpressionInterceptors() {
    return impressionInterceptors;
  }

  public void setImpressionInterceptors(List<String> impressionInterceptors) {
    this.impressionInterceptors = impressionInterceptors;
  }

  /**
   * A list of click tracking interceptors.
   */
  public List<String> getClickInterceptors() {
    return clickInterceptors;
  }

  public void setClickInterceptors(List<String> clickInterceptors) {
    this.clickInterceptors = clickInterceptors;
  }

  /**
   * @return List of cookie matching interceptors.
   */
  public List<String> getMatchInterceptors() {
    return matchInterceptors;
  }

  public void setMatchInterceptors(List<String> matchInterceptors) {
    this.matchInterceptors = matchInterceptors;
  }

  /**
   * (DoubleClick Ad Exchange specific) The encryption key for winning price decoding.
   */
  public String getEncryptionKey() {
    return encryptionKey;
  }

  public void setEncryptionKey(String encryptionKey) {
    this.encryptionKey = encryptionKey;
  }

  /**
   * (DoubleClick Ad Exchange specific) The integrity key for winning price decoding.
   */
  public String getIntegrityKey() {
    return integrityKey;
  }

  public void setIntegrityKey(String integrityKey) {
    this.integrityKey = integrityKey;
  }

  /**
   * @return Default bidder machine type for a given zone or {@code null} if not found.
   */
  public String getBidderMachineType(final String zoneName) {
    Zone zone = findZone(zoneName);
    return zone == null ? null : zone.bidderMachineType;
  }

  public void setBidderMachineType(final String zoneName, final String machineTypeName) {
    getOrCreate(zoneName).setBidderMachineType(machineTypeName);
  }

  /**
   * @return Mapping from Zone ID to default bidder machine type.
   */
  public Map<String, ResourceId> getBidderMachineTypes() {
    Map<String, ResourceId> map = new LinkedHashMap<>();
    for (Zone zone : zones) {
      String bidderMachineType = zone.getBidderMachineType();
      if (!Strings.isNullOrEmpty(bidderMachineType)) {
        map.put(zone.getId(), ResourceType.MACHINE_TYPE.getResourceId(
            Long.toString(id), zone.getId(), bidderMachineType));
      }
    }
    return map;
  }

  /**
   * @return Default load balancer machine type for a given zone or {@code null} if not found.
   */
  public String getLoadBalancerMachineType(final String zoneName) {
    Zone zone = findZone(zoneName);
    return zone == null ? null : zone.loadBalancerMachineType;
  }

  public void setLoadBalancerMachineType(final String zoneName, final String machineTypeName) {
    getOrCreate(zoneName).setLoadBalancerMachineType(machineTypeName);
  }

  /**
   * @return Mapping from Zone ID to default load balancer machine type.
   */
  public Map<String, ResourceId> getLoadBalancerMachineTypes() {
    Map<String, ResourceId> map = new LinkedHashMap<>();
    for (Zone zone : zones) {
      String loadBalancerMachineType = zone.getLoadBalancerMachineType();
      if (!Strings.isNullOrEmpty(loadBalancerMachineType)) {
        map.put(zone.getId(), ResourceType.MACHINE_TYPE.getResourceId(
            Long.toString(id), zone.getId(), loadBalancerMachineType));
      }
    }
    return map;
  }

  /**
   * @return Specific host name for a given zone or {@code null} if not found.
   */
  public String getZoneHost(final String zoneName) {
    Zone zone = findZone(zoneName);
    return zone == null ? null : zone.hostName;
  }

  public void setZoneHost(final String zoneName, String hostName) {
    getOrCreate(zoneName).setHostName(hostName);
  }

  /**
   * @return Mapping from zone host name to Zone ID.
   */
  public Map<String, String> getZoneHosts() {
    Map<String, String> map = new LinkedHashMap<>();
    for (Zone zone : zones) {
      String hostName = zone.getHostName();
      if (!Strings.isNullOrEmpty(hostName)) {
        map.put(hostName, zone.getId());
      }
    }
    return map;
  }

  private Zone getOrCreate(String zoneName) {
    List<Zone> zones = this.zones;
    Zone zone = findZone(zoneName);
    if (zone == null) {
      zone = new Zone();
      zone.setId(zoneName);
      zones.add(zone);
    }
    return zone;
  }

  private @Nullable Zone findZone(final String zoneName) {
    if (zones == null) {
      return null;
    }
    return Iterables.find(zones, new Predicate<Zone>() {
      @Override public boolean apply(Zone zone) {
        return zone.id.equals(zoneName);
      }},
      /* default value */ null);
  }

  /**
   * @return Google Cloud Storage URI for user's distribution artifacts.
   */
  public String getUserDistUri() {
    return userDistUri;
  }

  public void setUserDistUri(String userDistUri) {
    this.userDistUri = userDistUri;
  }

  /**
   * @return List of IP ranges, in CIDR notation, with whitelisted access to bidders and
   *          load balancers maintenance information.
   */
  public List<String> getWhiteListedIpRanges() {
    return whiteListedIpRanges;
  }

  public void setWhiteListedIpRanges(List<String> whiteListedIpRanges) {
    this.whiteListedIpRanges = whiteListedIpRanges;
  }

  /**
   * @return Network name.
   */
  public String getNetworkName() {
    return networkName;
  }

  public void setNetworkName(String networkName) {
    this.networkName = networkName;
  }

  /**
   * @return Cloud storage bucket where DoubleClick reports are uploaded
   */
  public String getDoubleClickReportingBucket() {
    return doubleClickReportingBucket;
  }

  public void setDoubleClickReportingBucket(String doubleClickReportingBucket) {
    this.doubleClickReportingBucket = doubleClickReportingBucket;
  }

  /**
   * @return Cloud storage bucket where DoubleClick preferred deals are uploaded
   */
  public String getDoubleClickPreferredDealsBucket() {
    return doubleClickPreferredDealsBucket;
  }

  public void setDoubleClickPreferredDealsBucket(String doubleClickPreferredDealsBucket) {
    this.doubleClickPreferredDealsBucket = doubleClickPreferredDealsBucket;
  }

  /**
   * @return A project UUID, set on first persist.
   */
  public String getProjectUuid() {
    if (Strings.isNullOrEmpty(projectUuid)) {
      generateProjectUuid();
    }
    return projectUuid;
  }

  @OnSave
  private void generateProjectUuid() {
    if (Strings.isNullOrEmpty(projectUuid)) {
      this.projectUuid = UUID.randomUUID().toString().replace("-", "");
    }
  }

  /**
   * @return Bidder request port
   */
  public String getBidderRequestPort() {
    return bidderRequestPort;
  }

  public void setBidderRequestPort(String bidderRequestPort) {
    this.bidderRequestPort = bidderRequestPort;
  }

  /**
   * @return Bidder admin port
   */
  public String getBidderAdminPort() {
    return bidderAdminPort;
  }

  public void setBidderAdminPort(String bidderAdminPort) {
    this.bidderAdminPort = bidderAdminPort;
  }

  /**
   * @return Load balancer request port
   */
  public String getLoadBalancerRequestPort() {
    return loadBalancerRequestPort;
  }

  public void setLoadBalancerRequestPort(String loadBalancerRequestPort) {
    this.loadBalancerRequestPort = loadBalancerRequestPort;
  }

  /**
   * @return Load balancer statistics port
   */
  public String getLoadBalancerStatPort() {
    return loadBalancerStatPort;
  }

  public void setLoadBalancerStatPort(String loadBalancerStatPort) {
    this.loadBalancerStatPort = loadBalancerStatPort;
  }

  /**
   * @return Cookie match nid for matching DoubleClick cookies
   */
  public String getCookieMatchNid() {
    return cookieMatchNid;
  }

  public void setCookieMatchNid(String cookieMatchNid) {
    this.cookieMatchNid = cookieMatchNid;
  }

  /**
   * @return Cookie match Url for matching DoubleClick cookies
   */
  public String getCookieMatchUrl() {
    return cookieMatchUrl;
  }

  public void setCookieMatchUrl(String cookieMatchUrl) {
    this.cookieMatchUrl = cookieMatchUrl;
  }

  /**
   * @return Bidder OS image
   */
  public String getBidderImage() {
    return bidderImage;
  }

  public void setBidderImage(String bidderImage) {
    this.bidderImage = bidderImage;
  }

  /**
   * @return {@code true} if this bidder image is a default one, otherwise  {@code false}.
   */
  public boolean getIsBidderImageDefault() {
    return isBidderImageDefault;
  }

  public void setIsBidderImageDefault(boolean isBidderImageDefault) {
    this.isBidderImageDefault = isBidderImageDefault;
  }

  /**
   * @return Load balancer OS image
   */
  public String getLoadBalancerImage() {
    return loadBalancerImage;
  }

  public void setLoadBalancerImage(String loadBalancerImage) {
    this.loadBalancerImage = loadBalancerImage;
  }

  /**
   * @return {@code true} if this load balancer image is a default one, otherwise  {@code false}.
   */
  public boolean getIsLoadBalancerImageDefault() {
    return isLoadBalancerImageDefault;
  }

  public void setIsLoadBalancerImageDefault(boolean isLoadBalancerImageDefault) {
    this.isLoadBalancerImageDefault = isLoadBalancerImageDefault;
  }

  /**
   * @return Auction type
   */
  public Integer getAuctionType() {
    return auctionType;
  }

  public void setAuctionType(Integer auctionType) {
    this.auctionType = auctionType;
  }

  /**
   * @return A list of Oauth2 Scopes bidder instances on Google Compute Engine
   * used to access other Google services.
   */
  public List<String> getBidderOauth2Scopes() {
    return bidderOauth2Scopes;
  }

  public void setBidderOauth2Scopes(List<String> bidderOauth2Scopes) {
    this.bidderOauth2Scopes = bidderOauth2Scopes;
  }

  /**
   * @return A list of Oauth2 Scopes load balancer instances on Google Compute Engine require.
   */
  public List<String> getLoadBalancerOauth2Scopes() {
    return loadBalancerOauth2Scopes;
  }

  public void setLoadBalancerOauth2Scopes(List<String> loadBalancerOauth2Scopes) {
    this.loadBalancerOauth2Scopes = loadBalancerOauth2Scopes;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("id", id)
        .add("projectName", projectName)
        .add("projectUuid", projectUuid)
        .add("adExchangeBuyerAccountId", adExchangeBuyerAccountId)
        .add("apiProjectId", apiProjectId)
        .add("apiProjectNumber", apiProjectNumber)
        .add("bidInterceptors", bidInterceptors)
        .add("impressionInterceptors", impressionInterceptors)
        .add("clickInterceptors", clickInterceptors)
        .add("matchInterceptors", matchInterceptors)
        .add("zones", zones)
        .add("whiteListedIpRanges", whiteListedIpRanges)
        .add("networkName", networkName)
        .add("doubleClickReportingBucket", doubleClickReportingBucket)
        .add("doubleClickPreferredDealsBucket", doubleClickPreferredDealsBucket)
        .add("auctionType", auctionType)
        .add("userDistUri", userDistUri)
        .add("bidderRequestPort", bidderRequestPort)
        .add("bidderAdminPort", bidderAdminPort)
        .add("loadBalancerRequestPort", loadBalancerRequestPort)
        .add("loadBalancerStatPort", loadBalancerStatPort)
        .add("cookieMatchUrl", cookieMatchUrl)
        .add("cookieMatchNid", cookieMatchNid)
        .add("bidderImage", bidderImage)
        .add("isBidderImageDefault", isBidderImageDefault)
        .add("loadBalancerImage", loadBalancerImage)
        .add("isLoadBalancerImageDefault", isLoadBalancerImageDefault)
        .add("bidderOauth2Scopes", bidderOauth2Scopes)
        .add("loadBalancerOauth2Scopes", loadBalancerOauth2Scopes)
        .add("vmParameters", vmParameters)
        .add("mainParameters", mainParameters)
        .toString();
  }

  public static Key<Project> key(long projectId) {
    return Key.create(Project.class, projectId);
  }

  private static class Zone {
    @Index
    private String id;

    @Index
    private String hostName;

    private String bidderMachineType;

    private String loadBalancerMachineType;

    private Zone() {
    }

    /**
     * @return Zone ID.
     */
    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    /**
     * @return Zone host name.
     */
    public String getHostName() {
      return hostName;
    }

    public void setHostName(String hostName) {
      this.hostName = hostName;
    }
    /**
     * Default Google Compute Engine instance type used for bidder in this zone.
     */
    public String getBidderMachineType() {
      return bidderMachineType;
    }

    public void setBidderMachineType(String bidderMachineType) {
      this.bidderMachineType = bidderMachineType;
    }

    /**
     * Default Google Compute Engine instance type used for load balancers in this zone.
     */
    public String getLoadBalancerMachineType() {
      return loadBalancerMachineType;
    }

    public void setLoadBalancerMachineType(String loadBalancerMachineType) {
      this.loadBalancerMachineType = loadBalancerMachineType;
    }

    @Override public int hashCode() {
      return Objects.hashCode(id, hostName, bidderMachineType, loadBalancerMachineType);
    }

    @Override public boolean equals(@Nullable Object obj) {
      if (this == obj) {
        return true;
      } else if (!(obj instanceof Zone)) {
        return false;
      }
      Zone other = (Zone) obj;
      return Objects.equal(id, other.id)
          && Objects.equal(hostName, other.hostName)
          && Objects.equal(bidderMachineType, other.bidderMachineType)
          && Objects.equal(loadBalancerMachineType, other.loadBalancerMachineType);
    }

    @Override public String toString() {
      return MoreObjects.toStringHelper(this).omitNullValues()
          .add("id", id)
          .add("hostname", hostName)
          .add("bidderMachineType", bidderMachineType)
          .add("loadBalancerMachineType", loadBalancerMachineType)
          .toString();
    }
  }
}
