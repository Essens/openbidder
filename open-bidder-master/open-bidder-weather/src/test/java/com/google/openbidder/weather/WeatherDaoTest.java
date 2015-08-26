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

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.api.client.http.HttpResponseException;
import com.google.openbidder.cloudstorage.GoogleCloudStorage;
import com.google.openbidder.cloudstorage.testing.FakeGoogleCloudStorage;
import com.google.openbidder.util.testing.FakeClock;
import com.google.openbidder.weather.model.Weather.WeatherBiddingRule;
import com.google.openbidder.weather.model.Weather.WeatherRules;
import com.google.openbidder.weather.model.Weather.WeatherTarget;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link WeatherDao}.
 */
public class WeatherDaoTest {
  private WeatherDao weatherDao;

  @Before
  public void setUp() throws HttpResponseException {
    GoogleCloudStorage cloudStorage = new FakeGoogleCloudStorage(new FakeClock());
    cloudStorage.putBucket("weather-0");
    weatherDao = new WeatherDaoCloudStorage(cloudStorage, "weather-0");
  }

  @Test
  public void testStorage() {
    WeatherRules rule = WeatherRules.newBuilder()
        .setOwnerId("0")
        .addRules(WeatherBiddingRule.newBuilder()
            .setTarget(WeatherTarget.newBuilder()
                .setMinTemp(50).setMaxTemp(90)
                .setMinWind(15).setMaxWind(40)
                .setMaxHumidity(0.5))
            .setMultiplier(1.0)
            .build()
        ).build();
    weatherDao.insert(rule);
    assertEquals(singletonList(rule), weatherDao.listRules());
    weatherDao.deleteRules("0");
    assertTrue(weatherDao.listRules().isEmpty());
  }
}
