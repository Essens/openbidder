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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.openbidder.deals.model.Deals.PreferredDeal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Exchange- and data source-neutral implementation for {@link PreferredDealCollectionManager}.
 */
public abstract class AbstractPreferredDealCollectionManager
    extends AbstractScheduledService
    implements PreferredDealCollectionManager {
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  private final PreferredDealCollection preferredDealCollection = new PreferredDealCollection();
  private final Scheduler scheduler;

  protected AbstractPreferredDealCollectionManager(Scheduler scheduler) {
    this.scheduler = checkNotNull(scheduler);
  }

  @Override
  protected Scheduler scheduler() {
    return scheduler;
  }

  @Override
  public PreferredDealCollection preferredDealCollection() {
    return preferredDealCollection;
  }

  /**
   * Loads {@code PreferredDeal}s from the data source.
   */
  protected abstract Collection<PreferredDeal> load();

  @Override
  protected void runOneIteration() {
    logger.info("Checking the storage service for updates to preferred deals information.");
    Collection<PreferredDeal> preferredDeals = load();
    preferredDealCollection.handlePreferredDealUpdates(
        new PreferredDealUpdateEvent(preferredDeals));
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("preferredDealCollection", preferredDealCollection)
        .toString();
  }
}