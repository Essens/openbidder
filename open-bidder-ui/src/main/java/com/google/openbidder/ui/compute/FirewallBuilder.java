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

import static java.util.Arrays.asList;

import com.google.api.services.compute.model.Firewall;
import com.google.api.services.compute.model.Firewall.Allowed;
import com.google.api.services.compute.model.Network;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.openbidder.ui.entity.Project;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Constructs {@link Firewall}s for the Open Bidder.
 */
public class FirewallBuilder {
  private static final String FIREWALL_NAME = "firewall-%s-%s";
  private static final String SSH_PORT = "22";

  /**
   * @return {@link Map} of firewall names to {@link Firewall}s. If the firewall value is
   * {@code null} then the firewalls hould be deleted.
   */
  public Map<String, Firewall> build(Project project, Network network) {
    Map<String, Firewall> firewalls = new HashMap<>();
    boolean hasWhitelist = !project.getWhiteListedIpRanges().isEmpty();

    // Cleanup firewalls from old versions (<0.5.4). Remove this later.
    firewalls.put(buildFirewallName(project, "lb-bidders"), null);

    if (Objects.equal(project.getBidderRequestPort(), project.getLoadBalancerRequestPort())) {
      Firewall firewall = new Firewall();
      firewall.setName(buildFirewallName(project, "world-lb-bidders"));
      firewall.setDescription("World to load balancers or bidders");
      firewall.setNetwork(network.getSelfLink());
      firewall.setSourceRanges(asList("0.0.0.0/0"));
      firewall.setTargetTags(asList(
          LoadBalancerInstanceBuilder.TAG, BidderInstanceBuilder.TAG));
      firewall.setAllowed(allowTcpPorts(project.getBidderRequestPort()));
      firewalls.put(firewall.getName(), firewall);

      firewalls.put(buildFirewallName(project, "world-lb"), null);
      firewalls.put(buildFirewallName(project, "world-bidders"), null);
    } else {
      Firewall firewall = new Firewall();
      firewall.setName(buildFirewallName(project, "world-lb"));
      firewall.setDescription("World to load balancers");
      firewall.setNetwork(network.getSelfLink());
      firewall.setSourceRanges(asList("0.0.0.0/0"));
      firewall.setTargetTags(asList(LoadBalancerInstanceBuilder.TAG));
      firewall.setAllowed(allowTcpPorts(project.getLoadBalancerRequestPort()));
      firewalls.put(firewall.getName(), firewall);

      firewall = new Firewall();
      firewall.setName(buildFirewallName(project, "world-bidders"));
      firewall.setDescription("World to bidders");
      firewall.setNetwork(network.getSelfLink());
      firewall.setSourceRanges(asList("0.0.0.0/0"));
      firewall.setTargetTags(asList(BidderInstanceBuilder.TAG));
      firewall.setAllowed(allowTcpPorts(project.getBidderRequestPort()));
      firewalls.put(firewall.getName(), firewall);

      firewalls.put(buildFirewallName(project, "world-lb-bidders"), null);
    }

    if (Objects.equal(project.getBidderAdminPort(), project.getLoadBalancerStatPort())) {
      String name = buildFirewallName(project, "wl-lb-bidders");
      if (hasWhitelist) {
        Firewall firewall = new Firewall();
        firewall.setName(name);
        firewall.setDescription("Whitelisted IPs to load balancers or bidders (admin)");
        firewall.setNetwork(network.getSelfLink());
        firewall.setSourceRanges(project.getWhiteListedIpRanges());
        firewall.setTargetTags(asList(
            LoadBalancerInstanceBuilder.TAG, BidderInstanceBuilder.TAG));
        firewall.setAllowed(allowTcpPorts(
            project.getBidderAdminPort(),
            SSH_PORT));
        firewalls.put(name, firewall);
      } else {
        firewalls.put(name, null);
      }

      firewalls.put(buildFirewallName(project, "wl-lb"), null);
      firewalls.put(buildFirewallName(project, "wl-bidders"), null);
    } else {
      String name = buildFirewallName(project, "wl-bidders");
      if (hasWhitelist) {
        Firewall firewall = new Firewall();
        firewall.setName(name);
        firewall.setDescription("Whitelisted IPs to bidders (admin)");
        firewall.setNetwork(network.getSelfLink());
        firewall.setSourceRanges(project.getWhiteListedIpRanges());
        firewall.setTargetTags(asList(BidderInstanceBuilder.TAG));
        firewall.setAllowed(allowTcpPorts(
            project.getBidderAdminPort(),
            SSH_PORT));
        firewalls.put(name, firewall);
      } else {
        firewalls.put(name, null);
      }

      name = buildFirewallName(project, "wl-lb");
      if (hasWhitelist) {
        Firewall firewall = new Firewall();
        firewall.setName(name);
        firewall.setDescription("Whitelisted IPs to load balancers (admin)");
        firewall.setNetwork(network.getSelfLink());
        firewall.setSourceRanges(project.getWhiteListedIpRanges());
        firewall.setTargetTags(asList(LoadBalancerInstanceBuilder.TAG));
        firewall.setAllowed(allowTcpPorts(
            project.getLoadBalancerStatPort(),
            SSH_PORT));
        firewalls.put(name, firewall);
      } else {
        firewalls.put(name, null);
      }

      firewalls.put(buildFirewallName(project, "wl-lb-bidders"), null);
    }

    return firewalls;
  }

  private String buildFirewallName(Project project, String name) {
    return String.format(FIREWALL_NAME, project.getProjectUuid(), name);
  }

  private ImmutableList<Allowed> allowTcpPorts(String... ports) {
    return allowTcpPorts(asList(ports));
  }

  private ImmutableList<Allowed> allowTcpPorts(List<String> ports) {
    return ImmutableList.of(new Allowed()
        .setIPProtocol("tcp")
        .setPorts(ports));
  }
}
