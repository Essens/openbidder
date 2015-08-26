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

package com.google.openbidder.ui.notify;

import com.google.common.base.Preconditions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Failed to find a matching subscriber.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class SubscriberNotFoundException extends RuntimeException {

  private static final String MESSAGE = "User %s, token %s not found";

  private final String userId;
  private final String token;

  public SubscriberNotFoundException(String userId, String token) {
    super(String.format(MESSAGE,
        Preconditions.checkNotNull(userId, "userId is null"),
        Preconditions.checkNotNull(token, "token is null")));
    this.userId = userId;
    this.token = token;
  }

  public String getUserId() {
    return userId;
  }

  public String getToken() {
    return token;
  }
}
