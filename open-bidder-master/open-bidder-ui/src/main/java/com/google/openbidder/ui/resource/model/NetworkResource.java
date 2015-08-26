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
import com.google.openbidder.ui.resource.support.ExternalResource;
import com.google.openbidder.ui.util.json.InstantDeserializer;
import com.google.openbidder.ui.util.json.InstantSerializer;
import com.google.openbidder.ui.util.validation.CidrList;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.Instant;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Represents a project-specific virtual network.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NetworkResource extends ExternalResource {

  private String ipv4Range;
  private Instant createdAt;
  @CidrList
  private List<String> whiteListedIpRanges;
  private String bidderRequestPort;
  private String bidderAdminPort;
  private String loadBalancerRequestPort;
  private String loadBalancerStatPort;
  private List<FirewallResource> firewalls;

  private boolean hasIpv4Range;
  private boolean hasCreatedAt;
  private boolean hasWhiteListedIpRanges;
  private boolean hasBidderRequestPort;
  private boolean hasBidderAdminPort;
  private boolean hasLoadBalancerRequestPort;
  private boolean hasLoadBalancerStatPort;
  private boolean hasFirewalls;

  public String getIpv4Range() {
    return ipv4Range;
  }

  public void setIpv4Range(String ipv4Range) {
    this.ipv4Range = ipv4Range;
    hasIpv4Range = true;
  }

  public void clearIpv4Range() {
    ipv4Range = null;
    hasIpv4Range = false;
  }

  public boolean hasIpv4Range() {
    return hasIpv4Range;
  }

  @JsonSerialize(using = InstantSerializer.class)
  public Instant getCreatedAt() {
    return createdAt;
  }

  @JsonDeserialize(using = InstantDeserializer.class)
  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
    hasCreatedAt = true;
  }

  public void clearCreatedAt() {
    createdAt = null;
    hasCreatedAt = false;
  }

  public boolean hasCreatedAt() {
    return hasCreatedAt;
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

  @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
  public List<FirewallResource> getFirewalls() {
    return firewalls;
  }

  public void setFirewalls(List<FirewallResource> firewalls) {
    this.firewalls = firewalls;
    hasFirewalls = true;
  }

  public void clearFirewalls() {
    firewalls = null;
    hasFirewalls = false;
  }

  public boolean hasFirewalls() {
    return hasFirewalls;
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

  @Override
  public int hashCode() {
    return Objects.hashCode(
        super.hashCode(),
        ipv4Range,
        createdAt,
        whiteListedIpRanges,
        bidderRequestPort,
        bidderAdminPort,
        loadBalancerRequestPort,
        loadBalancerStatPort,
        firewalls
    );
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof NetworkResource) || !super.equals(o)) {
      return false;
    }
    NetworkResource other = (NetworkResource) o;
    return Objects.equal(ipv4Range, other.ipv4Range)
        && Objects.equal(createdAt, other.createdAt)
        && Objects.equal(whiteListedIpRanges, other.whiteListedIpRanges)
        && Objects.equal(bidderRequestPort, other.bidderRequestPort)
        && Objects.equal(bidderAdminPort, other.bidderAdminPort)
        && Objects.equal(loadBalancerRequestPort, other.loadBalancerRequestPort)
        && Objects.equal(loadBalancerStatPort, other.loadBalancerStatPort)
        && Objects.equal(firewalls, other.firewalls)
        && Objects.equal(hasIpv4Range, other.hasIpv4Range)
        && Objects.equal(hasCreatedAt, other.hasCreatedAt)
        && Objects.equal(hasWhiteListedIpRanges, other.hasWhiteListedIpRanges)
        && Objects.equal(hasBidderRequestPort, other.hasBidderRequestPort)
        && Objects.equal(hasBidderAdminPort, other.hasBidderAdminPort)
        && Objects.equal(hasLoadBalancerRequestPort, other.hasLoadBalancerRequestPort)
        && Objects.equal(hasLoadBalancerStatPort, other.hasLoadBalancerStatPort)
        && Objects.equal(hasFirewalls, other.hasFirewalls);
  }

  @Override
  protected MoreObjects.ToStringHelper toStringHelper() {
    return super.toStringHelper()
        .add("ipv4Range", ipv4Range)
        .add("createdAt", createdAt)
        .add("whiteListedIpRanges", whiteListedIpRanges)
        .add("bidderRequestPort", bidderRequestPort)
        .add("bidderAdminPort", bidderAdminPort)
        .add("loadBalancerRequestPort", loadBalancerRequestPort)
        .add("loadBalancerStatPort", loadBalancerStatPort)
        .add("firewalls", firewalls);
  }
}
