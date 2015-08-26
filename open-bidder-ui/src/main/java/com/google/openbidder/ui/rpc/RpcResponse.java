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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.support.RequestContext;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

/**
 * Generic RPC response with arbitrary payload (response).
 */
public class RpcResponse<T> {

  @Nullable
  private T response;

  @JsonIgnore
  @Nullable
  private final RequestContext requestContext;

  private final List<RpcMessage> messages = new ArrayList<>();

  private RpcResponse(HttpServletRequest request) {
    this(new RequestContext(checkNotNull(request)));
  }

  private RpcResponse() {
    this((RequestContext) null);
  }

  private RpcResponse(RequestContext requestContext) {
    this.requestContext = requestContext;
  }

  @Nullable
  public T getResponse() {
    return response;
  }

  public RpcResponse<T> setResponse(@Nullable T response) {
    this.response = response;
    return this;
  }

  public List<RpcMessage> getMessages() {
    return messages;
  }

  /**
   * All messages are HTML escaped.
   */
  public RpcResponse<T> message(RpcMessageType messageType, String messageCode,
      Object... arguments) {
    checkNotNull(requestContext);
    String message = requestContext.getMessage(messageCode, arguments, true);
    messages.add(new RpcMessage(messageType, message));
    return this;
  }

  public RpcResponse<T> info(String messageCode, Object... arguments) {
    return message(RpcMessageType.INFO, messageCode, arguments);
  }

  public RpcResponse<T> success(String messageCode, Object... arguments) {
    return message(RpcMessageType.SUCCESS, messageCode, arguments);
  }

  public RpcResponse<T> error(String messageCode, Object... arguments) {
    return message(RpcMessageType.ERROR, messageCode, arguments);
  }

  public RpcResponse<T> exception(Exception e, boolean includeStackTrace) {
    messages.add(new RpcMessage(RpcMessageType.ERROR, e.getMessage(),
        includeStackTrace ? RpcUtil.stackTraceAsString(e) : null));
    return this;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("response", response)
        .add("messages", messages)
        .toString();
  }

  public static RpcResponse<Void> sendSuccessEmpty(
      HttpServletRequest request,
      String messageCode,
      Object... arguments) {

    return new RpcResponse<Void>(request).success(messageCode, arguments);
  }

  public static <T> RpcResponse<T> sendResponseNoMessage(@Nullable T response) {
    return new RpcResponse<T>().setResponse(response);
  }

  public static <T> RpcResponse<T> sendSuccess(
      HttpServletRequest request,
      @Nullable T response,
      String messageCode,
      Object... arguments) {

    return new RpcResponse<T>(request).setResponse(response).success(messageCode, arguments);
  }

  public static <T> RpcResponse<T> sendError(
      HttpServletRequest request,
      String messageCode,
      Object... arguments) {

    return new RpcResponse<T>(request).error(messageCode, arguments);
  }

  public static <T> RpcResponse<T> sendException(
      HttpServletRequest request,
      Exception e,
      boolean includeStackTrace) {

    return new RpcResponse<T>(request).exception(e, includeStackTrace);
  }

  public static <T> RpcResponse<RpcFormResponse<T>> sendFormResponse(
      HttpServletRequest request,
      BindingResult bindingResult,
      String messageCode,
      Object... arguments) {

    return sendFormResponse(request, null, bindingResult, messageCode, arguments);
  }

  public static <T> RpcResponse<RpcFormResponse<T>> sendFormResponse(
      HttpServletRequest request,
      T response,
      BindingResult bindingResult,
      String messageCode,
      Object... arguments) {

    RpcFormResponse<T> formResponse = RpcFormResponse.fromBindingResult(
        request, response, bindingResult);
    RpcResponse<RpcFormResponse<T>> rpcResponse = new RpcResponse<>(request);
    if (bindingResult.hasErrors()) {
      rpcResponse.error(messageCode, arguments);
    } else {
      rpcResponse.success(messageCode, arguments);
    }
    return rpcResponse.setResponse(formResponse);
  }
}
