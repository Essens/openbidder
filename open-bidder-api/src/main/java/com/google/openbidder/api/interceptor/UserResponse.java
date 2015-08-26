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
import com.google.openbidder.http.HttpResponse;

import org.apache.http.HttpStatus;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Open Bidder response to the exchange / user-agent.
 *
 * @see Interceptor
 */
public abstract class UserResponse<R extends UserResponse<?>> {
  private final Exchange exchange;
  private final HttpResponse.Builder httpResponse;
  private final Map<String, Object> metadata = new LinkedHashMap<>();

  protected UserResponse(Exchange exchange, HttpResponse.Builder httpResponseBuilder) {
    this.exchange = checkNotNull(exchange);
    this.httpResponse = checkNotNull(httpResponseBuilder);
  }

  @SuppressWarnings("unchecked")
  protected final R self() {
    return (R)this;
  }

  public abstract Builder<?> toBuilder();

  /**
   * @return Exchange for which the bid response is destinated
   */
  public Exchange getExchange() {
    return exchange;
  }

  /**
   * @return HTTP response sink
   */
  public final HttpResponse.Builder httpResponse() {
    return httpResponse;
  }

  /**
   * @return the metadata map. Returns the internal, mutable map.
   */
  public final Map<String, Object> metadata() {
    return metadata;
  }

  public R putMetadata(String key, Object value) {
    metadata.put(key, value);
    return self();
  }

  public R putAllMetadata(Map<String, Object> metadata) {
    this.metadata.putAll(metadata);
    return self();
  }

  protected ToStringHelper toStringHelper() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("exchange", exchange)
        .add("metadata", metadata);
  }

  @Override
  public final String toString() {
    return toStringHelper().toString();
  }

  /**
   * Builder for {@link UserResponse}.
   */
  public static abstract class Builder<B extends Builder<B>> {
    private Exchange exchange;
    private HttpResponse.Builder httpResponse;

    protected final B self() {
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

    public B setHttpResponse(@Nullable HttpResponse.Builder httpResponse) {
      this.httpResponse = httpResponse;
      return self();
    }

    public @Nullable HttpResponse.Builder getHttpResponse() {
      return httpResponse;
    }

    protected Exchange defaultExchange() {
      return NoExchange.INSTANCE;
    }

    protected int defaultStatusCode() {
      return HttpStatus.SC_OK;
    }

    public abstract UserResponse<?> build();

    protected ToStringHelper toStringHelper() {
      return MoreObjects.toStringHelper(this).omitNullValues()
          .add("exchange", exchange)
          .add("httpResponse", httpResponse);
    }

    @Override public final String toString() {
      return toStringHelper().toString();
    }
  }
}
