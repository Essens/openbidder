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

package com.google.openbidder.api.impression;

import com.google.common.base.MoreObjects;
import com.google.openbidder.api.interceptor.UserResponse;
import com.google.openbidder.api.platform.Exchange;
import com.google.openbidder.http.HttpResponse;

/**
 * A tracking response for impressions.
 */
public class ImpressionResponse extends UserResponse<ImpressionResponse> {

  /**
   * Creates an impression response.
   */
  protected ImpressionResponse(Exchange exchange, HttpResponse.Builder httpResponseBuilder) {
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
   * Builder for {@link ImpressionResponse}.
   */
  public static class Builder extends UserResponse.Builder<Builder> {
    protected Builder() {
    }

    @Override public ImpressionResponse build() {
      return new ImpressionResponse(
          MoreObjects.firstNonNull(getExchange(), defaultExchange()),
          getHttpResponse());
    }
  }
}
