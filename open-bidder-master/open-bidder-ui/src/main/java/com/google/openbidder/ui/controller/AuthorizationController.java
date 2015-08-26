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

package com.google.openbidder.ui.controller;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.openbidder.ui.project.ProjectService;
import com.google.openbidder.ui.project.ProjectUser;
import com.google.openbidder.ui.user.AuthorizationService;
import com.google.openbidder.ui.user.UserIdService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

/**
 * OAuth 2.0 authorization controller.
 */
@Controller
public class AuthorizationController {

  private static final Logger logger = LoggerFactory.getLogger(AuthorizationController.class);

  private final ProjectService projectService;
  private final AuthorizationService authorizationService;
  private final UserIdService userIdService;

  @Inject
  public AuthorizationController(
      ProjectService projectService,
      AuthorizationService authorizationService,
      UserIdService userIdService) {

    this.projectService = Preconditions.checkNotNull(projectService);
    this.authorizationService = Preconditions.checkNotNull(authorizationService);
    this.userIdService = Preconditions.checkNotNull(userIdService);
  }

  @RequestMapping(value = "/oauth2", method = RequestMethod.GET)
  public String redirectForAuthorization(
      HttpServletRequest request,
      @RequestParam("projectId") Long projectId,
      @RequestParam(value = "redirectTo", required = false) String redirectTo) {

    ProjectUser projectUser = projectService.getProject(projectId);
    AuthorizationCodeFlow flow = authorizationService.initializeFlow(projectUser.getProject());

    // Otherwise redirect the user to the OAuth provider and ask for authorization.
    String redirectUri = getRedirectUri(request);

    // The only callback variable is "state", so we must pack both the projectId and the
    // callback URL into it
    String effectiveRedirectTo = Strings.isNullOrEmpty(redirectTo)
      ? "/app/index.html#/projects/" + projectId
      : redirectTo;
    String state = String.format("%d:%s", projectId, effectiveRedirectTo);
    String responseUrl = flow.newAuthorizationUrl()
        .setRedirectUri(redirectUri)
        .setState(state) // passed to /oauth2callback
        .build();
    return "redirect:" + responseUrl;
  }

  @RequestMapping(value = "/oauth2callback", method = RequestMethod.GET)
  public String authorizationCallback(
      HttpServletRequest request,
      @RequestParam("state") String state) {

    String[] parts = state.split(":", 2);
    Preconditions.checkArgument(parts.length == 2, "Invalid callback state: %s", state);
    Long projectId = Long.valueOf(parts[0]);
    String redirectTo = parts[1];
    ProjectUser projectUser = projectService.getProject(projectId);
    saveTokens(request, projectUser);
    return "redirect:" + redirectTo;
  }

  private void saveTokens(HttpServletRequest request, ProjectUser projectUser) {
    AuthorizationCodeFlow flow = authorizationService.initializeFlow(projectUser.getProject());
    AuthorizationCodeResponseUrl responseUrl = getResponseUrl(request);
    String code = responseUrl.getCode();
    String redirectUri = getRedirectUri(request);
    if (responseUrl.getError() == null && code != null) {
      try {
        TokenResponse response = flow.newTokenRequest(code).setRedirectUri(redirectUri).execute();
        String userId = userIdService.getUserId();
        flow.createAndStoreCredential(response, userId);
      } catch (IOException e) {
        logger.warn("OAuth client error", e);
      }
    }
  }

  private static AuthorizationCodeResponseUrl getResponseUrl(HttpServletRequest request) {
    StringBuffer buf = request.getRequestURL();
    if (request.getQueryString() != null) {
      buf.append('?').append(request.getQueryString());
    }
    return new AuthorizationCodeResponseUrl(buf.toString());
  }

  private static String getRedirectUri(HttpServletRequest req) {
    GenericUrl url = new GenericUrl(req.getRequestURL().toString());
    url.setRawPath("/oauth2callback");
    return url.build();
  }
}
