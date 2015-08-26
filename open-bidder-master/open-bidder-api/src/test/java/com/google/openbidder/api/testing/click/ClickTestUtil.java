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

package com.google.openbidder.api.testing.click;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import com.google.openbidder.api.click.ClickController;
import com.google.openbidder.api.click.ClickInterceptor;
import com.google.openbidder.api.testing.interceptor.InterceptorTestUtil;

import com.codahale.metrics.MetricRegistry;

/**
 * Utilities for {@link ClickInterceptor} unit tests.
 */
public final class ClickTestUtil {

  private ClickTestUtil() {
  }

  public static ClickController newClickController(ClickInterceptor... interceptors) {
    return newClickController(new MetricRegistry(), interceptors);
  }

  public static ClickController newClickController(
      MetricRegistry metricRegistry, ClickInterceptor... interceptors) {
    ClickController controller = new ClickController(
        ImmutableList.<ClickInterceptor>copyOf(interceptors),
        metricRegistry);
    controller.startAsync().awaitRunning();
    return controller;
  }

  @SafeVarargs
  public static ClickController newClickController(
      Module userRootModule,
      Class<? extends ClickInterceptor>... interceptorClasses) {

    ClickController controller = new ClickController(
        InterceptorTestUtil.bind(userRootModule, interceptorClasses),
        new MetricRegistry());
    controller.startAsync().awaitRunning();
    return controller;
  }
}
