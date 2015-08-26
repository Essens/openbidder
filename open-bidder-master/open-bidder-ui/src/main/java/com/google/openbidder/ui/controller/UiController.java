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

import com.google.appengine.api.users.UserService;
import com.google.common.base.Preconditions;
import com.google.openbidder.ui.user.AuthorizationService;
import com.google.openbidder.ui.user.UserIdService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

import javax.inject.Inject;

/**
 * Serve the AngularJS UI. This is done from code rather than being served statically for
 * three reasons:
 * <ul>
 *   <li>populate a token for XSRF protection;</li>
 *   <li>populate a token to open a channel for server push communication; and</li>
 *   <li>prevent clickjacking by enabling the sending of a X-Frame-Options HTTP response header.
 *   Note: static files do not support this on App Engine. This is done by the
 *   {@link com.google.openbidder.ui.interceptor.ClickJackDefenseInterceptor}.</li>
 * </ul>
 */
@Controller
public class UiController {

  private final UserIdService userIdService;
  private final AuthorizationService authorizationService;
  private final UserService userService;
  private final String appVersion;
  private final String appDeployer;
  private final String appDeployTime;

  @Inject
  public UiController(
      UserIdService userIdService,
      AuthorizationService authorizationService,
      UserService userService,
      @Value("${OpenBidder.App.Version}") String appVersion,
      @Value("${OpenBidder.App.Deployer}") String appDeployer,
      @Value("${OpenBidder.App.DeployTime}") String appDeployTime) {
    
    this.userIdService = Preconditions.checkNotNull(userIdService);
    this.authorizationService = Preconditions.checkNotNull(authorizationService);
    this.userService = Preconditions.checkNotNull(userService);
    this.appVersion = Preconditions.checkNotNull(appVersion);
    this.appDeployer = Preconditions.checkNotNull(appDeployer);
    this.appDeployTime = Preconditions.checkNotNull(appDeployTime);
  }

  @RequestMapping("/")
  public String index(ModelMap model, Principal principal) {
    populateModel(model);

    // XSRF token
    model.put("xsrfToken", authorizationService.getAuthorizationToken());

    return "index";
  }

  @RequestMapping("/error/{errorName}")
  public String error(
      @PathVariable("errorName") String errorName,
      ModelMap model) {

    populateModel(model);
    return "error/" + errorName;
  }

  private void populateModel(ModelMap model) {
    // identity
    boolean isLoggedIn = userService.isUserLoggedIn();
    boolean isAdmin = isLoggedIn && userService.isUserAdmin();
    String userEmail = isLoggedIn ? userIdService.getUserId() : null;
    model.put("isLoggedIn", isLoggedIn);
    model.put("isAdmin", isAdmin);
    model.put("userEmail", userEmail);

    // app version
    model.put("appVersion", appVersion);
    model.put("appDeployer", appDeployer);
    model.put("appDeployTime", appDeployTime);
  }
}
