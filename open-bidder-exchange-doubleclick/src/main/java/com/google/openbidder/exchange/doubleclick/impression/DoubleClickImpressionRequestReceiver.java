package com.google.openbidder.exchange.doubleclick.impression;

import com.google.doubleclick.crypto.DoubleClickCrypto;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.openbidder.api.impression.ImpressionController;
import com.google.openbidder.api.platform.Exchange;
import com.google.openbidder.config.impression.PriceName;
import com.google.openbidder.http.HttpRequest;
import com.google.openbidder.impression.ImpressionRequestReceiver;

import com.codahale.metrics.MetricRegistry;

import javax.annotation.Nullable;

/**
 * {@link ImpressionRequestReceiver} for DoubleClick Ad Exchange.
 */
@Singleton
public class DoubleClickImpressionRequestReceiver extends ImpressionRequestReceiver {
  private final DoubleClickCrypto.Price priceCrypto;
  private final String priceName;

  @Inject
  public DoubleClickImpressionRequestReceiver(
      Exchange exchange, MetricRegistry metricRegistry, ImpressionController controller,
      @Nullable DoubleClickCrypto.Price priceCrypto,
      @PriceName @Nullable String priceName) {
    super(exchange, metricRegistry, controller);
    this.priceCrypto = priceCrypto;
    this.priceName = priceName;
  }

  @Override
  protected DoubleClickImpressionRequest newRequest(HttpRequest httpRequest) {
    return DoubleClickImpressionRequest.newBuilder()
        .setExchange(getExchange())
        .setHttpRequest(httpRequest)
        .setPriceCrypto(priceCrypto)
        .setPriceName(priceName)
        .build();
  }
}
