/*
 * Copyright 2014 Google Inc. All Rights Reserved.
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

package com.google.openbidder.http.response;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.openbidder.http.HttpResponse;
import com.google.openbidder.http.HttpStatusType;
import com.google.openbidder.http.message.AbstractHttpMessage;

/**
 * Base implementation of {@link HttpResponse}.
 */
public abstract class AbstractHttpResponse
extends AbstractHttpMessage implements HttpResponse {
  protected void validate() {
    checkState(isRedirect() || (getRedirectUri() == null && getRedirectParameters().isEmpty()),
        "Response with redirect URI or parameters must have redirect status");
  }

  @Override
  public final boolean isOk() {
    return HttpStatusType.SUCCESS.contains(getStatusCode());
  }

  @Override
  public final boolean isRedirect() {
    return HttpStatusType.REDIRECT.contains(getStatusCode());
  }

  @Override
  public final boolean isValidRedirect() {
    return isRedirect() && hasRedirectUri();
  }

  @Override
  public final boolean isClientError() {
    return HttpStatusType.CLIENT_ERROR.contains(getStatusCode());
  }

  @Override
  public final boolean isServerError() {
    return HttpStatusType.SERVER_ERROR.contains(getStatusCode());
  }

  @Override
  public final boolean isError() {
    return isClientError() || isServerError();
  }

  @Override
  public final boolean containsRedirectParameter(String name) {
    return getRedirectParameterNames().contains(name);
  }

  @Override
  public final String getRedirectParameter(String name) {
    return Iterables.getFirst(getRedirectParameters(name), /* default value */ null);
  }

  @Override
  public final ImmutableCollection<String> getRedirectParameters(String name) {
    return getRedirectParameters().get(name);
  }

  @Override
  public final ImmutableSet<String> getRedirectParameterNames() {
    return getRedirectParameters().keySet();
  }

  @Override
  public final String getRedirectHost() {
    return hasRedirectUri() ? getRedirectUri().getHost() : null;
  }

  @Override
  public final Integer getRedirectPort() {
    return hasRedirectUri() ? getRedirectUri().getPort() : -1;
  }

  @Override
  public final String getRedirectPath() {
    return hasRedirectUri() ? getRedirectUri().getPath() : null;
  }

  @Override
  public final String getRedirectFragment() {
    return hasRedirectUri() ? getRedirectUri().getFragment() : null;
  }

  @Override
  public HttpResponse.Builder toBuilder() {
    return createBuilder()
        .setStatusCode(getStatusCode())
        .setAllHeader(getHeaders())
        .setAllCookie(getCookies().values())
        .setRedirectUri(getRedirectUri());
  }

  protected HttpResponse.Builder createBuilder() {
    return StandardHttpResponse.newBuilder();
  }

  @Override
  protected ToStringHelper toStringHelper() {
    return super.toStringHelper()
        .add("statusCode", getStatusCode())
        .add("redirectUri", getRedirectUri());
  }
}
