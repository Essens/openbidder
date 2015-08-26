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

package com.google.openbidder.ui.adexchangebuyer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *  Write privileges were required for the Ad Exchange Buyer account. The user does not have them.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class AdExchangeBuyerAccountAccessException extends AdExchangeBuyerAccountException {

  private static final String MESSAGE = "No write access to Ad Exchange Buyer account %s";

  public AdExchangeBuyerAccountAccessException(String accountId) {
    super(accountId, String.format(MESSAGE, accountId));
  }
}