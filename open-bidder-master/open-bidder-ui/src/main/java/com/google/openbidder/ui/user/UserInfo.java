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

package com.google.openbidder.ui.user;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Google user information.
 */
public class UserInfo {

  private final String email;
  private final boolean verified;

  public UserInfo(String email, boolean verified) {
    this.email = Preconditions.checkNotNull(email);
    this.verified = verified;
  }

  public String getEmail() {
    return email;
  }

  public boolean isVerified() {
    return verified;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("email", email)
        .add("verified", verified)
        .toString();
  }
}
