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

import com.google.api.services.adexchangebuyer.model.Account;
import com.google.api.services.compute.model.Firewall;
import com.google.api.services.compute.model.Zone;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.openbidder.ui.adexchangebuyer.AdExchangeBuyerClient;
import com.google.openbidder.ui.adexchangebuyer.AdExchangeBuyerService;
import com.google.openbidder.ui.compute.BidderParameters;
import com.google.openbidder.ui.compute.ComputeClient;
import com.google.openbidder.ui.compute.ComputeService;
import com.google.openbidder.ui.compute.LoadBalancerParameters;
import com.google.openbidder.ui.compute.exception.ApiProjectNotFoundException;
import com.google.openbidder.ui.compute.exception.UnknownComputeException;
import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.entity.UserPreference;
import com.google.openbidder.ui.project.ProjectService;
import com.google.openbidder.ui.project.ProjectUser;
import com.google.openbidder.ui.project.exception.ProjectNotFoundException;
import com.google.openbidder.ui.resource.ProjectResourceService;
import com.google.openbidder.ui.resource.QuotaResourceService;
import com.google.openbidder.ui.resource.exception.NotProjectOwnerException;
import com.google.openbidder.ui.resource.exception.ProjectDeleteException;
import com.google.openbidder.ui.resource.model.DoubleClickProjectResource;
import com.google.openbidder.ui.resource.model.ProjectResource;
import com.google.openbidder.ui.resource.support.AbstractRootResourceService;
import com.google.openbidder.ui.resource.support.ResourceCollectionId;
import com.google.openbidder.ui.resource.support.ResourceId;
import com.google.openbidder.ui.resource.support.ResourceMethod;
import com.google.openbidder.ui.resource.support.ResourceType;
import com.google.openbidder.ui.user.AuthorizationService;
import com.google.openbidder.ui.user.UserIdService;
import com.google.openbidder.ui.user.exception.NoCredentialsForProjectException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import javax.inject.Inject;

/**
 * A {@link com.google.openbidder.ui.resource.ResourceService} for {@link ProjectResource}s.
 */
