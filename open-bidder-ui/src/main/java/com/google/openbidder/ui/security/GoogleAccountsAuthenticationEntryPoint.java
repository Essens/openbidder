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

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Delegates to Google login to authenticate users.
 */
public class GoogleAccountsAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final UserService userService;

  @Inject
  public GoogleAccountsAuthenticationEntryPoint(UserService userService) {
    this.userService = checkNotNull(userService);
  }

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException) throws IOException {

    response.sendRedirect(userService.createLoginURL(request.getRequestURI()));
  }
}
