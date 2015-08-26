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

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.support.RequestContext;

import java.util.Map;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

/**
 * A special {@link RpcResponse} that includes form binding results.
 */
public class RpcFormResponse<T> {

  private static Function<FieldError, String> GET_FIELD_NAME =
      new Function<FieldError, String>() {
        @Override
        public String apply(FieldError fieldError) {
          return fieldError.getField();
        }
      };

  private final @Nullable T response;
  private final boolean hasErrors;
  private final ImmutableMap<String, RpcFieldMessage> fieldMessages;

  private RpcFormResponse(@Nullable T response, boolean hasErrors,
      Map<String, RpcFieldMessage> fieldMessages) {
    this.response = response;
    this.hasErrors = hasErrors;
    this.fieldMessages = ImmutableMap.copyOf(fieldMessages);
  }

  public @Nullable T getResponse() {
    return response;
  }

  public boolean isHasErrors() {
    return hasErrors;
  }

  public ImmutableMap<String, RpcFieldMessage> getFieldMessages() {
    return fieldMessages;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("response", response)
        .add("hasErrors", hasErrors)
        .add("fieldMessages", fieldMessages)
        .toString();
  }

  public static <T> RpcFormResponse<T> fromBindingResult(
      HttpServletRequest request,
      BindingResult bindingResult) {

    return fromBindingResult(request, null, bindingResult);
  }

  public static <T> RpcFormResponse<T> fromBindingResult(
      HttpServletRequest request,
      @Nullable T response,
      BindingResult bindingResult) {

    Multimap<String, FieldError> fieldErrors = Multimaps.index(
        bindingResult.getFieldErrors(), GET_FIELD_NAME);
    ImmutableMap.Builder<String, RpcFieldMessage> fieldMessages = ImmutableMap.builder();
    final RequestContext requestContext = new RequestContext(request);
    for (String fieldName : fieldErrors.keySet()) {
      fieldMessages.put(fieldName,
          new RpcFieldMessage(RpcFieldMessageType.ERROR,
              Iterables.transform(fieldErrors.get(fieldName), new Function<FieldError, String>() {
                @Override
                public String apply(FieldError fieldError) {
                  return requestContext.getMessage(fieldError);
                }
              })));
    }
    return new RpcFormResponse<>(response, bindingResult.hasErrors(), fieldMessages.build());
  }
}
