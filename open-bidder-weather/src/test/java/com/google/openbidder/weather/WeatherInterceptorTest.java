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

import static org.junit.Assert.assertEquals;

import com.google.api.client.http.HttpResponseException;
import com.google.common.collect.Iterators;
import com.google.openbidder.api.bidding.BidController;
import com.google.openbidder.api.bidding.BidInterceptor;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.interceptor.InterceptorChain;
import com.google.openbidder.api.openrtb.ObExt;
import com.google.openbidder.api.testing.bidding.BiddingTestUtil;
import com.google.openbidder.api.testing.bidding.TestBidRequestBuilder;
import com.google.openbidder.api.testing.bidding.TestBidResponseBuilder;
import com.google.openbidder.cloudstorage.GoogleCloudStorage;
import com.google.openbidder.cloudstorage.testing.FakeGoogleCloudStorage;
import com.google.openbidder.util.testing.FakeClock;
import com.google.openbidder.weather.WeatherServiceMock.WeatherData;
import com.google.openbidder.weather.model.Weather.WeatherBiddingRule;
import com.google.openbidder.weather.model.Weather.WeatherConditions;
import com.google.openbidder.weather.model.Weather.WeatherRules;
import com.google.openbidder.weather.model.Weather.WeatherTarget;
import com.google.openrtb.OpenRtb;
import com.google.openrtb.OpenRtb.BidRequest.Device;
import com.google.openrtb.OpenRtb.BidRequest.Geo;
import com.google.openrtb.OpenRtb.BidRequest.Imp;
import com.google.openrtb.OpenRtb.BidRequest.Imp.Banner;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid.Bid;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import javax.annotation.Nullable;

/**
 * Unit tests for {@link WeatherInterceptor}.
 */
public class WeatherInterceptorTest {
  private static final String CID_AMUSEMENT_PARK = "1";
  private static final String CID_SAILING_CRUISE = "2";
  private BidController controller;

  @Before
  public void setUp() throws HttpResponseException {
    GoogleCloudStorage cloudStorage = new FakeGoogleCloudStorage(new FakeClock());
    cloudStorage.putBucket("weather-0");

    // Fake data for our tests
    WeatherDao weatherDao = new WeatherDaoCloudStorage(cloudStorage, "weather-0");

    weatherDao.insert(WeatherRules.newBuilder().setOwnerId(CID_AMUSEMENT_PARK)
        .addRules(WeatherBiddingRule.newBuilder()
            .setTarget(WeatherTarget.newBuilder()
                .setMinTemp(50).setMaxTemp(60)
                .setMaxWind(30)
                .setMinHumidity(0.1).setMaxHumidity(0.5))
            .setMultiplier(0.8))
        .addRules(WeatherBiddingRule.newBuilder()
            .setTarget(WeatherTarget.newBuilder()
                .setMinTemp(60).setMaxTemp(80)
                .setMaxWind(30)
                .setMinHumidity(0.1).setMaxHumidity(0.7))
            .setMultiplier(1.0))
        .addRules(WeatherBiddingRule.newBuilder()
            .setTarget(WeatherTarget.newBuilder()
                .setMinTemp(80).setMaxTemp(90)
                .setMaxWind(30)
                .setMinHumidity(0.1).setMaxHumidity(0.7))
            .setMultiplier(0.8))
        .build());

    weatherDao.insert(WeatherRules.newBuilder().setOwnerId(CID_SAILING_CRUISE)
        .addRules(WeatherBiddingRule.newBuilder()
            .setTarget(WeatherTarget.newBuilder()
                .setMinTemp(50).setMaxTemp(90)
                .setMinWind(15).setMaxWind(40)
                .setMaxHumidity(0.5))
            .setMultiplier(1.0))
        .build());

    // Fake weather information for our tests
    WeatherService weatherService = new WeatherServiceMock(
        new WeatherData("New York,USA", WeatherConditions.newBuilder()
            .setTempFahrenheit(72).setWindMph(14).setHumidityPercent(0.3).build()),
        new WeatherData("Rio de Janeiro,BRA", WeatherConditions.newBuilder()
            .setTempFahrenheit(90).setWindMph(35).setHumidityPercent(0.2).build()),
        new WeatherData("Paris,FRA", WeatherConditions.newBuilder()
            .setTempFahrenheit(55).setWindMph(5).setHumidityPercent(0.1).build())
    );

    controller = BiddingTestUtil.newBidController(
        new WeatherInterceptor(weatherDao, weatherService),
        new BidInterceptor() {
          @Override public void execute(InterceptorChain<BidRequest, BidResponse> chain) {
            for (Imp imp : chain.request().imps()) {
              for (String cid : imp.getExtension(ObExt.impCid)) {
                chain.response().addBid(Bid.newBuilder()
                    .setId("1")
                    .setImpid(imp.getId())
                    .setCid(cid)
                    .setPrice(1.0)
                    .setAdm("snippet"));
              }
            }
          }});
  }

  @After
  public void tearDown() {
    if (controller != null) {
      controller.stopAsync().awaitTerminated();
    }
  }

  @Test
  public void testAmusementPark() {
    // Good weather; max bid
    assertEquals(1.0, run(CID_AMUSEMENT_PARK, "New York,USA"), 1e-9);
    // Too hot; min bid
    assertEquals(0.2, run(CID_AMUSEMENT_PARK, "Rio de Janeiro,BRA"), 1e-9);
    // Acceptable; low bid
    assertEquals(0.8, run(CID_AMUSEMENT_PARK, "Paris,FRA"), 1e-9);
  }

  @Test
  public void testSailingCruise() {
    // No wind; min bid
    assertEquals(0.2, run(CID_SAILING_CRUISE, "New York,USA"), 1e-9);
    // Good weather; max bid
    assertEquals(1.0, run(CID_SAILING_CRUISE, "Rio de Janeiro,BRA"), 1e-9);
    // No wind; min bid
    assertEquals(0.2, run(CID_SAILING_CRUISE, "Paris,FRA"), 1e-9);
  }

  @Test
  public void testNoLocation() {
    // No location; min bid
    assertEquals(0.2, run(CID_AMUSEMENT_PARK, null), 1e-9);
  }

  private double run(String cid, @Nullable String location) {
    OpenRtb.BidRequest.Builder openrtbRequest = OpenRtb.BidRequest.newBuilder()
        .setId("1")
        .addImp(Imp.newBuilder()
            .setId("1")
            .setBanner(Banner.newBuilder().setId("1").setW(728).setH(90))
            .setBidfloor(0.1)
            .addExtension(ObExt.impCid, String.valueOf(cid)));
    if (location != null) {
      openrtbRequest.setDevice(Device.newBuilder().setGeo(Geo.newBuilder()
          .setCity(location.split(",")[0])
          .setCountry(location.split(",")[1])));
    }
    BidRequest request = TestBidRequestBuilder.create().setRequest(openrtbRequest).build();
    BidResponse response = TestBidResponseBuilder.create().build();
    controller.onRequest(request, response);
    Iterator<Bid.Builder> bids = response.bids().iterator();
    return bids.hasNext() ? Iterators.getOnlyElement(bids).getPrice() : 0;
  }
}
