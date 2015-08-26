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

package com.google.openbidder.ui.resource;

import static java.util.Arrays.asList;

import com.google.api.services.compute.model.Firewall;
import com.google.api.services.compute.model.Image;
import com.google.api.services.compute.model.Instance;
import com.google.api.services.compute.model.MachineType;
import com.google.api.services.compute.model.Network;
import com.google.api.services.compute.model.Quota;
import com.google.api.services.compute.model.Region;
import com.google.api.services.compute.model.Zone;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.entity.support.ProjectRole;
import com.google.openbidder.ui.resource.model.ReportResource;

import java.util.Collection;

/**
 * Methods intended for static import to match JSON results to the underlying entity.
 */
public class ResourceMatchers {

  private static final Function<Project, ProjectJsonMatcher> PROJECT_MATCHER =
      new Function<Project, ProjectJsonMatcher>() {
        @Override public ProjectJsonMatcher apply(Project project) {
          return project(project);
        }};

  public static ProjectJsonMatcher project(Project project) {
    return new ProjectJsonMatcher(project);
  }

  public static ProjectJsonMatcher project(Project project, boolean networkExists) {
    return new ProjectJsonMatcher(project, networkExists);
  }

  public static Collection<ProjectJsonMatcher> projects(Project... projects) {
    return projects(asList(projects));
  }

  public static Collection<ProjectJsonMatcher> projects(Collection<Project> projects) {
    return Collections2.transform(projects, PROJECT_MATCHER);
  }

  public static UserJsonMatcher user(long projectId, String email, ProjectRole projectRole) {
    return new UserJsonMatcher(projectId, email, projectRole);
  }

  public static ReportJsonMatcher report(long projectId, ReportResource reportResource) {
    return new ReportJsonMatcher(projectId, reportResource);
  }

  public static Collection<ReportJsonMatcher> reports(
      long projectId, ReportResource... reportResources) {
    return reports(projectId, asList(reportResources));
  }

  public static Collection<ReportJsonMatcher> reports(
      final long projectId,
      Collection<ReportResource> reportResources) {

    return Collections2.transform(
        reportResources,
        new Function<ReportResource, ReportJsonMatcher>() {
          @Override public ReportJsonMatcher apply(ReportResource reportResource) {
            return new ReportJsonMatcher(projectId, reportResource);
          }});
  }

  public static QuotaJsonMatcher quota(long projectId, Quota quota) {
    return new QuotaJsonMatcher(projectId, quota);
  }

  public static Collection<QuotaJsonMatcher> quotas(long projectId, Quota... quotas) {
    return quotas(projectId, asList(quotas));
  }

  public static Collection<QuotaJsonMatcher> quotas(
      final long projectId,
      Collection<Quota> quotas) {

    return Collections2.transform(quotas, new Function<Quota, QuotaJsonMatcher>() {
      @Override public QuotaJsonMatcher apply(Quota quota) {
        return new QuotaJsonMatcher(projectId, quota);
      }});
  }

  public static ZoneJsonMatcher zone(long projectId, Zone zone) {
    return new ZoneJsonMatcher(projectId, zone);
  }

  public static ZoneJsonMatcher zone(long projectId, Zone zone, String zoneHost) {
    return new ZoneJsonMatcher(projectId, zone, zoneHost);
  }

  public static Collection<ZoneJsonMatcher> zones(long projectId, Zone... zones) {
    return zones(projectId, asList(zones));
  }

  public static Collection<ZoneJsonMatcher> zones(
      final long projectId,
      Collection<Zone> zones) {

    return Collections2.transform(zones, new Function<Zone, ZoneJsonMatcher>() {
      @Override public ZoneJsonMatcher apply(Zone zone) {
        return new ZoneJsonMatcher(projectId, zone);
      }});
  }

  public static NetworkJsonMatcher network(long projectId, Network network) {
    return new NetworkJsonMatcher(projectId, network);
  }