public class ProjectResourceServiceImpl
    extends AbstractRootResourceService<ProjectResource>
    implements ProjectResourceService {

  private static final Logger logger = LoggerFactory.getLogger(
      NetworkResourceServiceImpl.class);

  private final QuotaResourceService quotaResourceService;
  private final ProjectService projectService;
  private final AuthorizationService authorizationService;
  private final ComputeService computeService;
  private final UserIdService userIdService;
  private final AdExchangeBuyerService adExchangeBuyerService;
  private final BidderParameters bidderParameters;
  private final LoadBalancerParameters loadBalancerParameters;
  private final long computeTimeoutMillis;

  public static final String QUOTAS = "quotas";

  @Inject
  public ProjectResourceServiceImpl(
      QuotaResourceService quotaResourceService,
      ProjectService projectService,
      AuthorizationService authorizationService,
      ComputeService computeService,
      UserIdService userIdService,
      AdExchangeBuyerService adExchangeBuyerService,
      BidderParameters bidderParameters,
      LoadBalancerParameters loadBalancerParameters,
      @Value("${Management.Compute.TimeOutMs}") long computeTimeoutMillis) {

    super(ResourceType.PROJECT, EnumSet.allOf(ResourceMethod.class));
    this.quotaResourceService = Preconditions.checkNotNull(quotaResourceService);
    this.projectService = Preconditions.checkNotNull(projectService);
    this.authorizationService = Preconditions.checkNotNull(authorizationService);
    this.computeService = Preconditions.checkNotNull(computeService);
    this.userIdService = Preconditions.checkNotNull(userIdService);
    this.adExchangeBuyerService = Preconditions.checkNotNull(adExchangeBuyerService);
    this.bidderParameters = Preconditions.checkNotNull(bidderParameters);
    this.loadBalancerParameters = Preconditions.checkNotNull(loadBalancerParameters);
    this.computeTimeoutMillis = computeTimeoutMillis;
  }

  @Override
  public ProjectResource setAsDefault(String projectIdStr) {
    Preconditions.checkNotNull(projectIdStr);
    ResourceId projectResourceId = ResourceType.PROJECT.getResourceId(projectIdStr);

    ProjectUser projectUser = getProjectUser(projectResourceId);
    UserPreference userPreference = projectService.setAsDefaultProject(projectUser);
    projectUser = userIdService.buildProjectUser(projectUser.getProject(), userPreference);

    return build(projectUser, false);
  }

  @Override
  public void addDefaultBidderConfig(Project project) {
    project.setVmParameters(bidderParameters.getDefaultJvmParameters());
    project.setMainParameters(bidderParameters.getDefaultMainParameters());
    project.setBidInterceptors(bidderParameters.getDefaultBidInterceptors());
    project.setImpressionInterceptors(bidderParameters.getDefaultImpressionInterceptors());
    project.setClickInterceptors(bidderParameters.getDefaultClickInterceptors());
    project.setMatchInterceptors(bidderParameters.getDefaultMatchInterceptors());
    project.setBidderRequestPort(bidderParameters.getRequestPort());
    project.setBidderAdminPort(bidderParameters.getAdminPort());
    project.setLoadBalancerRequestPort(loadBalancerParameters.getRequestPort());
    project.setLoadBalancerStatPort(loadBalancerParameters.getStatPort());
    project.setCookieMatchUrl(bidderParameters.getDcMatchUrl());
  }

  @Override
  public boolean isAuthorized(String projectId) {
    ResourceId projectResourceId = ResourceType.PROJECT.getResourceId(projectId);
    ProjectUser projectUser = getProjectUser(projectResourceId);
    return authorizationService.isAuthorized(projectUser);
  }

  @Override
  public ProjectResource revokeAuthorization(String projectIdStr) {
    Preconditions.checkNotNull(projectIdStr);
    ResourceId projectResourceId = ResourceType.PROJECT.getResourceId(projectIdStr);

    ProjectUser projectUser = getProjectUser(projectResourceId);
    return build(authorizationService.revokeAuthorization(projectUser), false);
  }

  @Override
  protected ProjectResource get(
      ResourceId projectResourceId,
      Multimap<String, String> params) {

    return build(getProjectUser(projectResourceId), params.containsKey(QUOTAS));
  }

  @Override
  protected List<ProjectResource> list(
      ResourceCollectionId projectCollection,
      Multimap<String, String> params) {

    List<ProjectUser> allProjectsForUser = projectService.getAllProjectsForUser();
    final boolean includeQuotas = params.containsKey(QUOTAS);
    return Lists.transform(allProjectsForUser, new Function<ProjectUser, ProjectResource>() {
      @Override
      public ProjectResource apply(ProjectUser projectUser) {
        return build(projectUser, includeQuotas);
      }
    });
  }

  @Override
  protected ProjectResource create(
      ResourceCollectionId resourceCollectionId,
      ProjectResource newProject) {

    Project project = new Project();
    populateFromJson(project, newProject, true);
    ProjectUser projectUser = projectService.insertProject(project);
    updateBuyerAccount(projectUser, newProject);
    return build(projectUser, false);
  }

  @Override
  protected ProjectResource update(
      ResourceId projectResourceId,
      final ProjectResource updatedProject) {

    Preconditions.checkNotNull(projectResourceId);
    Preconditions.checkArgument(ResourceType.PROJECT == projectResourceId.getResourceType(),
        "Expected resourceId of type project, found %s", projectResourceId.getResourceType());

    ProjectUser projectUser = getProjectUser(projectResourceId);
    long projectId = projectUser.getProject().getId();
    Project modifiedProject = projectService.updateProject(projectId,
        new Function<Project, Project>() {
          @Override
          public Project apply(Project project) {
            populateFromJson(project, updatedProject, false);
            return project;
          }
        });
    ProjectUser updatedProjectUser = projectUser.updateProject(modifiedProject);
    updateBuyerAccount(updatedProjectUser, updatedProject);
    return build(projectUser.updateProject(modifiedProject), false);
  }

  @Override
  protected void delete(ResourceId projectResourceId) {
    Preconditions.checkNotNull(projectResourceId);
    Preconditions.checkArgument(ResourceType.PROJECT == projectResourceId.getResourceType(),
        "Expected resourceId of type project, found %s", projectResourceId.getResourceType());

    ProjectUser projectUser = getProjectUser(projectResourceId);
    long projectId = projectUser.getProject().getId();
    if (!projectUser.getUserRole().getProjectRole().isOwner()) {
      throw new NotProjectOwnerException(projectId);
    }

    if (instanceExists(projectUser)) {
      logger.info("Attempted to delete project {}, but still has running instances",
          projectResourceId.getResourceName());
      throw new ProjectDeleteException(
          "Please terminate all of this project's instances before deletion.", projectId);
    }

    deleteFirewallsAndNetwork(projectUser);

    projectService.deleteProject(projectId);
  }

  private boolean instanceExists(ProjectUser projectUser) {

    String networkName = projectUser.getProject().getNetworkName();
    if (Strings.isNullOrEmpty(networkName)) {
      return false;
    }
    ComputeClient computeClient = computeService.connect(projectUser);
    List<Zone> zones = computeClient.listZones();
    for (Zone zone : zones) {
      if (computeClient.listInstances(networkName, zone.getName()).size() > 0) {
        return true;
      }
    }
    return false;
  }

  private void deleteFirewallsAndNetwork(ProjectUser projectUser) {
    String networkName = projectUser.getProject().getNetworkName();
    if (networkName == null) {
      return;
    }

    ComputeClient computeClient = computeService.connect(projectUser);
    Collection<Firewall> firewallsForNetwork = computeClient.listFirewallsForNetwork(networkName);
    //TODO(jnwang): support async deletion.
    for (Firewall firewall : firewallsForNetwork) {
      computeClient.deleteFirewallAndWait(firewall.getName(), computeTimeoutMillis);
    }
    computeClient.deleteNetworkAndWait(networkName, computeTimeoutMillis);
  }

  private void populateFromJson(
      Project project,
      ProjectResource projectResource,
      boolean newProject) {

    if (newProject) {
      addDefaultBidderConfig(project);
      if (projectResource.hasApiProjectId()) {
        project.setApiProjectId(projectResource.getApiProjectId());
      }
    }
    if (projectResource.hasApiProjectNumber()) {
      project.setApiProjectNumber(projectResource.getApiProjectNumber());
    }
    if (projectResource.hasDescription()) {
      project.setProjectName(projectResource.getDescription());
    }
    if (projectResource.hasOauth2ClientId()) {
      project.setOauth2ClientId(projectResource.getOauth2ClientId());
    }
    if (projectResource.hasOauth2ClientSecret()) {
      project.setOauth2ClientSecret(projectResource.getOauth2ClientSecret());
    }
    if (projectResource.hasAdExchangeBuyerAccount()) {
      if (projectResource.getAdExchangeBuyerAccount() == null) {
        project.setAdExchangeBuyerAccountId(null);
      } else {
        project.setAdExchangeBuyerAccountId(
            projectResource.getAdExchangeBuyerAccount().getResourceName());
      }
    }
    if (projectResource.hasVmParameters()) {
      project.setVmParameters(projectResource.getVmParameters());
    }
    if (projectResource.hasMainParameters()) {
      project.setMainParameters(projectResource.getMainParameters());
    }
    if (projectResource.hasBidInterceptors()) {
      project.setBidInterceptors(projectResource.getBidInterceptors());
    }
    if (projectResource.hasImpressionInterceptors()) {
      project.setImpressionInterceptors(projectResource.getImpressionInterceptors());
    }
    if (projectResource.hasClickInterceptors()) {
      project.setClickInterceptors(projectResource.getClickInterceptors());
    }
    if (projectResource.hasMatchInterceptors()) {
      project.setMatchInterceptors(projectResource.getMatchInterceptors());
    }
    if (projectResource.hasCookieMatchUrl()) {
      project.setCookieMatchUrl(projectResource.getCookieMatchUrl());
    }
    if (projectResource.hasCookieMatchNid()) {
      project.setCookieMatchNid(projectResource.getCookieMatchNid());
    }
    if (projectResource.hasUserDistUri()) {
      project.setUserDistUri(projectResource.getUserDistUri());
    }
    if (projectResource.hasBidderRequestPort()) {
      project.setBidderRequestPort(projectResource.getBidderRequestPort());
    }
    if (projectResource.hasBidderAdminPort()) {
      project.setBidderAdminPort(projectResource.getBidderAdminPort());
    }
    if (projectResource.hasLoadBalancerRequestPort()) {
      project.setLoadBalancerRequestPort(projectResource.getLoadBalancerRequestPort());
    }
    if (projectResource.hasLoadBalancerStatPort()) {
      project.setLoadBalancerStatPort(projectResource.getLoadBalancerStatPort());
    }
    if (projectResource.hasBidderOauth2Scopes()) {
      project.setBidderOauth2Scopes(projectResource.getBidderOauth2Scopes());
    }
    if (projectResource.hasLoadBalancerOauth2Scopes()) {
      project.setLoadBalancerOauth2Scopes(projectResource.getLoadBalancerOauth2Scopes());
    }
    if (projectResource.hasDoubleClickPreferredDealsBucket()) {
      project.setDoubleClickPreferredDealsBucket(
          projectResource.getDoubleClickPreferredDealsBucket());
    }
    if (projectResource.hasAuctionType()) {
      project.setAuctionType(projectResource.getAuctionType());
    }
    if (projectResource.hasDoubleClickProjectResource()) {
      DoubleClickProjectResource doubleClickProjectResource =
          projectResource.getDoubleClickProjectResource();
      if (doubleClickProjectResource.hasEncryptionKey()) {
        project.setEncryptionKey(doubleClickProjectResource.getEncryptionKey());
      }
      if (doubleClickProjectResource.hasIntegrityKey()) {
        project.setIntegrityKey(doubleClickProjectResource.getIntegrityKey());
      }
      if (doubleClickProjectResource.hasDoubleClickReportingBucket()) {
        project.setDoubleClickReportingBucket(
            doubleClickProjectResource.getDoubleClickReportingBucket());
      }
    }
  }

  private void updateBuyerAccount(ProjectUser projectUser, ProjectResource projectResource) {
    if (projectResource.getAdExchangeBuyerAccount() != null
        && (projectResource.hasCookieMatchUrl() || projectResource.hasCookieMatchNid())) {
      AdExchangeBuyerClient adExchangeBuyerClient = adExchangeBuyerService.connect(projectUser);
      String accountId = projectResource.getAdExchangeBuyerAccount().getResourceName();
      Account account = adExchangeBuyerClient.getAccount(accountId);
      if (projectResource.hasCookieMatchUrl()) {
        account.setCookieMatchingUrl(projectResource.getCookieMatchUrl());
      }
      if (projectResource.hasCookieMatchNid()) {
        account.setCookieMatchingNid(projectResource.getCookieMatchNid());
      }
      adExchangeBuyerClient.updateAccount(accountId, account);
    }
  }

  private ProjectUser getProjectUser(ResourceId projectResourceId) {
    Preconditions.checkArgument(projectResourceId.getResourceType() == ResourceType.PROJECT);
    long projectId;
    try {
      projectId = Long.parseLong(projectResourceId.getResourceName());
    } catch (NumberFormatException e) {
      throw new ProjectNotFoundException(projectResourceId.getResourceName());
    }
    return projectService.getProject(projectId);
  }

  private ProjectResource build(ProjectUser projectUser, boolean includeQuotas) {
    Project project = projectUser.getProject();
    ResourceId projectId = getResourceId(Long.toString(project.getId()));
    ProjectResource projectResource = new ProjectResource();
    projectResource.setId(projectId);
    projectResource.setDescription(project.getProjectName());
    projectResource.setOauth2ClientId(project.getOauth2ClientId());
    projectResource.setOauth2ClientSecret(project.getOauth2ClientSecret());
    projectResource.setApiProjectId(project.getApiProjectId());
    projectResource.setApiProjectNumber(project.getApiProjectNumber());
    if (!Strings.isNullOrEmpty(project.getAdExchangeBuyerAccountId())) {
      projectResource.setAdExchangeBuyerAccount(
          projectId.getChildResource(ResourceType.ACCOUNT, project.getAdExchangeBuyerAccountId()));
    }
    projectResource.setVmParameters(project.getVmParameters());
    projectResource.setMainParameters(
        // Migration: mainParameters introduced in 0.4.0
        project.getMainParameters() == null ? "" : project.getMainParameters());
    projectResource.setBidInterceptors(project.getBidInterceptors());
    projectResource.setImpressionInterceptors(project.getImpressionInterceptors());
    projectResource.setClickInterceptors(project.getClickInterceptors());
    projectResource.setMatchInterceptors(project.getMatchInterceptors());
    projectResource.setUserDistUri(project.getUserDistUri());
    projectResource.setWhiteListedIpRanges(project.getWhiteListedIpRanges());
    projectResource.setProjectUuid(project.getProjectUuid());
    projectResource.setDefaultProject(projectUser.isDefault());
    projectResource.setProjectRole(projectUser.getUserRole().getProjectRole());
    projectResource.setAuthorized(projectUser.getUserRole().isAuthorized());
    projectResource.setBidderRequestPort(project.getBidderRequestPort());
    projectResource.setBidderAdminPort(project.getBidderAdminPort());
    projectResource.setLoadBalancerRequestPort(project.getLoadBalancerRequestPort());
    projectResource.setLoadBalancerStatPort(project.getLoadBalancerStatPort());
    projectResource.setBidderOauth2Scopes(project.getBidderOauth2Scopes());
    projectResource.setLoadBalancerOauth2Scopes(project.getLoadBalancerOauth2Scopes());
    DoubleClickProjectResource doubleClickProjectResource = new DoubleClickProjectResource();
    doubleClickProjectResource.setEncryptionKey(project.getEncryptionKey());
    doubleClickProjectResource.setIntegrityKey(project.getIntegrityKey());
    doubleClickProjectResource.setDoubleClickReportingBucket(
        project.getDoubleClickReportingBucket());
    projectResource.setDoubleClickProjectResource(doubleClickProjectResource);
    projectResource.setDoubleClickPreferredDealsBucket(
        project.getDoubleClickPreferredDealsBucket());
    projectResource.setAuctionType(project.getAuctionType());
    if (!project.getBidderMachineTypes().isEmpty()) {
      projectResource.setBidderMachineTypes(project.getBidderMachineTypes());
    }
    if (!project.getLoadBalancerMachineTypes().isEmpty()) {
      projectResource.setLoadBalancerMachineTypes(project.getLoadBalancerMachineTypes());
    }
    ResourceCollectionId bidderImages = projectId.getChildCollection(
        project.getIsBidderImageDefault() ? ResourceType.DEFAULT_IMAGE : ResourceType.CUSTOM_IMAGE);
    if (!Strings.isNullOrEmpty(project.getBidderImage())) {
      projectResource.setBidderImage(bidderImages.getResourceId(project.getBidderImage()));
    }
    ResourceCollectionId loadBalancerImages = projectId.getChildCollection(
        project.getIsLoadBalancerImageDefault()
            ? ResourceType.DEFAULT_IMAGE
            : ResourceType.CUSTOM_IMAGE);
    if (!Strings.isNullOrEmpty(project.getLoadBalancerImage())) {
      projectResource.setLoadBalancerImage((
          loadBalancerImages.getResourceId(project.getLoadBalancerImage())));
    }
    projectResource.setApiProjectAuthorized(projectUser.getUserRole().isAuthorized());
    try {
      ComputeClient computeClient = computeService.connect(projectUser);
      if (!Strings.isNullOrEmpty(project.getNetworkName())) {
        if (computeClient.networkExists(project.getNetworkName())) {
          projectResource.setNetwork(
              projectId.getChildResource(ResourceType.NETWORK, project.getNetworkName()));
        }
      }
      if (includeQuotas) {
        projectResource.setQuotas(quotaResourceService.filterAndBuild(
            projectId.getChildCollection(ResourceType.QUOTA),
            computeClient.getProject().getQuotas()
        ));
      }
    } catch (ApiProjectNotFoundException e) {
      projectResource.setApiProjectAuthorized(false);
    } catch (NoCredentialsForProjectException | UnknownComputeException e) {
      logger.warn(e.toString());
    }
    return projectResource;
  }
}
