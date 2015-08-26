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

package com.google.openbidder.exchange.doubleclick.match;

import com.google.openbidder.api.match.MatchRequest;
import com.google.openbidder.api.platform.Exchange;
import com.google.openbidder.exchange.doubleclick.DoubleClickConstants;
import com.google.openbidder.http.HttpRequest;
import com.google.openbidder.http.request.HttpRequestOrBuilder;

import javax.annotation.Nullable;

/**
 * DoubleClick Ad Exchange {@link MatchRequest} for handling cookie match and pixel match requests.
 */
public class DoubleClickMatchRequest extends MatchRequest {

  /**
   * Creates a pixel-matching request.
   *
   * @param httpRequest HTTP request
   */
  protected DoubleClickMatchRequest(HttpRequest httpRequest) {
    super(DoubleClickConstants.EXCHANGE, httpRequest);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  @Override
  public Builder toBuilder() {
    return newBuilder()
        .setExchange(getExchange())
        .setHttpRequest(httpRequest());
  }

  @Override
  public final String getUserId() {
    return httpRequest().getParameter(DoubleClickMatchTag.GOOGLE_GID);
  }

  public final boolean isPush() {
    return httpRequest().getParameters().containsKey(DoubleClickMatchTag.GOOGLE_PUSH);
  }

  public final String getPushData() {
    return httpRequest().getParameter(DoubleClickMatchTag.GOOGLE_PUSH);
  }

  public final @Nullable Long getCookieVersion() {
    String version = httpRequest().getParameter(DoubleClickMatchTag.GOOGLE_COOKIE_VERSION);
    return version == null ? null : Long.valueOf(version);
  }

  /**
   * Builder for {@link DoubleClickMatchRequest}.
   */
  public static class Builder extends MatchRequest.Builder {
    protected Builder() {
    }

    @Override
    protected Exchange defaultExchange() {
      return DoubleClickConstants.EXCHANGE;
    }

    @Override
    public DoubleClickMatchRequest build() {
      return new DoubleClickMatchRequest(builtHttpRequest());
    }

    // Overrides for covariance
    @Override public Builder setExchange(@Nullable Exchange exchange) {
      super.setExchange(exchange);
      return this;
    }
    @Override public Builder setHttpRequest(@Nullable HttpRequestOrBuilder httpRequest) {
      super.setHttpRequest(httpRequest);
      return this;
    }
  }
}
