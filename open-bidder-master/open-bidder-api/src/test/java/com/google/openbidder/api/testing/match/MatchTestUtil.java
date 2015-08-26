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

package com.google.openbidder.api.testing.match;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import com.google.openbidder.api.match.MatchController;
import com.google.openbidder.api.match.MatchInterceptor;
import com.google.openbidder.api.testing.interceptor.InterceptorTestUtil;

import com.codahale.metrics.MetricRegistry;

/**
 * Utilities for Match unit tests.
 */
public final class MatchTestUtil {

  private MatchTestUtil() {
  }

  @SafeVarargs
  public static MatchController newMatchController(MatchInterceptor... interceptors) {
    return newMatchController(new MetricRegistry(), interceptors);
  }

  @SafeVarargs
  public static MatchController newMatchController(
      MetricRegistry metricRegistry, MatchInterceptor... interceptors) {
    MatchController controller = new MatchController(
        ImmutableList.copyOf(interceptors),
        metricRegistry);
    controller.startAsync().awaitRunning();
    return controller;
  }

  @SafeVarargs
  public static MatchController newMatchController(
      Module userRootModule,
      Class<? extends MatchInterceptor>... interceptorClasses) {

    MatchController controller = new MatchController(
        InterceptorTestUtil.bind(userRootModule, interceptorClasses),
        new MetricRegistry());
    controller.startAsync().awaitRunning();
    return controller;
  }
}
