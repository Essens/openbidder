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

import static com.google.common.base.Preconditions.checkState;

import com.google.api.services.compute.model.Firewall;
import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.openbidder.ui.compute.ResourceName;
import com.google.openbidder.ui.resource.support.ExternalResource;
import com.google.openbidder.ui.resource.support.ResourceCollectionId;
import com.google.openbidder.ui.resource.support.ResourceId;
import com.google.openbidder.ui.resource.support.ResourceType;
import com.google.openbidder.ui.util.json.InstantDeserializer;
import com.google.openbidder.ui.util.json.InstantSerializer;
import com.google.openbidder.ui.util.json.ResourceIdDeserializer;
import com.google.openbidder.ui.util.validation.ResourcePathType;
import com.google.openbidder.ui.util.web.WebUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.joda.time.Instant;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Represents a project-specific firewall.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FirewallResource extends ExternalResource {

  private static final Function<Firewall.Allowed, Allowed> FROM_ALLOWED =
      new Function<Firewall.Allowed, FirewallResource.Allowed>() {
        @Override public FirewallResource.Allowed apply(Firewall.Allowed firewallAllowed) {
          FirewallResource.Allowed allowed = new FirewallResource.Allowed();
          allowed.setProtocol(firewallAllowed.getIPProtocol());
          allowed.setPorts(firewallAllowed.getPorts());
          return allowed;
        }};

  @ResourcePathType(type = ResourceType.NETWORK)
  private ResourceId network;
  private Instant createdAt;
  private List<String> sourceRanges;
  private List<String> sourceTags;
  private List<String> targetTags;
  private List<Allowed> allowed;

  private boolean hasNetwork;
  private boolean hasCreatedAt;
  private boolean hasSourceRanges;
  private boolean hasSourceTags;
  private boolean hasTargetTags;
  private boolean hasAllowed;

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

  @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
  public List<String> getSourceRanges() {
    return sourceRanges;
  }

  public void setSourceRanges(List<String> sourceRanges) {
    this.sourceRanges = sourceRanges;
    hasSourceRanges = true;
  }

  public void clearSourceRanges() {
    sourceRanges = null;
    hasSourceRanges = false;
  }

  public boolean hasSourceRanges() {
    return hasSourceRanges;
  }

  @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
  public List<String> getSourceTags() {
    return sourceTags;
  }

  public void setSourceTags(List<String> sourceTags) {
    this.sourceTags = sourceTags;
    hasSourceTags = true;
  }

  public void clearSourceTags() {
    sourceTags = null;
    hasSourceTags = false;
  }

  public boolean hasSourceTags() {
    return hasSourceTags;
  }

  public List<String> getTargetTags() {
    return targetTags;
  }

  public void setTargetTags(List<String> targetTags) {
    this.targetTags = targetTags;
    hasTargetTags = true;
  }

  public void clearTargetTags() {
    targetTags = null;
    hasTargetTags = false;
  }

  public boolean hasTargetTags() {
    return hasTargetTags;
  }

  public List<Allowed> getAllowed() {
    return allowed;
  }

  public void setAllowed(List<Allowed> allowed) {
    this.allowed = allowed;
    hasAllowed = true;
  }

  public void clearAllowed() {
    allowed = null;
    hasAllowed = false;
  }

  public boolean hasAllowed() {
    return hasAllowed;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        super.hashCode(),
        network,
        createdAt,
        sourceRanges,
        sourceTags,
        targetTags,
        allowed
    );
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof FirewallResource) || !super.equals(o)) {
      return false;
    }
    FirewallResource other = (FirewallResource) o;
    return Objects.equal(network, other.network)
        && Objects.equal(createdAt, other.createdAt)
        && Objects.equal(sourceRanges, other.sourceRanges)
        && Objects.equal(sourceTags, other.sourceTags)
        && Objects.equal(targetTags, other.targetTags)
        && Objects.equal(allowed, other.allowed)
        && Objects.equal(hasNetwork, other.hasNetwork)
        && Objects.equal(hasCreatedAt, other.hasCreatedAt)
        && Objects.equal(hasSourceRanges, other.hasSourceRanges)
        && Objects.equal(hasSourceTags, other.hasSourceTags)
        && Objects.equal(hasTargetTags, other.hasTargetTags)
        && Objects.equal(hasAllowed, other.hasAllowed);
  }

  @Override
  protected MoreObjects.ToStringHelper toStringHelper() {
    return super.toStringHelper()
        .add("network", network)
        .add("createdAt", createdAt)
        .add("sourceRanges", sourceRanges)
        .add("sourceTags", sourceTags)
        .add("targetTags", targetTags)
        .add("allowed", allowed);
  }

  public static FirewallResource build(
      String apiProjectId,
      ResourceCollectionId resourceCollectionId,
      Firewall firewall) {

    FirewallResource firewallResource = new FirewallResource();
    firewallResource.setId(resourceCollectionId.getResourceId(firewall.getName()));
    firewallResource.setDescription(firewall.getDescription());
    if (firewall.getNetwork() != null) {
      ResourceId projectId = resourceCollectionId.getParent();
      if (projectId == null || projectId.getResourceType() != ResourceType.PROJECT) {
        throw new IllegalStateException("Parent should be a project not " + projectId);
      }
      String networkName = parseName(apiProjectId, firewall.getNetwork());
      firewallResource.setNetwork(
          projectId.getChildCollection(ResourceType.NETWORK).getResourceId(networkName));
    }
    if (firewall.getCreationTimestamp() != null) {
      firewallResource.setCreatedAt(WebUtils.parse8601(firewall.getCreationTimestamp()));
    }
    firewallResource.setSourceRanges(firewall.getSourceRanges());
    firewallResource.setSourceTags(firewall.getSourceTags());
    firewallResource.setTargetTags(firewall.getTargetTags());
    firewallResource.setAllowed(Lists.transform(firewall.getAllowed(), FROM_ALLOWED));
    return firewallResource;
  }

  private static String parseName(String apiProjectId, String resourceUrl) {
    ResourceName resourceName = ResourceName.parseResource(resourceUrl);
    checkState(resourceName.getApiProjectId().equals(apiProjectId),
        "Resource URL %s had API project ID %s, expected %s",
        resourceUrl,
        resourceName.getApiProjectId(),
        apiProjectId);
    return resourceName.getResourceName();
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Allowed {

    private String protocol;
    private List<String> ports;

    private boolean hasProtocol;
    private boolean hasPorts;

    public String getProtocol() {
      return protocol;
    }

    public void setProtocol(String protocol) {
      this.protocol = protocol;
      hasProtocol = true;
    }

    public void clearProtocol() {
      protocol = null;
      hasProtocol = false;
    }

    public boolean hasProtocol() {
      return hasProtocol;
    }

    public List<String> getPorts() {
      return ports;
    }

    public void setPorts(List<String> ports) {
      this.ports = ports;
      hasPorts = true;
    }

    public void clearPorts() {
      ports = null;
      hasPorts = false;
    }

    public boolean hasPorts() {
      return hasPorts;
    }

    @Override public String toString() {
      return MoreObjects.toStringHelper(this).omitNullValues()
          .add("protocol", protocol)
          .add("ports", ports)
          .toString();
    }
  }
}
