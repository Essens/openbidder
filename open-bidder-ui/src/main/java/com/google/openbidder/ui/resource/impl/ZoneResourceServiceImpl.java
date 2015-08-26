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

package com.google.openbidder.ui.resource.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.api.services.adexchangebuyer.model.Account;
import com.google.api.services.compute.model.Instance;
import com.google.api.services.compute.model.Zone;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.openbidder.ui.adexchangebuyer.AdExchangeBuyerClient;
import com.google.openbidder.ui.adexchangebuyer.AdExchangeBuyerService;
import com.google.openbidder.ui.adexchangebuyer.exception.AdExchangeBuyerException;
import com.google.openbidder.ui.compute.ComputeClient;
import com.google.openbidder.ui.compute.ComputeService;
import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.project.ProjectService;
import com.google.openbidder.ui.project.ProjectUser;
import com.google.openbidder.ui.project.exception.NoProjectWriteAccessException;
import com.google.openbidder.ui.resource.ZoneResourceService;
import com.google.openbidder.ui.resource.exception.BadRequestException;
import com.google.openbidder.ui.resource.exception.ZoneHostNameAlreadyExistsException;
import com.google.openbidder.ui.resource.model.InstanceResource;
import com.google.openbidder.ui.resource.model.ScheduledOutage;
import com.google.openbidder.ui.resource.model.ZoneResource;
import com.google.openbidder.ui.resource.support.AbstractComputeResourceService;
import com.google.openbidder.ui.resource.support.InstanceType;
import com.google.openbidder.ui.resource.support.ResourceCollectionId;
import com.google.openbidder.ui.resource.support.ResourceId;
import com.google.openbidder.ui.resource.support.ResourceMethod;
import com.google.openbidder.ui.resource.support.ResourceType;
import com.google.openbidder.ui.util.EnumCounter;
import com.google.openbidder.ui.util.RegionMatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import javax.inject.Inject;

/**
 * A {@link com.google.openbidder.ui.resource.ResourceService} for
 * {@link com.google.openbidder.ui.resource.model.ZoneResource}s.
 */
