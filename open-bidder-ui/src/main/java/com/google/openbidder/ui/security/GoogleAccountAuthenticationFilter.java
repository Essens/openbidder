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
import com.google.appengine.api.users.UserService;

import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This filter is called before the request is served. It determines if the user has been
 * authenticated to App Engine (by there being a {@link User}) and then ensures an
 * {@link Authentication} exists for that user. If there isn't one, one is requested (from the
 * {@link AuthenticationManager}).
 * <p>
 * {@link AuthenticationException}s trigger a {@link AuthenticationFailureHandler} call,
 * which will probably redirect the user to the App Engine login page or return an error page.
 */
public class GoogleAccountAuthenticationFilter extends GenericFilterBean {

  private final UserService userService;
  private final AuthenticationDetailsSource<ServletRequest, WebAuthenticationDetails> ads;
  private final AuthenticationManager authenticationManager;
  private final AuthenticationFailureHandler failureHandler;

  @Inject
  public GoogleAccountAuthenticationFilter(
      UserService userService,
      AuthenticationDetailsSource<ServletRequest, WebAuthenticationDetails> ads,
      AuthenticationManager authenticationManager,
      AuthenticationFailureHandler failureHandler) {

    this.userService = checkNotNull(userService);
    this.ads = checkNotNull(ads);
    this.authenticationManager = checkNotNull(authenticationManager);
    this.failureHandler = checkNotNull(failureHandler);
  }

  @Override
  public void doFilter(
      ServletRequest request,
      ServletResponse response,
      FilterChain chain) throws IOException, ServletException {

    User googleUser = userService.getCurrentUser();
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (googleUser == null) {
      // we need to manually de-authenticate the user if they logged out
      SecurityContextHolder.getContext().setAuthentication(null);
    } else if (authentication == null) {
      // otherwise try and authenticate the user if no authentication present exists
      PreAuthenticatedAuthenticationToken token =
          new PreAuthenticatedAuthenticationToken(googleUser, null);
      token.setDetails(ads.buildDetails(request));

      try {
        authentication = authenticationManager.authenticate(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      } catch (AuthenticationException e) {
        failureHandler.onAuthenticationFailure(
            (HttpServletRequest) request,
            (HttpServletResponse) response,
            e);
        return;
      }
    }

    chain.doFilter(request, response);
  }
}
