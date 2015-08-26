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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.openbidder.ui.entity.UserPreference;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.EnumSet;

/**
 * Represents a {@link UserDetails} for the Spring Security system.
 * <p>
 * The granted authorities are application-specific. In our case our authorities are
 * represented by {@link AppRole}. A user can have no authorities (which means they won't pass
 * any {@code }hasRole()} tests but they will pass {@code isAuthenticated()} checks.
 * <p>
 * Presently, admin users have the {@link AppRole#ADMIN} authority. Admins and those users
 * with one or more visible projects have the {@link AppRole#USER} role.
 */
public class OpenBidderUser implements UserDetails {

  private static final ImmutableList<AppRole> USER_ROLES =
      ImmutableList.of(AppRole.USER);

  private static final ImmutableList<AppRole> ADMIN_ROLES =
      ImmutableList.of(AppRole.USER, AppRole.ADMIN);

  private final UserPreference userPreference;
  private final ImmutableCollection<? extends GrantedAuthority> authorities;

  public OpenBidderUser(UserPreference userPreference) {
    this.userPreference = Preconditions.checkNotNull(userPreference);
    if (userPreference.isAdmin()) {
      this.authorities = ImmutableSet.copyOf(EnumSet.copyOf(ADMIN_ROLES));
    } else if (userPreference.getVisibleProjectCount() > 0) {
      this.authorities = ImmutableSet.copyOf(EnumSet.copyOf(USER_ROLES));
    } else {
      this.authorities = ImmutableSet.copyOf(EnumSet.noneOf(AppRole.class));
    }
  }

  public UserPreference getUserPreference() {
    return userPreference;
  }

  @Override
  public ImmutableCollection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    throw new UnsupportedOperationException("getPassword");
  }

  @Override
  public String getUsername() {
    return userPreference.getEmail();
  }

  @Override
  public boolean isAccountNonExpired() {
    return false;
  }

  @Override
  public boolean isAccountNonLocked() {
    return false;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return false;
  }

  @Override
  public boolean isEnabled() {
    return userPreference.isAdmin() || userPreference.getVisibleProjectCount() > 0;
  }
}
