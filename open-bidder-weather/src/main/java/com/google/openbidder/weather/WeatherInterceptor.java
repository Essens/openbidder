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

package com.google.openbidder.weather;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.openbidder.api.bidding.BidInterceptor;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.interceptor.InterceptorChain;
import com.google.openbidder.weather.model.Weather.WeatherConditions;
import com.google.openbidder.weather.model.Weather.WeatherRules;
import com.google.openrtb.OpenRtb.BidRequest.Geo;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid.Bid;
import com.google.protobuf.TextFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * An interceptor that prices bids based on the weather conditions at the target location.
 * This can be useful for services that depend on weather, such as an amusement park.
 */
public class WeatherInterceptor implements BidInterceptor {
  private static final Logger logger = LoggerFactory.getLogger(WeatherInterceptor.class);
  private ImmutableMap<String, WeatherBiddingFunction> weatherFunctions;
  private final WeatherDao weatherDao;
  private final WeatherService weatherService;

  @Inject
  public WeatherInterceptor(WeatherDao weatherDao, WeatherService weatherService) {
    this.weatherDao = weatherDao;
    this.weatherService = weatherService;
  }

  @PostConstruct
  public void postConstruct() {
    ImmutableMap.Builder<String, WeatherBiddingFunction> map = ImmutableMap.builder();
    for (WeatherRules rules : weatherDao.listRules()) {
      map.put(rules.getOwnerId(), new WeatherBiddingFunction(rules));
    }
    weatherFunctions = map.build();

    logger.info("Initialized with {} rules", weatherFunctions.size());
  }

  @Override
  public void execute(final InterceptorChain<BidRequest, BidResponse> chain) {
    // We will post-process bids created by other interceptors down the chain.
    chain.proceed();

    Geo geo = chain.request().openRtb().getDevice().getGeo();
    final WeatherConditions cond = geo.hasCity() && geo.hasCountry()
        ? weatherService.getWeatherConditions(geo.getCity() + ',' + geo.getCountry())
        : null;

    chain.response().updateBids(new Function<Bid.Builder, Boolean>() {
      @Override public Boolean apply(Bid.Builder bid) {
        assert bid != null;
        WeatherBiddingFunction function = bid.hasCid() ? weatherFunctions.get(bid.getCid()) : null;

        if (function != null) {
          Double multiplier = cond == null ? null : function.apply(cond);
          // No weather data or applicable rules for this location? Make the bid very cheap
          multiplier = multiplier == null ? 0.2 : multiplier;
          double updatedPrice = multiplier * bid.getPrice();

          if (logger.isDebugEnabled()) {
            logger.debug("Weather interceptor updating bid: {} x {} = {}: {}",
                bid.getPrice(), multiplier, updatedPrice, TextFormat.shortDebugString(bid));
          }

          bid.setPrice(updatedPrice);
        }
        return true;
      }});
  }
}
