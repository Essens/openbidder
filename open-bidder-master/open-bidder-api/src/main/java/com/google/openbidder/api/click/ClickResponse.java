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

package com.google.openbidder.api.click;

import com.google.openbidder.api.interceptor.UserResponse;
import com.google.openbidder.api.platform.Exchange;
import com.google.openbidder.http.HttpResponse;
import com.google.openbidder.http.util.HttpUtil;

import org.apache.http.HttpStatus;

import java.net.URI;

/**
 * A tracking response for clicks.
 */
public class ClickResponse extends UserResponse<ClickResponse> {

  /**
   * Creates a click response.
   */
  protected ClickResponse(Exchange exchange, HttpResponse.Builder httpResponseBuilder) {
    super(exchange, httpResponseBuilder);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  @Override
  public Builder toBuilder() {
    return newBuilder()
        .setExchange(getExchange())
        .setHttpResponse(httpResponse().build().toBuilder());
  }

  /**
   * Gets the redirect (HTTP 302) location.
   */
  public final URI getRedirectLocation() {
    return httpResponse().getRedirectUri();
  }

  /**
   * Sets the redirect (HTTP 302) location.
   */
  public final ClickResponse setRedirectLocation(URI redirectLocation) {
    httpResponse().setRedirectUri(redirectLocation);
    httpResponse().setStatusCode(HttpStatus.SC_MOVED_TEMPORARILY);
    return self();
  }

  public final ClickResponse setRedirectLocation(String redirectLocation) {
    setRedirectLocation(HttpUtil.buildUri(redirectLocation));
    return self();
  }

  /**
   * Builder for {@link ClickResponse}.
   */
  public static class Builder extends UserResponse.Builder<Builder> {
    protected Builder() {
    }

    @Override
    public ClickResponse build() {
      return new ClickResponse(getExchange(), getHttpResponse());
    }
  }
}
