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
import com.google.common.collect.ImmutableSet;
import com.google.openbidder.http.response.HttpResponseOrBuilder;

import java.net.URI;

/**
 * Implementation independent view of an HTTP response
 */
public interface HttpResponse extends HttpMessage, HttpResponseOrBuilder {

  // status
  boolean isOk();
  boolean isRedirect();
  boolean isValidRedirect();
  boolean isError();
  boolean isClientError();
  boolean isServerError();

  // Overrides for covariance
  @Override ImmutableMultimap<String, String> getRedirectParameters();
  @Override ImmutableCollection<String> getRedirectParameters(String name);
  @Override ImmutableSet<String> getRedirectParameterNames();
  @Override Builder toBuilder();

  /**
   * Builds a {@link HttpResponse}.
   */
  interface Builder extends HttpResponseOrBuilder, HttpMessage.Builder<HttpResponse.Builder> {

    // status
    HttpResponse.Builder setStatusCode(int statusCode);
    HttpResponse.Builder setStatusOk();

    // redirect URI
    HttpResponse.Builder setRedirectUri(URI uri);
    HttpResponse.Builder setRedirectUri(String uri);
    HttpResponse.Builder clearRedirectUri();
    HttpResponse.Builder setRedirectHost(String hostname);
    HttpResponse.Builder setRedirectPort(int port);
    HttpResponse.Builder setRedirectPath(String path);
    HttpResponse.Builder setRedirectFragment(String fragment);
    HttpResponse.Builder setRedirectParameter(String name, String value);
    HttpResponse.Builder addRedirectParameter(String name, String value);
    HttpResponse.Builder removeRedirectParameter(String name);
    HttpResponse.Builder removeRedirectParameter(String name, String value);
    HttpResponse.Builder clearRedirectParameter();

    // Overrides for covariance
    @Override HttpResponse build();
  }
}
