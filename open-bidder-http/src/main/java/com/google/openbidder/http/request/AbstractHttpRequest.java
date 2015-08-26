/*
 * Copyright 2013 Google Inc. All Rights Reserved.
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

package com.google.openbidder.http.request;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.api.client.util.escape.CharEscapers;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.openbidder.http.HttpRequest;
import com.google.openbidder.http.message.AbstractHttpMessage;

import javax.annotation.Nullable;

/**
 * Base implementation of {@link HttpRequest}.
 */
public abstract class AbstractHttpRequest
extends AbstractHttpMessage implements HttpRequest {
  private final static String PROTOCOL_HTTPS = "https";

  @Nullable
  @Override
  public final String getParameter(String name) {
    return Iterables.getOnlyElement(getParameters(name), /* default value  */ null);
  }

  @Override
  public final ImmutableCollection<String> getParameters(String name) {
    return getParameters().get(name);
  }

  @Override
  public final ImmutableSet<String> getParameterNames() {
    return getParameters().keySet();
  }

  @Override
  public final boolean isSecure() {
    return PROTOCOL_HTTPS.equals(getUri().getScheme());
  }

  @Override
  public final @Nullable String getParameterDecoded(String name) {
    String value = getParameter(name);
    return value == null ? null : CharEscapers.decodeUri(value);
  }

  @Override
  public final @Nullable String getParameterDecoded2(String name) {
    String value = getParameter(name);
    return value == null ? null : CharEscapers.decodeUri(CharEscapers.decodeUri(value));
  }

  @Override
  public final String getServerName() {
    String host = getHeader("Host");
    if (host == null) {
      return null;
    } else {
      int pos = host.lastIndexOf(':');
      return pos == -1 ? host : host.substring(0, pos);
    }
  }

  @Override
  public final int getServerPort() {
    String host = checkNotNull(getHeader("Host"));
    int pos = host.lastIndexOf(':');
    return pos == -1
        ? (isSecure() ? 443 : 80)
        : Integer.parseInt(host.substring(pos + 1));
  }

  @Override
  public HttpRequest.Builder toBuilder() {
    return createBuilder()
        .setProtocol(getProtocol())
        .setMethod(getMethod())
        .setUri(getUri())
        .setAllHeader(getHeaders())
        .setAllParameter(getParameters())
        .setAllCookie(getCookies().values());
  }

  protected HttpRequest.Builder createBuilder() {
    return StandardHttpRequest.newBuilder();
  }

  @Override
  protected ToStringHelper toStringHelper() {
    return super.toStringHelper()
        .add("protocol", getProtocol())
        .add("method", getMethod())
        .add("uri", getUri())
        .add("remoteAddress", getRemoteAddress())
        .add("parameters", getParameters().isEmpty() ? null : getParameters());
  }
}
