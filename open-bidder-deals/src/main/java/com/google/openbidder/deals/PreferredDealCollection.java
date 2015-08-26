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

import static com.google.common.collect.Lists.newCopyOnWriteArrayList;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.openbidder.deals.model.Deals.PreferredDeal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

/**
 * An in-memory collection of {@link PreferredDeal}s.
 */
public class PreferredDealCollection implements Serializable {

  private static final Logger logger = LoggerFactory.getLogger(PreferredDealCollection.class);

  private final AtomicReference<ImmutableMap<Long, PreferredDeal>> index =
      new AtomicReference<>(ImmutableMap.<Long, PreferredDeal>of());
  private final List<PreferredDealUpdateListener> listeners = newCopyOnWriteArrayList();

  /**
   * @return {@link PreferredDeal} with  the deal id parameter,
   * or {@code null} if it does not exist.
   */
  public final @Nullable PreferredDeal getById(Long dealId) {
    return index.get().get(dealId);
  }

  /**
   * @return Unmodifiable set of {@link PreferredDeal}s currently loaded by the bidder.
   */
  public final ImmutableCollection<PreferredDeal> getAll() {
    return index.get().values();
  }

  /**
   * Registers a {@link PreferredDealUpdateListener} that will be notified when new
   * {@link PreferredDeal}s information is received.
   */
  public final void addPreferredDealUpdateListener(PreferredDealUpdateListener listener) {
    listeners.add(listener);
  }

  /**
   * Removes a {@link PreferredDealUpdateListener}.
   */
  public void removePreferredDealUpdateListener(PreferredDealUpdateListener listener) {
    listeners.remove(listener);
  }

  /**
   * Processes {@link PreferredDeal} changes, updating the underlying collection and
   * notifying listeners.
   */
  public void handlePreferredDealUpdates(PreferredDealUpdateEvent event) {
    logger.info("Handling preferred deals update event...");
    ImmutableMap.Builder<Long, PreferredDeal> builder = ImmutableMap.builder();
    for (PreferredDeal preferredDeal : event.allPreferredDeals()) {
      builder.put(preferredDeal.getDealId(), preferredDeal);
    }
    this.index.set(builder.build());

    // Propagate the event to our own listeners
    for (PreferredDealUpdateListener listener : listeners) {
      listener.onPreferredDealUpdate(event);
    }
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("index", index)
        .add("listeners", listeners)
        .toString();
  }
}
