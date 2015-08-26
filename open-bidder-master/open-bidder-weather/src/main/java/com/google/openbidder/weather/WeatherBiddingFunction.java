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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Function;
import com.google.openbidder.weather.model.Weather.WeatherBiddingRule;
import com.google.openbidder.weather.model.Weather.WeatherConditions;
import com.google.openbidder.weather.model.Weather.WeatherRules;
import com.google.openbidder.weather.model.Weather.WeatherTarget;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

/**
 * A function that uses a set of weather targeting rules (possibly overlapping)
 * to make a bidding decision for a specific weather condition.
 */
public class WeatherBiddingFunction implements Function<WeatherConditions, Double> {
  private static final Logger logger = LoggerFactory.getLogger(WeatherBiddingFunction.class);
  private final WeatherRules rules;

  public WeatherBiddingFunction(WeatherRules rules) {
    this.rules = checkNotNull(rules);
  }

  @Override
  @Nullable
  public Double apply(WeatherConditions conditions) {
    // Rules don't have to be all mutually exclusive; it's possible that more than one rule
    // matches the current weather conditions. If this happen, we will select the highest bid.

    WeatherBiddingRule bestRule = null;

    for (WeatherBiddingRule rule : rules.getRulesList()) {
      if (apply(rule.getTarget(), conditions)
          && (bestRule == null || bestRule.getMultiplier() < rule.getMultiplier())) {
        bestRule = rule;
      }
    }

    if (logger.isDebugEnabled()) {
      logger.debug("Weather conditions:\n{}\nMatching rule:\n{}", conditions, bestRule);
    }

    return bestRule == null ? null : bestRule.getMultiplier();
  }

  private static boolean apply(WeatherTarget target, WeatherConditions cond) {
    boolean ret =
           (!target.hasMinTemp()     || target.getMinTemp()     <= cond.getTempFahrenheit())
        && (!target.hasMaxTemp()     || target.getMaxTemp()     >= cond.getTempFahrenheit())
        && (!target.hasMinWind()     || target.getMinWind()     <= cond.getWindMph())
        && (!target.hasMaxWind()     || target.getMaxWind()     >= cond.getWindMph())
        && (!target.hasMinHumidity() || target.getMinHumidity() <= cond.getHumidityPercent())
        && (!target.hasMaxHumidity() || target.getMaxHumidity() >= cond.getHumidityPercent());

    if (ret && logger.isDebugEnabled()) {
      logger.debug(
          "Condition: (T={}, W={}, H={}) passes rule: (T=[{}, {}], W=[{}, {}], H=[{}, {}])",
          cond.getTempFahrenheit(), cond.getWindMph(), cond.getHumidityPercent(),
          target.getMinTemp(), target.getMaxTemp(),
          target.getMinWind(), target.getMaxWind(),
          target.getMinHumidity(), target.getMaxHumidity()
      );
    }

    return ret;
  }

  @Override
  public String toString() {
    return rules.toString();
  }
}