public class ZoneResourceServiceImpl
    extends AbstractComputeResourceService<ZoneResource>
    implements ZoneResourceService {

  private static final Logger logger = LoggerFactory.getLogger(ZoneResourceServiceImpl.class);

  private static final String SUMMARY = "summary";
  private static final String INSTANCES = "instances";

  private final AdExchangeBuyerService adExchangeBuyerService;

  @Inject
  public ZoneResourceServiceImpl(
      ProjectService projectService,
      ComputeService computeService,
      AdExchangeBuyerService adExchangeBuyerService) {

    super(ResourceType.ZONE,
        EnumSet.of(ResourceMethod.GET, ResourceMethod.LIST, ResourceMethod.UPDATE),
        projectService,
        computeService);
    this.adExchangeBuyerService = adExchangeBuyerService;
  }

  /**
   * Valid parameters:
   * <dl>
   *   <dt>instances</dt>
   *   <dd>include all instances in this zone</dd>
   *   <dt>summary</dt>
   *   <dd>include a count of instance types and total instances in this zone</dd>
   * </dl>
   */
  @Override
  protected ZoneResource get(
      ComputeClient computeClient,
      final ProjectUser projectUser,
      final ResourceId zoneResourceId,
      Multimap<String, String> params) {

    Project project = projectUser.getProject();
    Zone zone = computeClient.getZone(zoneResourceId.getResourceName());

    String requestUrl = buildRequestUrl(
        project.getZoneHost(zone.getName()), project.getLoadBalancerRequestPort());
    Account.BidderLocation bidderLocation = requestUrl == null
        ? null
        : findBidderLocation(getBidderLocations(projectUser), requestUrl, zone.getName());

    List<Instance> instances;
    if (!Strings.isNullOrEmpty(project.getNetworkName())) {
      if (params.containsKey(INSTANCES)) {
        instances = computeClient.listInstances(project.getNetworkName(), zone.getName());
      } else if (params.containsKey(SUMMARY)) {
        instances = computeClient.listInstanceSummary(project.getNetworkName(), zone.getName());
      } else {
        instances = null;
      }
    } else {
      instances = null;
    }

    List<InstanceResource> zoneInstanceResources = instances == null
        ? null
        : Lists.transform(instances,
            instanceConverter(project, zoneResourceId));
    return build(
        project,
        zone,
        zoneResourceId,
        zoneInstanceResources,
        params.containsKey(SUMMARY),
        params.containsKey(INSTANCES),
        bidderLocation != null,
        bidderLocation == null ? null : bidderLocation.getMaximumQps());
  }

  /**
   * Valid parameters:
   * <dl>
   *   <dt>instances</dt>
   *   <dd>include all instances in each zone</dd>
   *   <dt>summary</dt>
   *   <dd>include a count of instance types and total instances in each zone</dd>
   * </dl>
   */
  @Override
  protected List<ZoneResource> list(
      final ComputeClient computeClient,
      final ProjectUser projectUser,
      final ResourceCollectionId resourceCollectionId,
      final Multimap<String, String> params) {

    final Project project = projectUser.getProject();
    final List<Account.BidderLocation> bidderLocations = getBidderLocations(projectUser);

    return Lists.transform(computeClient.listZones(), new Function<Zone, ZoneResource>() {
      @Override public ZoneResource apply(Zone zone) {
        ResourceId zoneResourceId = resourceCollectionId.getResourceId(zone.getName());

        final List<Instance> instances;
        if (!Strings.isNullOrEmpty(project.getNetworkName())) {
          if (params.containsKey(INSTANCES)) {
            instances = computeClient.listInstances(project.getNetworkName(), zone.getName());
          } else if (params.containsKey(SUMMARY)) {
            instances = computeClient.listInstanceSummary(
                project.getNetworkName(), zone.getName());
          } else {
            instances = new ArrayList<>();
          }
        } else {
          instances = new ArrayList<>();
        }
        ResourceId projectResourceId = zoneResourceId.getParentResource();
        if (projectResourceId == null
            || projectResourceId.getResourceType() != ResourceType.PROJECT) {
          throw new IllegalStateException("parent not a project: " + projectResourceId);
        }
        List<InstanceResource> zoneInstanceResources = instances == null
            ? null
            : Lists.transform(instances,
            instanceConverter(projectUser.getProject(), zoneResourceId));

        String requestUrl = buildRequestUrl(
            project.getZoneHost(zone.getName()), project.getLoadBalancerRequestPort());
        Account.BidderLocation bidderLocation = requestUrl == null
            ? null
            : findBidderLocation(bidderLocations, requestUrl, zone.getName());

        return build(
            project,
            zone,
            zoneResourceId,
            zoneInstanceResources,
            params.containsKey(SUMMARY),
            params.containsKey(INSTANCES),
            bidderLocation != null,
            bidderLocation == null ? null : bidderLocation.getMaximumQps());
      }
    });
  }

  @Override
  protected ZoneResource update(
      ComputeClient computeClient,
      ProjectUser projectUser,
      ResourceId zoneResourceId,
      final ZoneResource updatedZoneResource) {

    if (!projectUser.getUserRole().getProjectRole().isWrite()) {
      throw new NoProjectWriteAccessException(
          projectUser.getProject().getId(),
          projectUser.getEmail());
    }

    ZoneResource zoneResource = get(
        computeClient,
        projectUser,
        zoneResourceId,
        ArrayListMultimap.<String, String>create());
    final String zoneName = zoneResourceId.getResourceName();

    if (updatedZoneResource.hasHostName()) {
      final String hostName = updatedZoneResource.getHostName();
      if (projectUser.getProject().getZoneHosts().containsKey(hostName)) {
        throw new ZoneHostNameAlreadyExistsException(
            hostName, projectUser.getProject().getZoneHosts().get(hostName));
      }

      Project updatedProject = getProjectService().updateProject(projectUser.getProject().getId(),
          new Function<Project, Project>() {
            @Override public Project apply(Project project) {
              project.setZoneHost(zoneName, hostName);
              return project;
            }});
      zoneResource.setHostName(updatedProject.getZoneHost(zoneName));
    }

    return zoneResource;
  }

  private Function<Instance, InstanceResource> instanceConverter(
      final Project project,
      final ResourceId zoneResourceId) {

    return new Function<Instance, InstanceResource>() {
      @Override public InstanceResource apply(Instance instance) {
        return InstanceResource.build(
            project,
            zoneResourceId.getChildCollection(ResourceType.INSTANCE),
            instance);
      }};
  }

  private static ZoneResource build(
      Project project,
      Zone zone,
      ResourceId zoneResourceId,
      Collection<InstanceResource> zoneInstanceResources,
      boolean includeSummary,
      boolean includeInstances,
      boolean isRegistered,
      Integer maxBidRequestQps) {

    ZoneResource zoneResource = new ZoneResource();
    zoneResource.setId(zoneResourceId);
    zoneResource.setDescription(zone.getDescription());
    zoneResource.setStatus(zone.getStatus());
    List<Zone.MaintenanceWindows> maintenanceWindows = zone.getMaintenanceWindows();
    if (maintenanceWindows == null) {
      zoneResource.setScheduledOutages(null);
    } else {
      zoneResource.setScheduledOutages(
          Lists.transform(maintenanceWindows, ScheduledOutage.FROM_MAINTENANCE_WINDOW));
    }
    zoneResource.setHostName(project.getZoneHost(zoneResource.getResourceName()));
    // TODO(wshields): available zone machine types once Compute starts populating the field
    if (includeInstances) {
      zoneResource.setInstances(ImmutableList.copyOf(zoneInstanceResources));
    }
    if (includeSummary) {
      EnumCounter<InstanceType> counter = EnumCounter.newInstance(InstanceType.class);
      for (InstanceResource instanceResource : zoneInstanceResources) {
        counter.increment(instanceResource.getInstanceType());
      }
      zoneResource.setInstanceSummary(counter.toMap());
      zoneResource.setInstanceCount(counter.getTotal());
    }
    zoneResource.setIsRegistered(isRegistered);
    zoneResource.setMaxBidRequestQps(maxBidRequestQps);
    return zoneResource;
  }

  @Override
  public ZoneResource register(
      String projectId,
      ZoneResource zoneResource){

    checkNotNull(projectId);
    ProjectUser projectUser = getParent(projectId);
    AdExchangeBuyerClient adExchangeBuyerClient = adExchangeBuyerService.connect(
        projectUser);

    return register(adExchangeBuyerClient, projectUser, zoneResource);
  }


  @Override
  public ZoneResource unregister(String projectId, String zoneId) {
    checkNotNull(projectId);
    checkNotNull(zoneId);
    ResourceId zoneResourceId = ResourceType.ZONE.getResourceId(projectId, zoneId);
    ProjectUser projectUser = getParent(projectId);
    AdExchangeBuyerClient adExchangeBuyerClient = adExchangeBuyerService.connect(
        projectUser);
    ZoneResource zoneResource = get(
        getService(projectUser),
        projectUser,
        zoneResourceId,
        HashMultimap.<String, String>create());
    return unregister(adExchangeBuyerClient, projectUser, zoneResource);
  }

  private ZoneResource register(
      AdExchangeBuyerClient adExchangeBuyerClient,
      ProjectUser projectUser,
      ZoneResource zoneResource) {

    if (!projectUser.getUserRole().getProjectRole().isWrite()) {
      throw new NoProjectWriteAccessException(
          projectUser.getProject().getId(),
          projectUser.getEmail());
    }

    Project project = projectUser.getProject();
    Integer maxBidRequestQps = zoneResource.getMaxBidRequestQps();
    if (Strings.isNullOrEmpty(zoneResource.getHostName()) || maxBidRequestQps == null) {
      throw new BadRequestException("Missing parameter for zone registration request");
    }
    String accountId = projectUser.getProject().getAdExchangeBuyerAccountId();
    Account account = adExchangeBuyerClient.getAccount(accountId);

    String requestUrl = buildRequestUrl(
        zoneResource.getHostName(), project.getLoadBalancerRequestPort());
    if (requestUrl != null) {
      List<Account.BidderLocation> bidderLocations = addBidderLocation(
          adExchangeBuyerClient.getBidderLocations(accountId),
          requestUrl,
          zoneResource.getResourceName(),
          maxBidRequestQps);
      adExchangeBuyerClient.updateAccount(accountId, account.setBidderLocation(bidderLocations));
    }

    zoneResource.setIsRegistered(true);
    zoneResource.setMaxBidRequestQps(maxBidRequestQps);
    return zoneResource;
  }

  private ZoneResource unregister(
      AdExchangeBuyerClient adExchangeBuyerClient,
      ProjectUser projectUser,
      ZoneResource zoneResource){

    if (!projectUser.getUserRole().getProjectRole().isWrite()) {
      throw new NoProjectWriteAccessException(
          projectUser.getProject().getId(),
          projectUser.getEmail());
    }

    if (Strings.isNullOrEmpty(zoneResource.getHostName())) {
      throw new BadRequestException("Missing parameter for zone unregistration request");
    }

    String accountId = projectUser.getProject().getAdExchangeBuyerAccountId();
    String requestUrl = buildRequestUrl(
        zoneResource.getHostName(), projectUser.getProject().getLoadBalancerRequestPort());
    if (requestUrl != null) {
      Account account = adExchangeBuyerClient.getAccount(accountId);
      ImmutableList<Account.BidderLocation> bidderLocations = removeBidderLocation(
          adExchangeBuyerClient.getBidderLocations(accountId),
          requestUrl,
          RegionMatcher.mapGceToDoubleClickRegion(zoneResource.getDescription()));
      adExchangeBuyerClient.updateAccount(accountId, account.setBidderLocation(bidderLocations));
    }

    zoneResource.setIsRegistered(false);
    zoneResource.setMaxBidRequestQps(null);
    return zoneResource;
  }

  private List<Account.BidderLocation> addBidderLocation(
      List<Account.BidderLocation> bidderLocations,
      String requestUrl,
      String zoneName,
      Integer maxBidRequestQps) {
    checkNotNull(requestUrl);

    Account.BidderLocation bidderLocation = findBidderLocation(
        bidderLocations, requestUrl, zoneName);

    if (bidderLocation == null) {
      Account.BidderLocation newBidderLocation = new Account.BidderLocation();
      newBidderLocation.setUrl(requestUrl);
      newBidderLocation.setMaximumQps(maxBidRequestQps);
      newBidderLocation.setRegion(RegionMatcher.mapGceToDoubleClickRegion(zoneName));
      bidderLocations.add(newBidderLocation);
    }

    return bidderLocations;
  }

  private ImmutableList<Account.BidderLocation> removeBidderLocation(
      List<Account.BidderLocation> bidderLocations,
      final String requestUrl,
      final String region) {
    checkNotNull(requestUrl);

    return ImmutableList.copyOf(Iterables.filter(
        bidderLocations,
        new Predicate<Account.BidderLocation>() {
          @Override public boolean apply(Account.BidderLocation bidderLocation) {
            return !bidderLocation.getUrl().equals(requestUrl)
                || !bidderLocation.getRegion().equals(region);
          }
        }));
  }

  private Account.BidderLocation findBidderLocation(
      List<Account.BidderLocation> bidderLocations,
      final String requestUrl,
      final String zoneName) {

    checkNotNull(requestUrl);
    final String region = RegionMatcher.mapGceToDoubleClickRegion(zoneName);

    return Iterables.find(
        bidderLocations,
        new Predicate<Account.BidderLocation>() {
          @Override public boolean apply(Account.BidderLocation bidderLocation) {
            return bidderLocation.getUrl().equals(requestUrl)
                && bidderLocation.getRegion().equals(region);
          }},
        /* default value */ null);
  }

  /**
   * Get bidder location list per project. Returns an empty list if no Ad Exchange account
   * has been set, or the account lacks privileges.
   */
  private List<Account.BidderLocation> getBidderLocations(ProjectUser projectUser) {
    try {
      String accountId = projectUser.getProject().getAdExchangeBuyerAccountId();
      if (accountId != null) {
        AdExchangeBuyerClient adExchangeBuyerClient = adExchangeBuyerService.connect(
            projectUser);
        return adExchangeBuyerClient.getBidderLocations(accountId);
      }
    } catch (AdExchangeBuyerException e) {
      logger.warn(e.getMessage());
    }

    return new ArrayList<>();
  }

  private String buildRequestUrl(String hostName, String port) {
    return hostName == null
        ? null
        : port == null
            ? "http://" + hostName + "/bid_request/doubleclick"
            : "http://" + hostName + ":" + port + "/bid_request/doubleclick";
  }
}
