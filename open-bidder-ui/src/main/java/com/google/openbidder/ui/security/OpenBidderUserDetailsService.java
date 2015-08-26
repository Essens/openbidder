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

package com.google.openbidder.ui.security;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.appengine.api.users.UserService;
import com.google.openbidder.ui.entity.UserPreference;
import com.google.openbidder.ui.user.UserIdService;
import com.google.openbidder.ui.user.exception.UserNotFoundException;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.inject.Inject;

/**
 * Loads {@link UserDetails} for {@link OpenBidderUser}s.
 */
public class OpenBidderUserDetailsService implements UserDetailsService {

  private final UserIdService userIdService;
  private final UserService userService;

  @Inject
  public OpenBidderUserDetailsService(
      UserIdService userIdService,
      UserService userService) {

    this.userIdService = checkNotNull(userIdService);
    this.userService = checkNotNull(userService);
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    UserPreference userPreference;
    try {
      userPreference = userIdService.getUserPreference(username);
    } catch (UserNotFoundException e) {
      userPreference = new UserPreference(username);
    }

    // this is to get around a limitation on GAE that you can't inspect the admin status for
    // anyone other than the current user
    userPreference.setAdmin(
        username.equals(userIdService.getUserId()) && userService.isUserAdmin());

    return new OpenBidderUser(userPreference);
  }
}
