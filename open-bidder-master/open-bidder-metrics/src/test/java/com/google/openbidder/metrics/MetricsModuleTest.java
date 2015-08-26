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

package com.google.openbidder.metrics;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;
import com.google.openbidder.config.server.BidderAdminPort;
import com.google.openbidder.http.route.HttpRoute;
import com.google.openbidder.http.template.TemplateModule;
import com.google.openbidder.http.template.mustache.MustacheModule;
import com.google.openbidder.util.FasterxmlJsonModule;

import com.beust.jcommander.JCommander;

import org.junit.Test;

import java.util.List;
import java.util.Set;

/**
 * Tests for {@link MetricsModule}.
 */
public class MetricsModuleTest {

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
            binder.bind(Integer.class).annotatedWith(BidderAdminPort.class)
                .toInstance(BidderAdminPort.DEFAULT);
          }
        },
        new MetricsModule(),
        new FasterxmlJsonModule(),
        new MustacheModule(),
        new TemplateModule()
    );
    JCommander jcommander = new JCommander(modules);
    jcommander.parse(
        "--metrics=" + enabled);
    Injector injector = Guice.createInjector(Stage.DEVELOPMENT, modules);

    if (enabled) {
      @SuppressWarnings("unchecked")
      Set<HttpRoute> httpRoutes = (Set<HttpRoute>)
      injector.getInstance(Key.get(TypeLiteral.get(Types.setOf(HttpRoute.class))));
      assertEquals(5, Iterables.size(httpRoutes));
    }
  }
}
