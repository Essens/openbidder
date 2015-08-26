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

package com.google.openbidder.ui.interceptor;

import com.google.common.base.Preconditions;
import com.google.openbidder.ui.entity.support.Permission;
import com.google.openbidder.ui.security.CheckPermission;
import com.google.openbidder.ui.user.AuthorizationService;
import com.google.openbidder.ui.util.web.WebUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * XSRF prevention by pre-shared token checking.
 */
public class XsrfValidationInterceptor extends HandlerInterceptorAdapter {

  private static final Logger logger = LoggerFactory.getLogger(XsrfValidationInterceptor.class);

  public static final String AUTHORIZATION_TOKEN = "X-Authorization-Token";

  private final AuthorizationService authorizationService;

  @Inject
  public XsrfValidationInterceptor(AuthorizationService authorizationService) {
    this.authorizationService = Preconditions.checkNotNull(authorizationService);
  }

  @Override
  public boolean preHandle(
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler) throws Exception {

    if (WebUtils.isInternalRequest(request)) {
      if (logger.isDebugEnabled()) {
        logger.debug("internal request automatically passes");
      }
      return true;
    }

    HandlerMethod method = (HandlerMethod) handler;
    String authToken;
    CheckPermission annotation = AnnotationUtils.findAnnotation(
        method.getMethod(), CheckPermission.class);
    if (annotation != null && annotation.value() != Permission.READ) {
      authToken = request.getHeader(AUTHORIZATION_TOKEN);
      if (authToken == null || !authorizationService.isValidAuthorizationToken(authToken)) {
        logger.warn("auth token {} not acceptable", authToken);
        response.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
        return false;
      }
    }
    return true;
  }
}
