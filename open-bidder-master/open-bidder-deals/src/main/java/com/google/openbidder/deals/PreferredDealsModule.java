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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.AbstractScheduledService.Scheduler;
import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

/**
 * Provides the PreferredDealCollectionManager and PreferredDealCollection.
 */
@Parameters(separators = "=")
public class PreferredDealsModule extends AbstractModule {

  private static final Logger logger = LoggerFactory.getLogger(PreferredDealsModule.class);

  @Parameter(names = "--deals_update_period",
      description = "Period to poll the preferred deals bucket for changes, in seconds")
  private int updatePeriodSeconds = 600;

  @Parameter(names = "--deals_bucket",
      description ="Google Cloud Storage bucket where preferred deals are to be loaded from")
  private String preferredDealsBucket;

  @Override
  public void configure() {
    if (Strings.isNullOrEmpty(preferredDealsBucket)) {
      logger.info("Not using cloud storage for loading preferred deals");
      bind(PreferredDealCollectionManager.class)
          .to(NullPreferredDealCollectionManager.class).in(Scopes.SINGLETON);
    } else {
      Preconditions.checkArgument(updatePeriodSeconds > 0,
          "--deals_update_period must be greater than 0");
      logger.info("Loading preferred deals from GCE bucket: {} in every: {} seconds",
          preferredDealsBucket, updatePeriodSeconds);
      bind(Scheduler.class).annotatedWith(PreferredDealsBucket.class)
          .toInstance(Scheduler.newFixedDelaySchedule(0, updatePeriodSeconds, TimeUnit.SECONDS));
      bind(String.class).annotatedWith(PreferredDealsBucket.class).toInstance(preferredDealsBucket);
      bind(PreferredDealCollectionManager.class).to(
          CloudStoragePreferredDealCollectionManager.class).in(Scopes.SINGLETON);
    }
    Multibinder.newSetBinder(binder(), Service.class).addBinding()
        .to(PreferredDealCollectionManager.class);
  }

  @Provides
  @Singleton
  public PreferredDealCollection providePreferredDealCollection(
      PreferredDealCollectionManager manager) {
    return manager.preferredDealCollection();
  }
}