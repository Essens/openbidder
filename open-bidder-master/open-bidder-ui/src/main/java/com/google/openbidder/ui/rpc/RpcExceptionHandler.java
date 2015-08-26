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

package com.google.openbidder.ui.rpc;

import com.google.appengine.api.users.UserService;
import com.google.common.base.Preconditions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Customize the response to exceptions for RPC calls by encoding the exception into a JSON
 * response. For admin users this includes the stack trace.
 */
public class RpcExceptionHandler implements HandlerExceptionResolver, Ordered {

  private static final Logger logger = LoggerFactory.getLogger(RpcExceptionHandler.class);

  private static final int DEFAULT_ORDER = 10;

  private final UserService userService;
  private final View serviceExceptionView;
  private int order = DEFAULT_ORDER;

  @Inject
  public RpcExceptionHandler(
      UserService userService,
      @Named("jsonView") View serviceExceptionView) {

    this.userService = Preconditions.checkNotNull(userService);
    this.serviceExceptionView = Preconditions.checkNotNull(serviceExceptionView);
  }

  @Override
  public @Nullable ModelAndView resolveException(
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler,
      Exception ex) {

    if (RpcUtil.isAjax(request)) {
      logger.error("RPC exception", ex); // otherwise this swallows exceptions
      RpcResponse<Void> rpcResponse = RpcResponse.sendException(request, ex,
          userService.isUserAdmin());
      ResponseStatus responseStatus = AnnotationUtils.findAnnotation(
          ex.getClass(), ResponseStatus.class);
      HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
      if (responseStatus != null) {
        httpStatus = responseStatus.value();
      }
      response.setStatus(httpStatus.value());
      return new ModelAndView(serviceExceptionView, "response", rpcResponse);
    }
    return null;
  }

  @Override
  public int getOrder() {
    return order;
  }

  public void setOrder(int order) {
    this.order = order;
  }
}
