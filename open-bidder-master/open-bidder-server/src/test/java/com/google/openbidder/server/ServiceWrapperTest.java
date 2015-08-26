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

package com.google.openbidder.server;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.Service;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;
import com.google.openbidder.config.server.WebserverRuntime;

import org.junit.Test;

/**
 * Tests for {@link ServiceWrapper}, {@link ServiceUtil}.
 */
public class ServiceWrapperTest {

  @Test
  public void test_Ok() {
    TestService service = new TestService(new String[]{}, false, false);
    service.main();
    service.stopAsync().awaitTerminated();
  }

  @Test(expected = IllegalStateException.class)
  public void test_failStartup() {
    TestService service = new TestService(new String[]{}, true, false);
    service.main();
  }

  @Test(expected = IllegalStateException.class)
  public void test_failShutdown() {
    TestService service = new TestService(new String[]{}, false, true);
    service.main();
    service.stopAsync().awaitTerminated();
  }

  @Test(expected = IllegalStateException.class)
  public void test_failParams() {
    TestService service = new TestService(new String[]{ "--help" }, false, true);
    service.main();
    service.stopAsync().awaitTerminated();
  }

  static class TestService extends ServiceWrapper {
    private final boolean failStartup;
    private final boolean failShutdown;
    public TestService(String[] args, boolean failStartup, boolean failShutdown) {
      super(args);
      this.failStartup = failStartup;
      this.failShutdown = failShutdown;
      setRuntime(null);
    }

    @Override protected ImmutableList<Module> getModules() {
      return ImmutableList.<Module>of(new Module() {
        @Override public void configure(Binder binder) {
          Multibinder.newSetBinder(binder, Service.class).addBinding().toInstance(newService());
          binder.bind(Service.class).annotatedWith(WebserverRuntime.class).toInstance(newService());
        }
      });
    }

    private Service newService() {
      return new AbstractIdleService() {
        @Override protected void startUp() throws Exception {
          if (failStartup) {
            throw new IllegalStateException("Bad startup");
          }
        }
        @Override protected void shutDown() throws Exception {
          if (failShutdown) {
            throw new IllegalStateException("Bad shutdown");
          }
        }
      };
    }
  }
}
