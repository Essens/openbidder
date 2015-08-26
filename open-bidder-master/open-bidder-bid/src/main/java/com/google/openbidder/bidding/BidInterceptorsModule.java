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

import com.google.common.io.CharStreams;
import com.google.inject.AbstractModule;
import com.google.inject.util.Providers;
import com.google.openbidder.api.openrtb.ObExt;
import com.google.openbidder.bidding.interceptor.ConfigurableBidInterceptor;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid.Bid;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Standard bid interceptors.
 */
@Parameters(separators = "=")
public class BidInterceptorsModule extends AbstractModule {

  @Parameter(names = "--configbid_url", required = false,
      description = "ConfigurableBidInterceptor: Clickthrough URL (mandatory for this interceptor)")
  private String configbidUrl;

  @Parameter(names = "--configbid_snippet", required = false,
      description = "ConfigurableBidInterceptor: Snippet resource (started with '/') or value"
        + "(mandatory for this interceptor)")
  private String configbidSnippet;

  @Parameter(names = "--configbid_bid_probability", required = false,
      description = "ConfigurableBidInterceptor: Bid probability (0..1)")
  private float configbidBidProbability = ConfigurableBidInterceptor.BidProbability.DEFAULT;

  @Parameter(names = "--configbid_error_probability", required = false,
      description = "ConfigurableBidInterceptor: Error probability (0..1)")
  private float configbidErrorProbability = ConfigurableBidInterceptor.ErrorProbability.DEFAULT;

  @Parameter(names = "--configbid_cpm_multiplier", required = false,
      description = "ConfigurableBidInterceptor: CPM Multiplier (0=minCpm..1=maxCpm)")
  private Double configbidCpmMultiplier;

  @Parameter(names = "--configbid_cpm_value", required = false,
      description = "ConfigurableBidInterceptor: CPM value in currency units")
  private Double configbidCpmMicros;

  @Parameter(names = "--configbid_crid", required = false,
      description = "ConfigurableBidInterceptor: Creative ID")
  private String configbidCrid;

  @Override
  protected void configure() {
    configureConfigurableBidInterceptor();
  }

  private void configureConfigurableBidInterceptor() {
    if (configbidSnippet != null && configbidUrl != null) {
      Bid.Builder bid = Bid.newBuilder()
          .setAdm(readSnippet(configbidSnippet))
          .setExtension(ObExt.bid, ObExt.Bid.newBuilder()
              .addClickThroughUrl(configbidUrl)
              .build());
      if (configbidCrid != null) {
        bid.setCrid(configbidCrid);
      }
      bind(Bid.class).annotatedWith(ConfigurableBidInterceptor.BidPrototype.class)
          .toInstance(bid.buildPartial());
      bind(float.class).annotatedWith(ConfigurableBidInterceptor.BidProbability.class)
          .toInstance(configbidBidProbability);
      bind(float.class).annotatedWith(ConfigurableBidInterceptor.ErrorProbability.class)
          .toInstance(configbidErrorProbability);
      bind(Double.class).annotatedWith(ConfigurableBidInterceptor.CpmMultiplier.class)
          .toProvider(Providers.of(configbidCpmMultiplier));
      bind(Double.class).annotatedWith(ConfigurableBidInterceptor.CpmValue.class)
          .toProvider(Providers.of(configbidCpmMicros));
    }
  }

  private static String readSnippet(String valueOrResource) {
    if (valueOrResource.startsWith("/")) {
      try {
        InputStream is = BidInterceptorsModule.class.getResourceAsStream(valueOrResource);
        if (is == null) {
          throw new IllegalStateException("Resource not found: " + valueOrResource);
        }
        return CharStreams.toString(new InputStreamReader(is));
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    } else {
      return valueOrResource;
    }
  }
}
