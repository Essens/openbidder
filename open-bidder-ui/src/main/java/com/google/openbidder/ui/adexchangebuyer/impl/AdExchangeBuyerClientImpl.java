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

package com.google.openbidder.ui.adexchangebuyer.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.adexchangebuyer.AdExchangeBuyer;
import com.google.api.services.adexchangebuyer.model.Account;
import com.google.api.services.adexchangebuyer.model.Creative;
import com.google.api.services.adexchangebuyer.model.DirectDeal;
import com.google.openbidder.ui.adexchangebuyer.AdExchangeBuyerClient;
import com.google.openbidder.ui.adexchangebuyer.AdExchangeBuyerResourceType;
import com.google.openbidder.ui.adexchangebuyer.exception.AdExchangeBuyerAccountAccessException;
import com.google.openbidder.ui.adexchangebuyer.exception.AdExchangeBuyerAccountException;
import com.google.openbidder.ui.adexchangebuyer.exception.AdExchangeBuyerAccountNotSetException;
import com.google.openbidder.ui.adexchangebuyer.exception.AdExchangeBuyerAccountResourceNotFoundException;
import com.google.openbidder.ui.adexchangebuyer.exception.AdExchangeBuyerException;

import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Default implementation for {@link AdExchangeBuyerClient}.
 */
public class AdExchangeBuyerClientImpl implements AdExchangeBuyerClient {
  private final AdExchangeBuyer adExchangebuyer;

  public AdExchangeBuyerClientImpl(AdExchangeBuyer adExchangebuyer) {
    this.adExchangebuyer = checkNotNull(adExchangebuyer);
  }

  @Override
  public Account getAccount(final String accountId) {
    return execute(new Action<Account>() {
      @Override public Account execute() throws IOException {
        return adExchangebuyer.accounts().get(parseId(accountId)).execute();
      }},
      AdExchangeBuyerResourceType.ACCOUNT, accountId);
  }

  @Override
  public List<Account> listAccounts() {
    return execute(new Action<List<Account>>() {
      @Override public List<Account> execute() throws IOException {
        List<Account> accounts = adExchangebuyer.accounts().list().execute().getItems();
        return accounts == null ? new ArrayList<Account>() : accounts;
      }},
      AdExchangeBuyerResourceType.ACCOUNT);
  }

  @Override
  public Account updateAccount(final String accountId, final Account account) {
    return execute(new Action<Account>() {
      @Override public Account execute() throws IOException {
        return adExchangebuyer.accounts().patch(parseId(accountId), account).execute();
      }},
      AdExchangeBuyerResourceType.ACCOUNT);
  }

  @Override
  public List<Account.BidderLocation> getBidderLocations(String accountId) {
    List<Account.BidderLocation> bidderLocations = getAccount(accountId).getBidderLocation();
    return bidderLocations == null ? new ArrayList<Account.BidderLocation>() : bidderLocations;
  }

  @Override
  public List<Creative> listCreatives() {
    return execute(new Action<List<Creative>>() {
      @Override public List<Creative> execute() throws IOException {
        return adExchangebuyer.creatives().list().execute().getItems();
      }},
      AdExchangeBuyerResourceType.CREATIVE);
  }

  @Override
  public List<DirectDeal> listDirectDeals() {
    return execute(new Action<List<DirectDeal>>() {
      @Override public List<DirectDeal> execute() throws IOException {
        return adExchangebuyer.directDeals().list().execute().getDirectDeals();
      }},
      AdExchangeBuyerResourceType.PREFERRED_DEAL);
  }

  private static int parseId(String id) {
    if (id == null) {
      throw new AdExchangeBuyerAccountNotSetException();
    }
    try {
      return Integer.parseInt(id);
    } catch (NumberFormatException e) {
      throw new AdExchangeBuyerAccountException(id);
    }
  }

  private <T> T execute(Action<T> action, AdExchangeBuyerResourceType resourceType) {
    return execute(
        action,
        checkNotNull(resourceType),
        /* Ad Exchange Buyer account id */ null);
  }

  private <T> T execute(
      Action<T> action,
      AdExchangeBuyerResourceType resourceType,
      @Nullable String accountId) {

    try {
      return action.execute();
    } catch (GoogleJsonResponseException e) {
      if (resourceType == AdExchangeBuyerResourceType.ACCOUNT) {
        if (e.getStatusCode() == HttpStatus.NOT_FOUND.value()) {
          throw new AdExchangeBuyerAccountResourceNotFoundException(accountId);
        }
      }
      if (e.getStatusCode() == HttpStatus.FORBIDDEN.value()) {
        throw new AdExchangeBuyerAccountAccessException(accountId);
      }
      throw new AdExchangeBuyerException(e.getDetails().getMessage(), e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static interface Action<T> {
    T execute() throws IOException;
  }
}
