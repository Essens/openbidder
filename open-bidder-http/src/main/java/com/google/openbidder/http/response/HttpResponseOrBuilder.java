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

import com.google.common.collect.Multimap;
import com.google.openbidder.http.message.HttpMessageOrBuilder;

import java.net.URI;
import java.util.Collection;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * Operations shared between {@link com.google.openbidder.http.HttpResponse}
 * and {@link com.google.openbidder.http.HttpResponse.Builder}.
 */
public interface HttpResponseOrBuilder extends HttpMessageOrBuilder {
  // status
  int getStatusCode();

  // redirect URI
  boolean hasRedirectUri();
  @Nullable URI getRedirectUri();
  @Nullable String getRedirectHost();
  @Nullable Integer getRedirectPort();
  @Nullable String getRedirectPath();
  @Nullable String getRedirectFragment();
  Multimap<String, String> getRedirectParameters();
  boolean containsRedirectParameter(String name);
  String getRedirectParameter(String name);
  Collection<String> getRedirectParameters(String name);
  Set<String> getRedirectParameterNames();
}
