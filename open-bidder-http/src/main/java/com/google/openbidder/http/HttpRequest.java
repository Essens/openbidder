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

package com.google.openbidder.http;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.openbidder.http.request.HttpRequestOrBuilder;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * Implementation independent view of an HTTP request
 */
public interface HttpRequest extends HttpMessage, HttpRequestOrBuilder {

  /**
   * Returns the single value for a parameter name, or {@code null} if no such parameter exists.
   * Both parameter name and value are considered non-encoded, they will be returned as-is.
   *
   * @see #getParameters(String) for multi-valued parameters
   * @throws IllegalArgumentException if the parameter is multi-valued
   */
  @Nullable String getParameter(String name);

  /**
   * @return All parameters with the given name with values as Strings
   */
  ImmutableCollection<String> getParameters(String name);

  /**
   * @return All parameter names
   */
  Set<String> getParameterNames();

  /**
   * @return The server name, or {@code null} if the Host header is absent.
   */
  @Nullable String getServerName();

  /**
   * @return The server port. This may be derived from the Host header, or from
   * the protocol's default port.
   *
   * @throws NullPointerException if the header Host is absent.
   */
  int getServerPort();

  /**
   * @return {@code true} for HTTPS, otherwise {@code false}
   */
  boolean isSecure();

  /**
   * Decodes and returns a URL-encoded parameter from the transport request, or {@code null}
   * if it doesn't exist. The parameter name should not be encoded.
   *
   * @see #getParameter(String) if the value is unencoded
   * @see #getParameterDecoded2(String) if the value is doubly-URL-encoded
   * @throws IllegalStateException if this parameter has multiple values.
   */
  @Nullable String getParameterDecoded(String name);

  /**
   * Decodes and returns a double-URL-encoded parameter from the transport request,
   * or <code>null</code> if it doesn't exist.
   *
   * @see #getParameter(String) if the value is unencoded
   * @see #getParameterDecoded(String) if the value is single-URL-encoded
   * @throws IllegalStateException if this parameter has multiple values.
   */
  @Nullable String getParameterDecoded2(String name);

  // Overrides for covariance
  @Override abstract ImmutableMultimap<String, String> getParameters();
  @Override Builder toBuilder();

  /**
   * Builds a {@link HttpRequest}.
   */
  interface Builder extends HttpRequestOrBuilder, HttpMessage.Builder<HttpRequest.Builder> {

    HttpRequest.Builder addParameter(String name, String value);
    HttpRequest.Builder addParameter(String name, Iterable<String> values);
    HttpRequest.Builder setParameter(String name, String value);
    HttpRequest.Builder setParameter(String name, Iterable<String> values);
    HttpRequest.Builder addAllParameter(Multimap<String, String> parameters);
    HttpRequest.Builder setAllParameter(Multimap<String, String> parameters);
    HttpRequest.Builder removeParameter(String name, String value);
    HttpRequest.Builder removeParameter(String name);
    HttpRequest.Builder clearParameter();

    HttpRequest.Builder setProtocol(Protocol protocol);
    HttpRequest.Builder setMethod(String method);

    HttpRequest.Builder setUri(String uri);
    HttpRequest.Builder setUri(URI uri);

    HttpRequest.Builder setRemoteAddress(InetSocketAddress remoteAddress);

    // Overrides for covariance
    @Override HttpRequest build();
  }
}
