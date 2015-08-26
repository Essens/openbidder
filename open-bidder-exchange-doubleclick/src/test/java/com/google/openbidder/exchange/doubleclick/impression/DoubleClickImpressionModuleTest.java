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

package com.google.openbidder.exchange.doubleclick.impression;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.doubleclick.crypto.DoubleClickCrypto;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;
import com.google.openbidder.api.platform.NoExchange;
import com.google.openbidder.api.testing.impression.CountingImpressionInterceptor;
import com.google.openbidder.config.http.Feature;
import com.google.openbidder.config.server.BidderListenPort;
import com.google.openbidder.exchange.doubleclick.testing.DoubleClickTestUtil;
import com.google.openbidder.http.route.HttpRoute;
import com.google.openbidder.impression.ImpressionModule;

import com.beust.jcommander.JCommander;

import org.junit.Test;

import java.util.List;
import java.util.Set;

/**
 * Tests for {@link DoubleClickImpressionModule}.
 */
public class DoubleClickImpressionModuleTest {

  @Test
  public void testDisabled() {
    testModule(false);
  }

  @Test
  public void testEnabled() {
    testModule(true);
  }

  private void testModule(boolean enabled) {
    List<Module> modules = ImmutableList.of(
        new Module() {
          @Override public void configure(Binder binder) {
            binder.bind(Integer.class).annotatedWith(BidderListenPort.class)
                .toInstance(BidderListenPort.DEFAULT);
            binder.bind(DoubleClickCrypto.Keys.class).toInstance(DoubleClickTestUtil.ZERO_KEYS);
          }
        },
        new NoExchange.Module(),
        new ImpressionModule(),
        new DoubleClickImpressionModule());
    JCommander jcommander = new JCommander(modules);
    jcommander.parse(
        enabled
            ? "--impression_interceptors=" + CountingImpressionInterceptor.class.getName()
            : "",
        "--impression_price=price");
    Injector injector = Guice.createInjector(Stage.DEVELOPMENT, modules);

    if (enabled) {
      @SuppressWarnings("unchecked")
      Set<HttpRoute> httpRoutes = (Set<HttpRoute>)
      injector.getInstance(Key.get(TypeLiteral.get(Types.setOf(HttpRoute.class))));
      assertEquals(
          ImmutableSet.of(Feature.IMPRESSION),
          Iterables.getOnlyElement(httpRoutes).getRequiredFeatures());
    }
  }
}
