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

package com.google.openbidder.ui.controller.support;

import com.google.openbidder.ui.resource.exception.ResourceMethodNotAllowedException;
import com.google.openbidder.ui.resource.support.ResourceId;
import com.google.openbidder.ui.resource.support.ResourceMethod;
import com.google.openbidder.ui.rpc.RpcFormResponse;
import com.google.openbidder.ui.util.web.ResourceIdEditor;
import com.google.openbidder.ui.util.web.WebUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Base level error handling. This exists mainly to deal with several oddities about how
 * Spring deals with validating JSON inputs (via @RequestBody) versus binding URL-encoded
 * POST requests.
 * <p>
 * For example, a binding error on a JSON request will throw an exception, not a call to
 * the controller with a populated BindingResult so this controller populates a response
 * automatically.
 */
public abstract class AbstractErrorHandlingController<T> {

  private static final Logger logger =
      LoggerFactory.getLogger(AbstractErrorHandlingController.class);

  @InitBinder
  public final void initBinder(ServletRequestDataBinder binder) throws Exception {
    binder.registerCustomEditor(ResourceId.class, new ResourceIdEditor());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseBody
  public final ResponseEntity<RpcFormResponse<T>> invalidArgument(
      HttpServletRequest request,
      MethodArgumentNotValidException e) {

    return bindingErrorResponse(request, e.getBindingResult());
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseBody
  public final void unreadableMessage(
      HttpServletRequest request,
      HttpServletResponse response,
      HttpMessageNotReadableException e) {

    String content;
    try {
      content = WebUtils.readBuffer(request.getReader());
    } catch (IOException | IllegalStateException ex) {
      content = null;
    }
    logger.warn("Unreadable message. Content-type: {}, length: {}, content: {}", new Object[]{
        request.getContentType(), request.getContentLength(), content
    }, e);
    try {
      response.sendError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    } catch (IOException ex) {
      logger.error("Error sending error response", ex);
    }
  }

  @ExceptionHandler(ResourceMethodNotAllowedException.class)
  @ResponseBody
  public ResponseEntity<Void> methodNotAllowed(ResourceMethodNotAllowedException e) {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setAllow(ResourceMethod.toHttpMethod(e.getSupportedMethods()));
    return new ResponseEntity<>(httpHeaders, HttpStatus.METHOD_NOT_ALLOWED);
  }

  @SuppressWarnings("unchecked")
  protected ResponseEntity<RpcFormResponse<T>> bindingErrorResponse(
      HttpServletRequest request,
      BindingResult bindingResult) {

    return new ResponseEntity<>(
        RpcFormResponse.<T>fromBindingResult(
            request,
            (T) bindingResult.getTarget(),
            bindingResult),
        HttpStatus.BAD_REQUEST);
  }
}
