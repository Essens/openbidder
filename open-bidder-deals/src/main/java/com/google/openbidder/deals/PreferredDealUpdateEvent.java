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

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.openbidder.deals.model.Deals.PreferredDeal;

import java.util.Collection;

/**
 * Event sent when {@link PreferredDeal}s have been updated
 * from the storage service (the two collections should be mutually exclusive)..
 */
public class PreferredDealUpdateEvent {
  private final ImmutableCollection<PreferredDeal> preferredDeals;

  public PreferredDealUpdateEvent(
      Collection<PreferredDeal> preferredDeals) {
    this.preferredDeals = ImmutableSet.copyOf(preferredDeals);
  }

  public final ImmutableCollection<PreferredDeal> allPreferredDeals() {
    return preferredDeals;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("preferredDeal#", preferredDeals.size())
        .toString();
  }
}