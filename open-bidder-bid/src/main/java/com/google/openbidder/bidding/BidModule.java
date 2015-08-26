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

package com.google.openbidder.bidding;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.openbidder.api.bidding.BidController;
import com.google.openbidder.api.bidding.BidInterceptor;
import com.google.openbidder.api.openrtb.ObExt.UrlParameter;
import com.google.openbidder.bidding.json.ObExtBidReader;
import com.google.openbidder.bidding.json.ObExtBidWriter;
import com.google.openbidder.bidding.json.ObExtImpReader;
import com.google.openbidder.bidding.json.ObExtImpressionWriter;
import com.google.openbidder.config.bid.BidInterceptors;
import com.google.openbidder.config.bid.CallbackUrl;
import com.google.openbidder.config.bid.ClickUrl;
import com.google.openbidder.config.bid.HasBid;
import com.google.openbidder.config.bid.ImpressionUrl;
import com.google.openbidder.config.http.Feature;
import com.google.openbidder.config.server.LoadBalancerHost;
import com.google.openbidder.config.server.LoadBalancerPort;
import com.google.openbidder.util.GuiceUtils;
import com.google.openrtb.OpenRtb.BidRequest;
import com.google.openrtb.OpenRtb.BidResponse;
import com.google.openrtb.json.OpenRtbJsonFactory;
import com.google.openrtb.snippet.OpenRtbSnippetProcessor;
import com.google.openrtb.snippet.SnippetProcessor;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.codahale.metrics.MetricRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

/**
 * Configure bid request support.
 */
@Parameters(separators = "=")
public class BidModule extends AbstractModule {
  private static final Logger logger = LoggerFactory.getLogger(BidModule.class);

  private static final String CALLBACK_URL_NO_PORT = "http://%s";
  private static final String CALLBACK_URL_WITH_PORT = "http://%s:%d";
  private static final int DEFAULT_HTTP_PORT = 80;

  @Parameter(names = "--bid_interceptors",
      description = "Comma separated list of bid interceptor classes to execute")
  private List<String> interceptors = new ArrayList<>();

  @Parameter(names = "--impression_url", description = "Impression callback URL")
  private String impressionUrl = ImpressionUrl.DEFAULT;

  @Parameter(names = "--click_url", description = "Impression callback URL")
  private String clickUrl = ClickUrl.DEFAULT;

  @Override
  protected void configure() {
    logger.info("Bid interceptors: {}", interceptors);
    boolean hasBid = !interceptors.isEmpty();
    bind(boolean.class).annotatedWith(HasBid.class).toInstance(hasBid);
    bind(new TypeLiteral<ImmutableList<String>>() {}).annotatedWith(BidInterceptors.class)
        .toInstance(ImmutableList.copyOf(interceptors));
    if (hasBid) {
      Multibinder.newSetBinder(binder(), Service.class).addBinding()
          .to(BidController.class).in(Scopes.SINGLETON);
      Multibinder.newSetBinder(binder(), Feature.class).addBinding().toInstance(Feature.BID);
      bind(SnippetProcessor.class).to(OpenRtbSnippetProcessor.class)
          .in(Scopes.SINGLETON);
    }

    logger.info("Impression callback URL: {}", impressionUrl);
    bind(String.class).annotatedWith(ImpressionUrl.class).toInstance(impressionUrl);

    logger.info("Click callback URL: {}", clickUrl);
    bind(String.class).annotatedWith(ClickUrl.class).toInstance(clickUrl);
  }

  @Provides
  @Singleton
  @CallbackUrl
  public String provideCallbackUrl(@LoadBalancerHost String host, @LoadBalancerPort int port) {
    String callbackUrl = host.isEmpty()
        ? ""
        : port == DEFAULT_HTTP_PORT
            ? String.format(CALLBACK_URL_NO_PORT, host)
            : String.format(CALLBACK_URL_WITH_PORT, host, port);
    logger.info("Callback URL: {}", callbackUrl);
    return callbackUrl;
  }

  @Provides
  @Singleton
  @BidInterceptors
  public ImmutableList<? extends BidInterceptor> provideBidInterceptors(
      Injector injector,
      @BidInterceptors ImmutableList<String> classNames) {

    return GuiceUtils.loadInstances(injector, BidInterceptor.class, classNames);
  }

  @Provides
  @Singleton
  public BidController provideBidController(
      MetricRegistry metricRegistry,
      @BidInterceptors ImmutableList<? extends BidInterceptor> interceptors) {

    return new BidController(interceptors, metricRegistry);
  }

  public static OpenRtbJsonFactory registerObExt(OpenRtbJsonFactory factory) {
    return factory
        .register(new ObExtImpReader(), BidRequest.Imp.Builder.class)
        .register(new ObExtImpressionWriter(), String.class, BidRequest.Imp.class)
        .register(new ObExtBidReader(), BidResponse.SeatBid.Bid.Builder.class)
        .register(new ObExtBidWriter.ClickThroughUrl(), String.class, BidResponse.SeatBid.Bid.class)
        .register(new ObExtBidWriter.ImpressionParameter(),
            UrlParameter.class, BidResponse.SeatBid.Bid.class, "bidImpressionParameter")
        .register(new ObExtBidWriter.ClickParameter(),
            UrlParameter.class, BidResponse.SeatBid.Bid.class, "bidClickParameter");
  }
}
