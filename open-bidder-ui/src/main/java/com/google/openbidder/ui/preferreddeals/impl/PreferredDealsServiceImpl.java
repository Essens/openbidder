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

package com.google.openbidder.ui.preferreddeals.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.api.services.adexchangebuyer.model.DirectDeal;
import com.google.common.collect.ImmutableList;
import com.google.openbidder.deals.model.Deals.PreferredDeal;
import com.google.openbidder.storage.dao.CloudStorageDao;
import com.google.openbidder.ui.adexchangebuyer.AdExchangeBuyerClient;
import com.google.openbidder.ui.adexchangebuyer.AdExchangeBuyerService;
import com.google.openbidder.ui.preferreddeals.PreferredDealsService;
import com.google.openbidder.ui.project.ProjectService;
import com.google.openbidder.ui.project.ProjectUser;
import com.google.openbidder.ui.util.DaoFactory;
import com.google.protobuf.MessageLite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import javax.inject.Inject;

/**
 * Default implementation of {@link PreferredDealsService}.
 */
public class PreferredDealsServiceImpl implements PreferredDealsService {
  private static final Logger logger = LoggerFactory.getLogger(PreferredDealsServiceImpl.class);

  private static final String PREFERRED_DEALS_OBJECT = "preferred-deals";

  private final ProjectService projectService;
  private final AdExchangeBuyerService adExchangeBuyerService;
  private final DaoFactory daoFactory;

  @Inject
  public PreferredDealsServiceImpl(
      ProjectService projectService,
      AdExchangeBuyerService adExchangeBuyerService,
      DaoFactory daoFactory) {
    this.projectService = checkNotNull(projectService);
    this.adExchangeBuyerService = checkNotNull(adExchangeBuyerService);
    this.daoFactory = checkNotNull(daoFactory);
  }

  @Override
  public void uploadPreferredDeals(long projectId) {

    ProjectUser projectUser = projectService.getProject(projectId);

    AdExchangeBuyerClient adExchangeBuyerClient = adExchangeBuyerService.connect(projectUser);
    List<DirectDeal> preferredDealList = adExchangeBuyerClient.listDirectDeals();

    String bucketName = projectUser.getProject().getDoubleClickPreferredDealsBucket();
    CloudStorageDao<MessageLite> dao = daoFactory.buildDao(projectUser);
    dao.deleteObject(bucketName, PREFERRED_DEALS_OBJECT);

    if (preferredDealList.isEmpty()) {
      logger.info("There are no preferred deals available for the adx account: {}",
          projectUser.getProject().getAdExchangeBuyerAccountId());
      return;
    }

    ImmutableList.Builder<PreferredDeal> preferredDealProtoList = ImmutableList.builder();
    for (DirectDeal preferredDeal : preferredDealList) {

      PreferredDeal.Builder builder = PreferredDeal.newBuilder();
      if (preferredDeal.getId() != null) {
        builder.setDealId(preferredDeal.getId());
      }
      if (preferredDeal.getAccountId() != null) {
        builder.setAccountId(preferredDeal.getAccountId());
      }
      if (preferredDeal.getFixedCpm() != null) {
        builder.setFixedCpm(preferredDeal.getFixedCpm());
      }
      if (preferredDeal.getSellerNetwork() != null) {
        builder.setSellerNetwork(preferredDeal.getSellerNetwork());
      }
      if (preferredDeal.getAdvertiser() != null) {
        builder.setAdvertiserName(preferredDeal.getAdvertiser());
      }
      preferredDealProtoList.add(builder.build());
    }

    dao.createObjectList(preferredDealProtoList.build(), bucketName, PREFERRED_DEALS_OBJECT);
    logger.info("Preferred deals have been successfully uploaded");
  }
}
