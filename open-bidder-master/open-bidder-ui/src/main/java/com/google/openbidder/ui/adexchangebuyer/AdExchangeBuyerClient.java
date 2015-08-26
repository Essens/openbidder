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

package com.google.openbidder.ui.adexchangebuyer;

import com.google.api.services.adexchangebuyer.model.Account;
import com.google.api.services.adexchangebuyer.model.Creative;
import com.google.api.services.adexchangebuyer.model.DirectDeal;

import java.util.List;

/**
 * Abstract operations to the Ad Exchange Buyer API.
 */
public interface AdExchangeBuyerClient {

  /**
   * Get an Ad Exchange Buyer account by id.
   *
   * @throws com.google.openbidder.ui.adexchangebuyer.exception.AdExchangeBuyerAccountNotSetException
   * if the account id has not been set.
   * @throws com.google.openbidder.ui.adexchangebuyer.exception.AdExchangeBuyerAccountResourceNotFoundException
   * if account cannot be found.
   */
  Account getAccount(String accountId);

  /**
   * @return All accounts a user has access to
   * @throws com.google.openbidder.ui.adexchangebuyer.exception.AdExchangeBuyerApiAccessException
   * if the user does not have permission to access the API
   */
  List<Account> listAccounts();

  /**
   * Update an Ad Exchange Buyer account by id.
   *
   * @throws com.google.openbidder.ui.adexchangebuyer.exception.AdExchangeBuyerAccountNotSetException
   * if the account id has not been set.
   * @throws com.google.openbidder.ui.adexchangebuyer.exception.AdExchangeBuyerAccountAccessException
   * if the user does not have write access to the specified account.
   */
  Account updateAccount(String accountId, Account account);

  /**
   * Get the list of bidder locations associated with the given Ad Exchange Buyer account.
   *
   * @return An empty list if no bidder locations have been found.
   */
  List<Account.BidderLocation> getBidderLocations(String accountId);

  /**
   * @return A list of creatives a user has stored in their buyer account.
   * @throws com.google.openbidder.ui.adexchangebuyer.exception.AdExchangeBuyerAccountAccessException
   * if the user does not have permission to access the API
   */
  List<Creative> listCreatives();

  /**
   * @return Lists all preferred deals available to the authenticated user.
   * @throws com.google.openbidder.ui.adexchangebuyer.exception.AdExchangeBuyerApiAccessException
   * if the user does not have permission to access the API.
   */
   List<DirectDeal> listDirectDeals();

}
