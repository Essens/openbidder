// Copyright 2013 Google Inc. All Rights Reserved.

package com.google.openbidder.ui.adexchangebuyer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * No Ad Exchange Buyer account has been set.
 */
@ResponseStatus(HttpStatus.PRECONDITION_FAILED)
public class AdExchangeBuyerAccountNotSetException extends AdExchangeBuyerException {
}