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

package com.google.openbidder.api.testing.impression;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import com.google.openbidder.api.impression.ImpressionController;
import com.google.openbidder.api.impression.ImpressionInterceptor;
import com.google.openbidder.api.testing.interceptor.InterceptorTestUtil;

import com.codahale.metrics.MetricRegistry;

/**
 * Utilities for {@link ImpressionInterceptor} unit tests.
 */
public final class ImpressionTestUtil {

  private ImpressionTestUtil() {
  }

  public static ImpressionController newImpressionController(
      ImpressionInterceptor... interceptors) {
    return newImpressionController(new MetricRegistry(), interceptors);
  }

  public static ImpressionController newImpressionController(
      MetricRegistry metricRegistry,
      ImpressionInterceptor... interceptors) {
    ImpressionController controller = new ImpressionController(
        ImmutableList.<ImpressionInterceptor>copyOf(interceptors),
        metricRegistry);
    controller.startAsync().awaitRunning();
    return controller;
  }

  @SafeVarargs
  public static ImpressionController newImpressionController(
      Module userRootModule,
      Class<? extends ImpressionInterceptor>... interceptorClasses) {

    ImpressionController controller = new ImpressionController(
        InterceptorTestUtil.bind(userRootModule, interceptorClasses),
        new MetricRegistry());
    controller.startAsync().awaitRunning();
    return controller;
  }
}
