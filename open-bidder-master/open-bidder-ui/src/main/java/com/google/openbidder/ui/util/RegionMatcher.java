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

package com.google.openbidder.ui.util;

import javax.annotation.Nullable;

/**
 * Matching Google Compute Engine zone name by key word. Null if not matching any of the
 * allowed region values by Ad Exchange.
 * See https://developers.google.com/ad-exchange/buyer-rest/v1.1/accounts#resource for details.
 */
public class RegionMatcher {

  private RegionMatcher() {}

  public static @Nullable String mapGceToDoubleClickRegion(String zoneId) {
    if (zoneId.contains("asia")) {
      return "ASIA";
    } else if (zoneId.contains("europe")) {
      return "EUROPE";
    } else if (zoneId.contains("us")) {
      if (zoneId.contains("east") || zoneId.contains("central")) {
        return "US_EAST";
      }
      if (zoneId.contains("west")) {
        return "US_WEST";
      }
    }
    return null;
  }
}
