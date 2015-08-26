/*
 * Copyright 2013 Google Inc. All Rights Reserved.
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

package com.google.openbidder.system;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;
import com.google.openbidder.config.http.Feature;
import com.google.openbidder.config.system.AvailableProcessors;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

/**
 * Physical system properties.
 */
@Parameters(separators = "=")
public class SystemModule extends AbstractModule {
  private static final Logger logger = LoggerFactory.getLogger(SystemModule.class);

  @Parameter(names = "--platform",
      description = "Whether running on general Linux or a Google Compute Engine VM")
  private Platform platform = Platform.GENERIC;

  @Parameter(names = "--available_processors",
      description = "Number of processors on the VM (0 = determine at runtime)")
  private int availableProcessors = AvailableProcessors.DEFAULT;

  @Override
  protected void configure() {
    bind(Platform.class).toInstance(platform);
    Multibinder.newSetBinder(binder(), Feature.class);
  }

  @Provides
  @Singleton
  @AvailableProcessors
  public int provideAvailableProcessors() {
    int processorCount;
    if (availableProcessors == 0) {
      processorCount = Runtime.getRuntime().availableProcessors();
      logger.info("Determined available processors from system: {}", processorCount);
    } else {
      processorCount = availableProcessors;
      logger.info("Overriding system available processors with: {}", processorCount);
    }
    return processorCount;
  }
}
