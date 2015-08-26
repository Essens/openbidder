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

import com.google.api.services.compute.model.Project;
import com.google.api.services.compute.model.Quota;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.openbidder.ui.compute.ComputeClient;
import com.google.openbidder.ui.compute.ComputeService;
import com.google.openbidder.ui.project.ProjectService;
import com.google.openbidder.ui.project.ProjectUser;
import com.google.openbidder.ui.resource.QuotaResourceService;
import com.google.openbidder.ui.resource.model.QuotaResource;
import com.google.openbidder.ui.resource.support.AbstractComputeResourceService;
import com.google.openbidder.ui.resource.support.ResourceCollectionId;
import com.google.openbidder.ui.resource.support.ResourceMethod;
import com.google.openbidder.ui.resource.support.ResourceType;

import org.springframework.beans.factory.annotation.Value;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

/**
 * Default implementation of {@link com.google.openbidder.ui.resource.QuotaResourceService}.
 */
public class QuotaResourceServiceImpl
    extends AbstractComputeResourceService<QuotaResource>
    implements QuotaResourceService {

  private final Predicate<Quota> isRequired = new Predicate<Quota>() {
    @Override
    public boolean apply(Quota quota) {
      return visibleQuotas.contains(quota.getMetric());
    }
  };

  private final Set<String> visibleQuotas;

  @Inject
  public QuotaResourceServiceImpl(
      ProjectService projectService,
      ComputeService computeService,
      @Value("#{visibleQuotas}") List<String> visibleQuotas) {

    super(ResourceType.QUOTA,
        EnumSet.of(ResourceMethod.LIST),
        projectService,
        computeService);
    this.visibleQuotas = ImmutableSet.copyOf(visibleQuotas);
  }

  @Override
  protected List<? extends QuotaResource> list(
      ComputeClient computeClient,
      ProjectUser projectUser,
      ResourceCollectionId resourceCollectionId,
      Multimap<String, String> params) {

    Project project = computeClient.getProject();
    return filterAndBuild(resourceCollectionId, project.getQuotas());
  }

  @Override
  public List<QuotaResource> filterAndBuild(
      final ResourceCollectionId resourceCollectionId,
      List<Quota> quotas) {

    return Lists.newArrayList(Iterables.transform(
        Iterables.filter(quotas, isRequired),
        new Function<Quota, QuotaResource>() {
          @Override
          public QuotaResource apply(Quota quotas) {
            QuotaResource quotaResource = new QuotaResource();
            quotaResource.setId(
                resourceCollectionId.getResourceId(quotas.getMetric().toLowerCase()));
            quotaResource.setMetric(quotas.getMetric());
            quotaResource.setUsage((quotas.getUsage()));
            quotaResource.setLimit(quotas.getLimit());
            return quotaResource;
          }
        }));
  }
}
