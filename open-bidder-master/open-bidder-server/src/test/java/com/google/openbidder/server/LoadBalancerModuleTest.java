/*
 * Copyright 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.openbidder.server;

import static org.junit.Assert.assertNotNull;

import com.google.common.collect.ImmutableList;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.Stage;

import com.beust.jcommander.JCommander;

import org.junit.Test;

import java.net.UnknownHostException;
import java.util.List;

/**
 * Tests for {@link LoadBalancerModule}.
 */
public class LoadBalancerModuleTest {

  @Test
  public void testNoConfig() {
    testModule(false);
  }

  @Test
  public void testConfig() {
    testModule(true);
  }

  private void testModule(boolean host) {
    try {
      List<Module> modules = ImmutableList.<Module>of(new LoadBalancerModule());
      JCommander jcommander = new JCommander(modules);
      jcommander.parse(
          host ? "--load_balancer_host=http://localhost" : "");
      assertNotNull(Guice.createInjector(Stage.DEVELOPMENT, modules));
    } catch (CreationException e) {
      // This may happen with VPNs
      if (!(e.getCause() instanceof IllegalStateException &&
          e.getCause().getCause() instanceof UnknownHostException)) {
        throw e;
      }
    }
  }
}
