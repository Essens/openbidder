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

package com.google.openbidder.impression;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.openbidder.api.impression.ImpressionController;
import com.google.openbidder.api.impression.ImpressionInterceptor;
import com.google.openbidder.config.http.Feature;
import com.google.openbidder.config.impression.HasImpression;
import com.google.openbidder.config.impression.ImpressionInterceptors;
import com.google.openbidder.config.impression.ImpressionPath;
import com.google.openbidder.config.impression.PriceName;
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
 * Impression callback functionality.
 */
@Parameters(separators = "=")
public class ImpressionModule extends AbstractModule {
  private static final Logger logger = LoggerFactory.getLogger(ImpressionModule.class);

  @Parameter(names = "--impression_interceptors",
      description = "List of impression interceptor class names to install")
  private List<String> interceptors = new ArrayList<>();

  @Parameter(names = "--impression_path",
      description = "Path spec to install impression callback support on")
  private String path = ImpressionPath.DEFAULT;

  @Parameter(names = "--impression_price",
      description = "Parameter name for the winning price")
  private String priceName = PriceName.DEFAULT;

  @Override
  protected void configure() {
    logger.info("Impression interceptors: {}", interceptors);
    boolean hasImpression = !interceptors.isEmpty();
    bind(boolean.class).annotatedWith(HasImpression.class).toInstance(hasImpression);
    bind(new TypeLiteral<ImmutableList<String>>() {}).annotatedWith(ImpressionInterceptors.class)
        .toInstance(ImmutableList.copyOf(interceptors));
    bind(String.class).annotatedWith(PriceName.class).toInstance(priceName);
    if (hasImpression) {
      logger.info("Binding impression requests to: {}", path);
      bind(String.class).annotatedWith(ImpressionPath.class).toInstance(path);
      Multibinder.newSetBinder(binder(), Service.class).addBinding()
          .to(ImpressionController.class).in(Scopes.SINGLETON);
      Multibinder.newSetBinder(binder(), Feature.class).addBinding().toInstance(Feature.IMPRESSION);
      Multibinder.newSetBinder(binder(), HttpRoute.class).addBinding()
          .toProvider(ImpressionHttpRouteProvider.class).in(Scopes.SINGLETON);
    } else {
      logger.info("Impression request handling not installed");
    }
  }

  @Provides
  @Singleton
  @ImpressionInterceptors
  public ImmutableList<? extends ImpressionInterceptor> provideImpressionInterceptors(
      Injector injector,
      @ImpressionInterceptors ImmutableList<String> classNames) {

    return GuiceUtils.loadInstances(injector, ImpressionInterceptor.class, classNames);
  }

  @Provides
  @Singleton
  public ImpressionController provideImpressionController(
      MetricRegistry metricRegistry,
      @ImpressionInterceptors ImmutableList<? extends ImpressionInterceptor> interceptors) {

    return new ImpressionController(interceptors, metricRegistry);
  }

  protected static class ImpressionHttpRouteProvider extends AbstractHttpRouteProvider {
    @Inject
    private ImpressionHttpRouteProvider(
        @ImpressionPath String path,
        ImpressionRequestReceiver receiver) {
      super(HttpRoute.get("impression", path, receiver, Feature.IMPRESSION));
    }
  }
}
