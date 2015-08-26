/*
 * Copyright 2015 Google Inc. All Rights Reserved.
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

package com.google.openbidder.netty.server;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.AbstractService;
import com.google.common.util.concurrent.Service;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.inject.multibindings.Multibinder;
import com.google.openbidder.config.bid.HasBid;
import com.google.openbidder.config.click.HasClick;
import com.google.openbidder.config.impression.HasImpression;
import com.google.openbidder.config.match.HasMatch;
import com.google.openbidder.config.server.WebserverRuntime;
import com.google.openbidder.flags.FlagsModuleBuilder;
import com.google.openbidder.http.HttpModule;
import com.google.openbidder.http.template.TemplateModule;
import com.google.openbidder.http.template.mustache.MustacheModule;
import com.google.openbidder.server.LoadBalancerModule;
import com.google.openbidder.server.ServerModule;
import com.google.openbidder.system.SystemModule;

import org.junit.Test;

import java.util.List;
import java.util.Properties;

/**
 * Tests for {@link NettyServerModule}.
 */
public class NettyServerModuleTest {

  @Test
  public void testModule() {
    Properties sysProps = (Properties) System.getProperties().clone();

    List<Module> modules = ImmutableList.<Module>of(
        new LoadBalancerModule(),
        new ServerModule(),
        new NettyServerModule(),
        new SystemModule(),
        new HttpModule(),
        new TemplateModule(),
        new MustacheModule(),
        new Module() {
          @Override public void configure(Binder binder) {
            binder.bind(boolean.class).annotatedWith(HasBid.class).toInstance(true);
            binder.bind(boolean.class).annotatedWith(HasImpression.class).toInstance(true);
            binder.bind(boolean.class).annotatedWith(HasClick.class).toInstance(true);
            binder.bind(boolean.class).annotatedWith(HasMatch.class).toInstance(true);

            Multibinder.newSetBinder(binder, Service.class).addBinding().toInstance(
                new AbstractService() {
                  @Override protected void doStart() {
                    notifyStarted();
                  }
                  @Override protected void doStop() {
                    notifyStopped();
                  }
                });
          }
        }
    );

    Module rootModule = new FlagsModuleBuilder().addModules(modules).build(
        "--load_balancer_host=http://localhost",
        "--load_balancer_port=18080",
        "--listen_port=18081",
        "--admin_port=18082");
    Injector injector = Guice.createInjector(Stage.DEVELOPMENT, rootModule);

    Service openBidderServer =
        injector.getInstance(Key.get(Service.class, WebserverRuntime.class));

    openBidderServer.startAsync().awaitRunning();
    openBidderServer.stopAsync().awaitTerminated();

    System.setProperties((Properties) sysProps.clone());
  }
}
