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

package com.google.openbidder.api.interceptor;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.openbidder.api.platform.Exchange;
import com.google.openbidder.api.platform.NoExchange;
import com.google.openbidder.http.HttpRequest;
import com.google.openbidder.http.request.HttpRequestOrBuilder;
import com.google.openbidder.http.util.HttpUtil;

import javax.annotation.Nullable;

/**
 * Open Bidder request from the user-agent / exchange.
 *
 * @see Interceptor
 */
public abstract class UserRequest {
  private final Exchange exchange;
  private final HttpRequest httpRequest;

  protected UserRequest(Exchange exchange, HttpRequest httpRequest) {
    this.exchange = checkNotNull(exchange);
    this.httpRequest = checkNotNull(httpRequest);
  }

  public abstract Builder<?> toBuilder();

  /**
   * Returns the transport request.
   */
  public HttpRequest httpRequest() {
    return httpRequest;
  }

  /**
   * @return Exchange from which the bid request originated
   */
  public final Exchange getExchange() {
    return exchange;
  }

  protected ToStringHelper toStringHelper() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("exchange", exchange);
  }

  @Override
  public final String toString() {
    return toStringHelper().toString();
  }

  /**
   * Builder for {@link UserRequest}.
   */
  public static abstract class Builder<B extends Builder<B>> {
    private Exchange exchange;
    private HttpRequestOrBuilder httpRequest;

    protected B self() {
      @SuppressWarnings("unchecked")
      B self = (B) this;
      return self;
    }

    public B setExchange(@Nullable Exchange exchange) {
      this.exchange = exchange;
      return self();
    }

    public @Nullable Exchange getExchange() {
      return exchange;
    }

    public B setHttpRequest(@Nullable HttpRequestOrBuilder httpRequest) {
      this.httpRequest = httpRequest;
      return self();
    }

    public @Nullable HttpRequestOrBuilder getHttpRequest() {
      return httpRequest;
    }

    protected final HttpRequest builtHttpRequest() {
      return HttpUtil.built(checkNotNull(httpRequest));
    }

    protected Exchange defaultExchange() {
      return NoExchange.INSTANCE;
    }

    public abstract UserRequest build();

    protected ToStringHelper toStringHelper() {
      return MoreObjects.toStringHelper(this).omitNullValues()
          .add("exchange", exchange)
          .add("httpRequest", httpRequest);
    }

    @Override public final String toString() {
      return toStringHelper().toString();
    }
  }
}
