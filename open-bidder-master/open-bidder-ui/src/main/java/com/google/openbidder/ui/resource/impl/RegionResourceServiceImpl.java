/*
 * Copyright 2013 Google Inc. All Rights Reserved.
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

import com.google.api.services.compute.model.Quota;
import com.google.api.services.compute.model.Region;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.openbidder.ui.compute.ComputeClient;
import com.google.openbidder.ui.compute.ComputeService;
import com.google.openbidder.ui.project.ProjectService;
import com.google.openbidder.ui.project.ProjectUser;
import com.google.openbidder.ui.resource.RegionResourceService;
import com.google.openbidder.ui.resource.model.QuotaResource;
import com.google.openbidder.ui.resource.model.RegionResource;
import com.google.openbidder.ui.resource.support.AbstractComputeResourceService;
import com.google.openbidder.ui.resource.support.ResourceCollectionId;
import com.google.openbidder.ui.resource.support.ResourceId;
import com.google.openbidder.ui.resource.support.ResourceMethod;
import com.google.openbidder.ui.resource.support.ResourceType;

import org.springframework.beans.factory.annotation.Value;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

/**
 * Default implementation of {@link RegionResourceService}
 */
public class RegionResourceServiceImpl
    extends AbstractComputeResourceService<RegionResource>
    implements RegionResourceService {

  private final Predicate<Quota> isRequired = new Predicate<Quota>() {
    @Override
    public boolean apply(Quota quota) {
      return visibleRegionalQuotas.contains(quota.getMetric());
    }
  };

  private final Set<String> visibleRegionalQuotas;

  @Inject
  public RegionResourceServiceImpl (
      ProjectService projectService,
      ComputeService computeService,
      @Value("#{visibleRegionalQuotas}") List<String> visibleRegionalQuotas) {

    super(ResourceType.REGION,
        EnumSet.of(ResourceMethod.GET, ResourceMethod.LIST),
        projectService,
        computeService);
    this.visibleRegionalQuotas = ImmutableSet.copyOf(visibleRegionalQuotas);
  }

  @Override
  protected RegionResource get(
      ComputeClient computeClient,
      ProjectUser projectUser,
      ResourceId resourceId,
      Multimap<String, String> params) {

    Region region = computeClient.getRegion(resourceId.getResourceName());
    return build(region, resourceId);
  }

  @Override
  protected List<? extends RegionResource> list(
    ComputeClient computeClient,
    ProjectUser projectUser,
    final ResourceCollectionId resourceCollectionId,
    Multimap<String, String> params) {

    return Lists.transform(computeClient.listRegions(), new Function<Region, RegionResource>() {
      @Override
      public RegionResource apply(Region region) {
        ResourceId regionResourceId = resourceCollectionId.getResourceId(region.getName());

        ResourceId projectResourceId = regionResourceId.getParentResource();
        if (projectResourceId == null
            || projectResourceId.getResourceType() != ResourceType.PROJECT) {
          throw new IllegalStateException("parent not a project: " + projectResourceId);
        }
        return build(region, regionResourceId);
      }
    });
  }

  private RegionResource build(Region region, ResourceId regionResourceId) {

    RegionResource regionResource = new RegionResource();
    regionResource.setId(regionResourceId);
    regionResource.setDescription(region.getDescription());
    regionResource.setRegionalQuotaResources(ImmutableList.copyOf(Iterables.transform(
        Iterables.filter(region.getQuotas(), isRequired),
        new Function<Quota, QuotaResource>() {
          @Override
          public QuotaResource apply(Quota quota) {
            QuotaResource quotaResource = new QuotaResource();
            quotaResource.setMetric(quota.getMetric());
            quotaResource.setUsage(quota.getUsage());
            quotaResource.setLimit(quota.getLimit());
            return quotaResource;
          }
        })));
    return regionResource;
  }
}