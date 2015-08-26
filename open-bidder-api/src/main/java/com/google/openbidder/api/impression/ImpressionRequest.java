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
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.openbidder.api.interceptor.UserRequest;
import com.google.openbidder.api.platform.Exchange;
import com.google.openbidder.http.HttpRequest;

import javax.annotation.Nullable;

/**
 * A tracking request for impressions.
 */
public class ImpressionRequest extends UserRequest {
  private final String priceName;
  private Double priceValue;

  /**
   * Creates an impression request.
   *
   * @param exchange Exchange from which the bid request originated
   * @param httpRequest Source HTTP request
   */
  protected ImpressionRequest(
      Exchange exchange, HttpRequest httpRequest,
      @Nullable String priceName) {
    super(exchange, httpRequest);
    this.priceName = priceName;
  }

  public static Builder newBuilder() {
    return new ImpressionRequest.Builder();
  }

  @Override
  public Builder toBuilder() {
    return newBuilder()
        .setExchange(getExchange())
        .setHttpRequest(httpRequest())
        .setPriceName(priceName);
  }

  /**
   * Returns {@code true} if this request supports retrieving the winning price.
   */
  public final boolean hasPrice() {
    return priceName != null && httpRequest().getParameters().containsKey(priceName);
  }

  /**
   * @return the winning price, decoded from the default parameter.
   * Decoding/decryption will be performed only once and cached.
   * @throws UnsupportedOperationException see {@link #hasPrice()}
   */
  public final double getPriceValue() {
    if (priceValue == null) {
      priceValue = getPriceValue(priceName);
    }

    return priceValue;
  }

  /**
   * @param priceName Name for a parameter containing the encoded winning price
   * @return the winning price, decoded from a specific parameter.
   * Decoding/decryption will be performed for every call.
   */
  public final double getPriceValue(String priceName) {
    String encodedPrice = httpRequest().getParameter(priceName);

    if (encodedPrice == null) {
      throw new IllegalStateException("Price parameter missing: " + priceName);
    }

    return decodePrice(encodedPrice);
  }

  protected double decodePrice(String encodedPrice) {
    return Double.parseDouble(encodedPrice);
  }

  public final String getPriceName() {
    return priceName;
  }

  @Override
  protected ToStringHelper toStringHelper() {
    return super.toStringHelper()
        .add("priceName", priceName);
  }

  /**
   * Builder for {@link ImpressionRequest}.
   */
  public static class Builder extends UserRequest.Builder<Builder> {
    private String priceName;

    protected Builder() {
    }

    public Builder setPriceName(@Nullable String priceName) {
      this.priceName = priceName;
      return self();
    }

    public @Nullable String getPriceName() {
      return priceName;
    }

    @Override public ImpressionRequest build() {
      return new ImpressionRequest(MoreObjects.firstNonNull(getExchange(), defaultExchange()),
          builtHttpRequest(),
          priceName);
    }

    @Override protected ToStringHelper toStringHelper() {
      return super.toStringHelper()
          .add("priceName", priceName);
    }
  }
}
