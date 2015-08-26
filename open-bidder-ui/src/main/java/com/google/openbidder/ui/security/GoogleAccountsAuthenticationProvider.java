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

import com.google.appengine.api.users.User;
import com.google.openbidder.ui.user.UserIdService;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import javax.inject.Inject;

/**
 * {@link AuthenticationProvider} that loads {@link OpenBidderUser}s.
 */
public class GoogleAccountsAuthenticationProvider implements AuthenticationProvider {

  private final OpenBidderUserDetailsService openBidderUserDetailsService;
  private final UserIdService userIdService;

  @Inject
  public GoogleAccountsAuthenticationProvider(
      OpenBidderUserDetailsService openBidderUserDetailsService,
      UserIdService userIdService) {

    this.openBidderUserDetailsService = checkNotNull(openBidderUserDetailsService);
    this.userIdService = checkNotNull(userIdService);
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    User googleUser = (User) authentication.getPrincipal();
    String userId = userIdService.getUserId(googleUser);
    OpenBidderUser openBidderUser =
        (OpenBidderUser) openBidderUserDetailsService.loadUserByUsername(userId);
    return new PreAuthenticatedAuthenticationToken(
        googleUser,
        openBidderUser.getUserPreference(),
        openBidderUser.getAuthorities());
  }

  @Override
  public final boolean supports(Class<?> authentication) {
    return PreAuthenticatedAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
