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

package com.google.openbidder.exchange.doubleclick.match;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.openbidder.api.match.MatchController;
import com.google.openbidder.api.match.MatchInterceptor;
import com.google.openbidder.config.http.Feature;
import com.google.openbidder.config.match.HasMatch;
import com.google.openbidder.exchange.doubleclick.config.DoubleClickMatchInterceptors;
import com.google.openbidder.exchange.doubleclick.config.DoubleClickMatchRedirectUrl;
import com.google.openbidder.exchange.doubleclick.config.DoubleClickMatchRequestPath;
import com.google.openbidder.exchange.doubleclick.config.DoubleClickNid;
import com.google.openbidder.flags.util.UriConverter;
import com.google.openbidder.flags.util.UriValidator;
import com.google.openbidder.http.route.AbstractHttpRouteProvider;
import com.google.openbidder.http.route.HttpRoute;
import com.google.openbidder.http.util.HttpUtil;
import com.google.openbidder.util.GuiceUtils;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.codahale.metrics.MetricRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * DoubleClick Ad Exchange cookie match support.
 */
@Parameters(separators = "=")
public class DoubleClickMatchModule extends AbstractModule {

  private static final Logger logger = LoggerFactory.getLogger(DoubleClickMatchModule.class);

  @Parameter(names = "--doubleclick_match_path",
      description = "Path spec for DoubleClick cookie match requests")
  private String path = DoubleClickMatchRequestPath.DEFAULT;

  @Parameter(names = "--doubleclick_match_url",
      description = "Redirect URL for DoubleClick cookie match requests",
      validateWith = UriValidator.class,
      converter = UriConverter.class)
  private URI redirectUri = HttpUtil.buildUri(DoubleClickMatchRedirectUrl.DEFAULT);

  @Parameter(names = "--doubleclick_match_nid",
      description = "DoubleClick NID for cookie match requests")
  private String nid;

  @Parameter(names = "--doubleclick_match_interceptors",
      description = "Class names for DoubleClick match interceptors")
  private List<String> interceptors = new ArrayList<>();

  @Override
  protected void configure() {
    boolean doubleClickMatchEnabled = !interceptors.isEmpty();
    bind(boolean.class).annotatedWith(HasMatch.class).toInstance(doubleClickMatchEnabled);
    bind(new TypeLiteral<ImmutableList<String>>() {})
        .annotatedWith(DoubleClickMatchInterceptors.class)
        .toInstance(ImmutableList.copyOf(interceptors));
    if (doubleClickMatchEnabled) {
      checkArgument(!Strings.isNullOrEmpty(nid),
          "--doubleclick_match_nid required if --doubleclick_match_interceptors set");
      logger.info("Binding DoubleClick cookie matching requests to: {}", path);
      bind(String.class).annotatedWith(DoubleClickMatchRequestPath.class).toInstance(path);
      bind(String.class).annotatedWith(DoubleClickNid.class).toInstance(nid);
      bind(URI.class).annotatedWith(DoubleClickMatchRedirectUrl.class)
          .toInstance(redirectUri);
      Multibinder.newSetBinder(binder(), Service.class).addBinding()
          .to(MatchController.class).in(Scopes.SINGLETON);
      Multibinder.newSetBinder(binder(), Feature.class).addBinding().toInstance(Feature.MATCH);
      Multibinder.newSetBinder(binder(), HttpRoute.class).addBinding()
          .toProvider(MatchRequestHttpRouteProvider.class).in(Scopes.SINGLETON);
    } else {
      logger.info("DoubleClick cookie match request handling not installed");
    }
  }

  @Provides
  @Singleton
  @DoubleClickMatchInterceptors
  @SuppressWarnings("unchecked")
  public ImmutableList<MatchInterceptor> provideMatchInterceptors(
      Injector injector,
      @DoubleClickMatchInterceptors ImmutableList<String> classNames) {

    // This cast doesn't need to be legal; see note in MatchInterceptor. No framework code
    // will ever cast individual interceptors to the exchange-specific interface.
    return (ImmutableList<MatchInterceptor>)
        GuiceUtils.loadInstances(injector, MatchInterceptor.class, classNames);
  }

  @Provides
  @Singleton
  public MatchController provideMatchController(
      MetricRegistry metricRegistry,
      @DoubleClickMatchInterceptors
        ImmutableList<MatchInterceptor> interceptors) {

    return new MatchController(interceptors, metricRegistry);
  }

  protected static class MatchRequestHttpRouteProvider extends AbstractHttpRouteProvider {
    @Inject
    private MatchRequestHttpRouteProvider(
        DoubleClickMatchRequestReceiver receiver,
        @DoubleClickMatchRequestPath String path) {
      super(HttpRoute.get("match_doubleclick", path, receiver, Feature.MATCH));
    }
  }
}
