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

import com.google.common.collect.ImmutableMap;
import com.google.openbidder.weather.model.Weather.WeatherConditions;

/**
 * Unit-testing mock for {@link WeatherService}.
 */
public class WeatherServiceMock implements WeatherService {
  private final ImmutableMap<String, WeatherConditions> data;

  public WeatherServiceMock(WeatherData... weatherData) {
    ImmutableMap.Builder<String, WeatherConditions> builder = ImmutableMap.builder();
    for (WeatherData wd : weatherData) {
      builder.put(wd.getLocation(), wd.getConditions());
    }
    this.data = builder.build();
  }

  @Override
  public WeatherConditions getWeatherConditions(String location) {
    return data.get(location);
  }

  public static class WeatherData {
    private final String location;
    private final WeatherConditions conditions;

    public WeatherData(String postalCode, WeatherConditions conditions) {
      this.location = postalCode;
      this.conditions = conditions;
    }

    public final String getLocation() {
      return location;
    }

    public final WeatherConditions getConditions() {
      return conditions;
    }
  }
}
