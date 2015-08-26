/*
 * Copyright 2014 Google Inc. All Rights Reserved.
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

package com.google.openbidder.deals;

/**
 * Implemented by classes that wish to be notified of updates to the
 * {@link com.google.openbidder.deals.model.Deals.PreferredDeal}s.
 * <p>
 * Implementing classes should inject a {@link PreferredDealCollection} and register themselves.
 */
public interface PreferredDealUpdateListener {

  /**
   * Called when there are {@link com.google.openbidder.deals.model.Deals.PreferredDeal} changes.
   */
  void onPreferredDealUpdate(PreferredDealUpdateEvent event);

}