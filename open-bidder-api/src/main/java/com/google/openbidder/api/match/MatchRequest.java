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

package com.google.openbidder.api.match;

import com.google.common.base.MoreObjects;
import com.google.openbidder.api.interceptor.UserRequest;
import com.google.openbidder.api.platform.Exchange;
import com.google.openbidder.http.HttpRequest;

import javax.annotation.Nullable;

/**
 * Pixel-matching request.
 */
public class MatchRequest extends UserRequest {

  /**
   * Creates a matching request.
   *
   * @param exchange Exchange from which the bid request originated
   * @param httpRequest Source HTTP request
   */
  protected MatchRequest(Exchange exchange, HttpRequest httpRequest) {
    super(exchange, httpRequest);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  @Override public Builder toBuilder() {
    return newBuilder()
        .setExchange(getExchange())
        .setHttpRequest(httpRequest());
  }

  /**
   * @return User ID from the exchange
   */
  @Nullable public String getUserId() {
    return null;
  }

  /**
   * Builder for {@link MatchRequest}.
   */
  public static class Builder extends UserRequest.Builder<Builder> {
    protected Builder() {
    }

    @Override public MatchRequest build() {
      return new MatchRequest(
          MoreObjects.firstNonNull(getExchange(), defaultExchange()),
          builtHttpRequest());
    }
  }
}
