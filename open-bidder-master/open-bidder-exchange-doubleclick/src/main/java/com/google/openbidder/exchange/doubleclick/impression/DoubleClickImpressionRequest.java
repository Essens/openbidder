package com.google.openbidder.exchange.doubleclick.impression;

import com.google.common.base.Strings;
import com.google.doubleclick.crypto.DoubleClickCrypto;
import com.google.openbidder.api.impression.ImpressionRequest;
import com.google.openbidder.api.platform.Exchange;
import com.google.openbidder.exchange.doubleclick.DoubleClickConstants;
import com.google.openbidder.http.HttpRequest;
import com.google.openbidder.http.request.HttpRequestOrBuilder;

import java.security.SignatureException;

import javax.annotation.Nullable;

/**
 * DoubleClick Ad Exchange {@link ImpressionRequest} for handling winning price.
 */
public class DoubleClickImpressionRequest extends ImpressionRequest {
  private final DoubleClickCrypto.Price priceCrypto;

  protected DoubleClickImpressionRequest(
      Exchange exchange, HttpRequest httpRequest,
      @Nullable String priceName,
      @Nullable DoubleClickCrypto.Price priceCrypto) {
    super(exchange, httpRequest, priceName);

    this.priceCrypto = priceCrypto;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  @Override
  public Builder toBuilder() {
    return newBuilder()
        .setExchange(getExchange())
        .setHttpRequest(httpRequest())
        .setPriceName(getPriceName())
        .setPriceCrypto(priceCrypto);
  }

  @Override
  protected double decodePrice(String encodedPrice) {
    if (priceCrypto == null) {
      throw new IllegalStateException("Not configured for price decryption (missing keys?)");
    }

    if (!Strings.isNullOrEmpty(encodedPrice)
        && !DoubleClickConstants.PRICE_UNKNOWN.equals(encodedPrice)) {
      try {
        return priceCrypto.decodePriceValue(encodedPrice);
      } catch (SignatureException e) {
        throw new IllegalStateException(e);
      }
    } else {
      return 0;
    }
  }

  /**
   * Builder for {@link DoubleClickImpressionRequest}.
   */
  public static class Builder extends ImpressionRequest.Builder {
    private DoubleClickCrypto.Price priceCrypto;

    protected Builder() {
    }

    @Override
    protected Exchange defaultExchange() {
      return DoubleClickConstants.EXCHANGE;
    }

    @Override
    protected Builder self() {
      return this;
    }

    public Builder setPriceCrypto(@Nullable DoubleClickCrypto.Price priceCrypto) {
      this.priceCrypto = priceCrypto;
      return self();
    }

    public @Nullable DoubleClickCrypto.Price getPriceCrypto() {
      return priceCrypto;
    }

    // Overrides for covariance
    @Override public Builder setPriceName(@Nullable String priceName) {
      return (Builder) super.setPriceName(priceName);
    }
    @Override
    public Builder setExchange(@Nullable Exchange exchange) {
      return (Builder) super.setExchange(exchange);
    }
    @Override public Builder setHttpRequest(@Nullable HttpRequestOrBuilder httpRequest) {
      return (Builder) super.setHttpRequest(httpRequest);
    }

    @Override
    public DoubleClickImpressionRequest build() {
      return new DoubleClickImpressionRequest(
          getExchange(), builtHttpRequest(),
          getPriceName(), priceCrypto);
    }
  }
}
