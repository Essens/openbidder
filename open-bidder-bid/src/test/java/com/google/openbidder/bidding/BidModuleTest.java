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

package com.google.openbidder.bidding;

import static org.junit.Assert.assertNotNull;

import com.google.common.collect.ImmutableList;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.openbidder.api.bidding.BidController;
import com.google.openbidder.bidding.interceptor.ConfigurableBidInterceptor;
import com.google.openbidder.config.server.LoadBalancerHost;
import com.google.openbidder.config.server.LoadBalancerPort;

import com.beust.jcommander.JCommander;

import org.junit.Test;

import java.util.List;

/**
 * Tests for {@link BidModule} and {@link BidInterceptorsModule}.
 */
public class BidModuleTest {
  private static final int DEFAULT_HTTP_PORT = 80;

  @Test
  public void testEnabled() {
    for (int test = 0; test < 3; ++test) {
      final int t = test;
      List<Module> modules = ImmutableList.of(
          new Module() {
            @Override public void configure(Binder binder) {
              binder.bind(String.class).annotatedWith(LoadBalancerHost.class)
                  .toInstance(t < 1 ? "" : "test.com");
              binder.bind(Integer.class).annotatedWith(LoadBalancerPort.class)
                  .toInstance(t < 2 ? DEFAULT_HTTP_PORT : LoadBalancerPort.DEFAULT);
            }
          },
          new BidModule(),
          new BidInterceptorsModule());
      JCommander jcommander = new JCommander(modules);
      if (test == 0) {
        jcommander.parse();
      } else {
        jcommander.parse(
          "--bid_interceptors=" + ConfigurableBidInterceptor.class.getName(),
          "--configbid_url=https://www.iab.net",
          "--configbid_snippet=" + (test == 1 ? "/logback.xml" : "<snippet/>"),
          test == 1 ? "" : "--configbid_crid=x",
          "--configbid_cpm_value=1.2",
          "--load_delay=10",
          "--load_work=10",
          "--raw_request=" + (test == 1 ? "/raw-request.txt" : ""),
          "--raw_response=" + (test == 1 ? "/raw-response.txt" : ""));
      }

      Injector injector = Guice.createInjector(Stage.DEVELOPMENT, modules);
      assertNotNull(injector.getInstance(BidController.class));
    }
  }
}
