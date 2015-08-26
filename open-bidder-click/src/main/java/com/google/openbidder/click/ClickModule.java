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

package com.google.openbidder.click;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.openbidder.api.click.ClickController;
import com.google.openbidder.api.click.ClickInterceptor;
import com.google.openbidder.config.click.ClickInterceptors;
import com.google.openbidder.config.click.ClickPath;
import com.google.openbidder.config.click.HasClick;
import com.google.openbidder.config.http.Feature;
import com.google.openbidder.http.route.AbstractHttpRouteProvider;
import com.google.openbidder.http.route.HttpRoute;
import com.google.openbidder.util.GuiceUtils;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.codahale.metrics.MetricRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Click callback functionality.
 */
@Parameters(separators = "=")
public class ClickModule extends AbstractModule {
  private static final Logger logger = LoggerFactory.getLogger(ClickModule.class);

  @Parameter(names = "--click_interceptors",
      description = "List of click interceptor class names to install")
  private List<String> interceptors = new ArrayList<>();

  @Parameter(names = "--click_path",
      description = "Path spec to install click callback support on")
  private String path = ClickPath.DEFAULT;

  @Override
  protected void configure() {
    logger.info("Click interceptors: {}", interceptors);
    boolean hasClick = !interceptors.isEmpty();
    bind(boolean.class).annotatedWith(HasClick.class).toInstance(hasClick);
    bind(new TypeLiteral<ImmutableList<String>>() {}).annotatedWith(ClickInterceptors.class)
        .toInstance(ImmutableList.copyOf(interceptors));
    if (hasClick) {
      logger.info("Binding click requests to: {}", path);
      bind(String.class).annotatedWith(ClickPath.class).toInstance(path);
      Multibinder.newSetBinder(binder(), Service.class).addBinding()
          .to(ClickController.class).in(Scopes.SINGLETON);
      Multibinder.newSetBinder(binder(), Feature.class).addBinding().toInstance(Feature.CLICK);
      Multibinder.newSetBinder(binder(), HttpRoute.class).addBinding()
          .toProvider(ClickHttpRouteProvider.class).in(Scopes.SINGLETON);
    } else {
      logger.info("Click request handling not installed");
    }
  }

  @Provides
  @Singleton
  @ClickInterceptors
  public ImmutableList<? extends ClickInterceptor> provideClickInterceptors(
      Injector injector,
      @ClickInterceptors ImmutableList<String> classNames) {

    return GuiceUtils.loadInstances(injector, ClickInterceptor.class, classNames);
  }

  @Provides
  @Singleton
  public ClickController provideClickController(
      MetricRegistry metricRegistry,
      @ClickInterceptors ImmutableList<? extends ClickInterceptor> interceptors) {

    return new ClickController(interceptors, metricRegistry);
  }

  protected static class ClickHttpRouteProvider extends AbstractHttpRouteProvider {
    @Inject
    private ClickHttpRouteProvider(@ClickPath String path, ClickRequestReceiver receiver) {
      super(HttpRoute.get("click", path, receiver, Feature.CLICK));
    }
  }
}
