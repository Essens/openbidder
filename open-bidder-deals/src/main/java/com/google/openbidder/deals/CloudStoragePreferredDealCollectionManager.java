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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.openbidder.deals.model.Deals.PreferredDeal;
import com.google.openbidder.storage.dao.Dao;
import com.google.protobuf.MessageLite;

import java.util.Collection;

import javax.inject.Inject;

/**
 * Populates the {@link PreferredDealCollection} with preferred deals information from
 * the Storage Service.
 */
public class CloudStoragePreferredDealCollectionManager
    extends AbstractPreferredDealCollectionManager {

  private static final String PREFERRED_DEALS_OBJECT = "preferred-deals";

  private final String preferredDealsBucket;
  private final Dao<MessageLite> dao;

  @Inject
  public CloudStoragePreferredDealCollectionManager(
      @PreferredDealsBucket Scheduler scheduler,
      Dao<MessageLite> dao,
      @PreferredDealsBucket String preferredDealsBucket) {

    super(scheduler);
    checkArgument(!Strings.isNullOrEmpty(preferredDealsBucket),
        "Must specify preferred deal cloud storage bucket");
    this.dao = checkNotNull(dao);
    this.preferredDealsBucket = checkNotNull(preferredDealsBucket);
  }

  @Override
  protected Collection<PreferredDeal> load() {
    return dao.getObjectList(PreferredDeal.class, preferredDealsBucket, PREFERRED_DEALS_OBJECT);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("preferredDealsBucket", preferredDealsBucket)
        .toString();
  }
}