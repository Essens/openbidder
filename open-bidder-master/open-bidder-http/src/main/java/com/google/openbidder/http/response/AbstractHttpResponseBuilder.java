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

package com.google.openbidder.http.response;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.openbidder.http.HttpResponse;
import com.google.openbidder.http.message.AbstractHttpMessageBuilder;
import com.google.openbidder.http.util.HttpUtil;

import org.apache.http.HttpStatus;

import java.util.Collection;
import java.util.Set;

public abstract class AbstractHttpResponseBuilder
extends AbstractHttpMessageBuilder<HttpResponse.Builder> implements HttpResponse.Builder {

  @Override
  public final HttpResponse.Builder setRedirectUri(String uri) {
    return setRedirectUri(HttpUtil.buildUri(uri));
  }

  @Override
  public final HttpResponse.Builder setStatusOk() {
    return setStatusCode(HttpStatus.SC_OK);
  }

  @Override
  public final HttpResponse.Builder setRedirectParameter(String name, String value) {
    return removeRedirectParameter(name).addRedirectParameter(name, value);
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
  public final Collection<String> getRedirectParameters(String name) {
    return getRedirectParameters().get(name);
  }

  @Override
  public final Set<String> getRedirectParameterNames() {
    return getRedirectParameters().keySet();
  }

  @Override
  public final AbstractHttpResponseBuilder addRedirectParameter(String name, String value) {
    redirectParameters().put(name, value);
    return this;
  }

  @Override
  public final AbstractHttpResponseBuilder removeRedirectParameter(String name) {
    redirectParameters().removeAll(name);
    return this;
  }

  @Override
  public final AbstractHttpResponseBuilder removeRedirectParameter(String name, String value) {
    redirectParameters().remove(name, value);
    return this;
  }

  @Override
  public final AbstractHttpResponseBuilder clearRedirectParameter() {
    redirectParameters().clear();
    return this;
  }

  protected abstract Multimap<String, String> redirectParameters();

  @Override
  protected ToStringHelper toStringHelper() {
    return super.toStringHelper()
        .add("statusCode", getStatusCode())
        .add("redirectUri", getRedirectUri());
  }
}
