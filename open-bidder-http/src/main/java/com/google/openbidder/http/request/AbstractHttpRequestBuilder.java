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

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.Multimap;
import com.google.openbidder.http.HttpRequest;
import com.google.openbidder.http.message.AbstractHttpMessageBuilder;
import com.google.openbidder.http.util.HttpUtil;

import java.util.Map.Entry;

/**
 * Base implementation for {@link com.google.openbidder.http.HttpRequest.Builder}.
 */
public abstract class AbstractHttpRequestBuilder
extends AbstractHttpMessageBuilder<HttpRequest.Builder> implements HttpRequest.Builder {

  @Override
  public final HttpRequest.Builder setParameter(String name, String value) {
    return removeParameter(name).addParameter(name, value);
  }

  @Override
  public final HttpRequest.Builder setParameter(String name, Iterable<String> values) {
    return removeParameter(name).addParameter(name, values);
  }

  @Override
  public final HttpRequest.Builder addParameter(String name, Iterable<String> values) {
    for (String value : values) {
      addParameter(name, value);
    }
    return this;
  }

  @Override
  public final HttpRequest.Builder addAllParameter(Multimap<String, String> parameters) {
    for (Entry<String, String> entry : parameters.entries()) {
      addParameter(entry.getKey(), entry.getValue());
    }
    return this;
  }

  @Override
  public final HttpRequest.Builder setAllParameter(Multimap<String, String> parameters) {
    return clearParameter().addAllParameter(parameters);
  }

  @Override
  public final HttpRequest.Builder setUri(String uri) {
    return setUri(HttpUtil.buildUri(uri));
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
