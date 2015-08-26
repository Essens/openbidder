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

import com.google.openbidder.weather.model.Weather.WeatherRules;

import java.util.List;

/**
 * Weather targeting is not part of DoubleClick Ad Exchange's campaign model, so this will
 * come from a user-specific database accessed by this DAO.
 */
public interface WeatherDao {

  /**
   * Inserts Weather rules.
   */
  void insert(WeatherRules rules);

  /**
   * Deletes all Weather rules for an owner entity.
   */
  void deleteRules(String ownerId);

  /**
   * @return All {@link WeatherRules}.
   */
  List<WeatherRules> listRules();
}