  public static Collection<NetworkJsonMatcher> networks(long projectId,
      Network... networks) {
    return networks(projectId, asList(networks));
  }

  public static Collection<NetworkJsonMatcher> networks(
      final long projectId,
      Collection<Network> networks) {

    return Collections2.transform(networks, new Function<Network, NetworkJsonMatcher>() {
      @Override public NetworkJsonMatcher apply(Network network) {
        return new NetworkJsonMatcher(projectId, network);
      }});
  }

  public static FirewallJsonMatcher firewall(Project project, Firewall firewall) {
    return new FirewallJsonMatcher(project, firewall);
  }

  public static Collection<FirewallJsonMatcher> firewalls(
      Project project,
      Firewall... firewalls) {

    return firewalls(project, asList(firewalls));
  }

  public static Collection<FirewallJsonMatcher> firewalls(
      final Project project,
      Collection<Firewall> firewalls) {

    return Collections2.transform(firewalls, new Function<Firewall, FirewallJsonMatcher>() {
      @Override public FirewallJsonMatcher apply(Firewall firewall) {
        return new FirewallJsonMatcher(project, firewall);
      }});
  }

  public static MachineTypeJsonMatcher machineType(
      long projectId,
      Zone zone,
      MachineType machineType) {

    return new MachineTypeJsonMatcher(projectId, zone, machineType);
  }

  public static Collection<MachineTypeJsonMatcher> machineTypes(
      long projectId,
      Zone zone,
      MachineType... machineTypes) {

    return machineTypes(projectId, zone, asList(machineTypes));
  }

  public static Collection<MachineTypeJsonMatcher> machineTypes(
      final long projectId,
      final Zone zone,
      Collection<MachineType> machineTypes) {

    return Collections2.transform(
        machineTypes,
        new Function<MachineType, MachineTypeJsonMatcher>() {
          @Override public MachineTypeJsonMatcher apply(MachineType machineType) {
            return new MachineTypeJsonMatcher(projectId, zone, machineType);
          }});
  }

  public static ImageJsonMatcher image(
      long projectId,
      Image image) {

    return new ImageJsonMatcher(projectId, image);
  }

  public static Collection<ImageJsonMatcher> images(
      long projectId,
      Image... images) {

    return images(projectId, asList(images));
  }

  public static Collection<ImageJsonMatcher> images(
      final long projectId,
      Collection<Image> images) {

    return Collections2.transform(images, new Function<Image, ImageJsonMatcher>() {
      @Override public ImageJsonMatcher apply(Image image) {
        return new ImageJsonMatcher(projectId, image);
      }});
  }

  public static RegionJsonMatcher region(long projectId, Region region) {

    return new RegionJsonMatcher(projectId, region);
  }

  public static Collection<RegionJsonMatcher> regions(
      long projectId,
      Region... regions) {

    return regions(projectId, asList(regions));
  }

  public static Collection<RegionJsonMatcher> regions(
      final long projectId,
      Collection<Region> regions) {

    return Collections2.transform(regions, new Function<Region, RegionJsonMatcher>() {
      @Override public RegionJsonMatcher apply(Region region) {
        return new RegionJsonMatcher(projectId, region);
      }});
  }

  public static InstanceJsonMatcher instance(
      Project project,
      Zone zone,
      Instance instance) {

    return new InstanceJsonMatcher(project, zone, instance);
  }

  public static Collection<InstanceJsonMatcher> instances(
      Project project,
      Zone zone,
      Instance... instances) {

    return instances(project, zone, asList(instances));
  }

  public static Collection<InstanceJsonMatcher> instances(
      final Project project,
      final Zone zone,
      Collection<Instance> instances) {

    return Collections2.transform(instances, new Function<Instance, InstanceJsonMatcher>() {
      @Override public InstanceJsonMatcher apply(Instance instance) {
        return new InstanceJsonMatcher(project, zone, instance);
      }});
  }
}
