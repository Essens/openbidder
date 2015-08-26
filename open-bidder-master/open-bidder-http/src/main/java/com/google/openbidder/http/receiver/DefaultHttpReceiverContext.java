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

package com.google.openbidder.http.receiver;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.openbidder.http.HttpReceiverContext;
import com.google.openbidder.http.HttpRequest;
import com.google.openbidder.http.HttpResponse;

import java.util.HashMap;
import java.util.Map;

public class DefaultHttpReceiverContext implements HttpReceiverContext {

  private final HttpRequest httpRequest;
  private final HttpResponse.Builder httpResponse;
  private Map<String, Object> attributes;

  public DefaultHttpReceiverContext(HttpRequest httpRequest, HttpResponse.Builder httpResponse) {
    this.httpRequest = Preconditions.checkNotNull(httpRequest);
    this.httpResponse = Preconditions.checkNotNull(httpResponse);
  }

  @Override
  public HttpRequest httpRequest() {
    return httpRequest;
  }

  @Override
  public HttpResponse.Builder httpResponse() {
    return httpResponse;
  }

  @Override
  public Map<String, Object> attributes() {
    if (attributes == null) {
      attributes = new HashMap<>();
    }
    return attributes;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("httpRequest", httpRequest)
        .add("httpResponse", httpResponse)
        .add("attributes", attributes == null || attributes.isEmpty() ? null : attributes)
        .toString();
  }
}
