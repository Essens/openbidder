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

import com.google.api.services.adexchangebuyer.model.Account;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.openbidder.ui.adexchangebuyer.AdExchangeBuyerClient;
import com.google.openbidder.ui.adexchangebuyer.AdExchangeBuyerService;
import com.google.openbidder.ui.project.ProjectService;
import com.google.openbidder.ui.project.ProjectUser;
import com.google.openbidder.ui.resource.AccountResourceService;
import com.google.openbidder.ui.resource.model.AccountResource;
import com.google.openbidder.ui.resource.support.AbstractAdExchangeBuyerResourceService;
import com.google.openbidder.ui.resource.support.ResourceCollectionId;
import com.google.openbidder.ui.resource.support.ResourceId;
import com.google.openbidder.ui.resource.support.ResourceMethod;
import com.google.openbidder.ui.resource.support.ResourceType;

import java.util.EnumSet;
import java.util.List;

import javax.inject.Inject;

/**
 * Default implementation of {@link AccountResourceService}.
 */
public class AccountResourceServiceImpl
    extends AbstractAdExchangeBuyerResourceService<AccountResource>
    implements AccountResourceService {

  @Inject
  public AccountResourceServiceImpl(
      ProjectService projectService,
      AdExchangeBuyerService adExchangeBuyerService) {

    super(ResourceType.ACCOUNT,
        EnumSet.of(ResourceMethod.GET, ResourceMethod.LIST),
        projectService,
        adExchangeBuyerService);
  }

  @Override
  protected AccountResource get(
      AdExchangeBuyerClient adExchangeBuyerClient,
      ProjectUser projectUser,
      ResourceId resourceId,
      Multimap<String, String> params) {

    return build(
        resourceId.getParent(),
        adExchangeBuyerClient.getAccount(resourceId.getResourceName()));
  }

  @Override
  protected List<? extends AccountResource> list(
      AdExchangeBuyerClient adExchangeBuyerClient,
      ProjectUser projectUser,
      final ResourceCollectionId resourceCollectionId,
      Multimap<String, String> params) {

    return Lists.transform(
        adExchangeBuyerClient.listAccounts(),
        new Function<Account, AccountResource>() {
          @Override public AccountResource apply(Account account) {
            return build(resourceCollectionId, account);
          }});
  }

  private static AccountResource build(
      ResourceCollectionId resourceCollectionId,
      Account account) {

    String accountId = account.getId().toString();
    AccountResource accountResource = new AccountResource();
    accountResource.setId(resourceCollectionId.getResourceId(accountId));
    accountResource.setCookieMatchNid(account.getCookieMatchingNid());
    accountResource.setCookieMatchUrl(account.getCookieMatchingUrl());
    accountResource.setMaximumTotalQps(account.getMaximumTotalQps());
    return accountResource;
  }
}
