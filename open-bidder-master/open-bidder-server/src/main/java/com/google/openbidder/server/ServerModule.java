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

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;
import com.google.openbidder.config.http.Feature;
import com.google.openbidder.config.http.Http404Receiver;
import com.google.openbidder.config.http.Http405Receiver;
import com.google.openbidder.config.http.HttpOptionsReceiver;
import com.google.openbidder.config.server.BidderAdminPort;
import com.google.openbidder.config.server.BidderListenPort;
import com.google.openbidder.config.server.BindHost;
import com.google.openbidder.config.server.ContextRoot;
import com.google.openbidder.config.server.MaxContentLength;
import com.google.openbidder.config.server.ServerLogging;
import com.google.openbidder.http.HttpReceiver;
import com.google.openbidder.http.route.HttpRoute;
import com.google.openbidder.http.route.HttpRouter;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import javax.inject.Singleton;

/**
 * Bidder HTTP server configuration.
 */
@Parameters(separators = "=")
public class ServerModule extends AbstractModule {
  private static final Logger logger = LoggerFactory.getLogger(ServerModule.class);

  @Parameter(names = "--bind_host", description = "Bind host name")
  private String bindHost = BindHost.DEFAULT;

  @Parameter(names = "--listen_port", description = "Bind port number")
  private int listenPort = BidderListenPort.DEFAULT;

  @Parameter(names = "--admin_port", description = "Bind port number (admin)")
  private int adminPort = BidderAdminPort.DEFAULT;

  @Parameter(names = "--context_root", description = "Context root")
  private String contextRoot = ContextRoot.DEFAULT;

  @Parameter(names = "--max_content_length", description = "Maximum content length in bytes")
  private int maxContentLength = MaxContentLength.DEFAULT;

  @Parameter(names = "--server_logging", description = "Enable detailed server logging")
  private boolean serverLogging;

  @Override
  protected void configure() {
    logger.info("Bind host: {}", bindHost);
    logger.info("Listen port: {}", listenPort);
    logger.info("Admin port: {}", adminPort);
    logger.info("Context root: {}", contextRoot);
    logger.info("Maximum content length: {}", maxContentLength);
    logger.info("Server logging: {}", serverLogging);
    bind(String.class).annotatedWith(BindHost.class).toInstance(bindHost);
    bind(Integer.class).annotatedWith(BidderListenPort.class).toInstance(listenPort);
    bind(Integer.class).annotatedWith(BidderAdminPort.class).toInstance(adminPort);
    bind(String.class).annotatedWith(ContextRoot.class).toInstance(contextRoot);
    bind(int.class).annotatedWith(MaxContentLength.class).toInstance(maxContentLength);
    bind(long.class).annotatedWith(MaxContentLength.class).toInstance((long) maxContentLength);
    bind(boolean.class).annotatedWith(ServerLogging.class).toInstance(serverLogging);
    Multibinder.newSetBinder(binder(), Service.class);
    Multibinder.newSetBinder(binder(), Feature.class);
  }

  @Provides
  @Singleton
  @BidderListenPort
  public HttpRouter provideListenRouter(
      Set<HttpRoute> httpRoutes,
      Set<Feature> enabledFeatures,
      @HttpOptionsReceiver HttpReceiver optionsReceiver,
      @Http404Receiver HttpReceiver notFoundReceiver,
      @Http405Receiver HttpReceiver methodNotAllowedReceiver) {

    return new HttpRouter(
        filterByFeatures(httpRoutes, false),
        Sets.difference(enabledFeatures, ImmutableSet.of(Feature.ADMIN)),
        optionsReceiver,
        notFoundReceiver,
        methodNotAllowedReceiver);
  }

  @Provides
  @Singleton
  @BidderAdminPort
  public HttpRouter provideAdminRouter(
      Set<HttpRoute> httpRoutes,
      @HttpOptionsReceiver HttpReceiver optionsReceiver,
      @Http404Receiver HttpReceiver notFoundReceiver,
      @Http405Receiver HttpReceiver methodNotAllowedReceiver) {

    return new HttpRouter(
        filterByFeatures(httpRoutes, true),
        ImmutableSet.of(Feature.ADMIN),
        optionsReceiver,
        notFoundReceiver,
        methodNotAllowedReceiver);
  }

  private static Set<HttpRoute> filterByFeatures(Set<HttpRoute> httpRoutes, final boolean admin) {
    return Sets.filter(httpRoutes, new Predicate<HttpRoute>() {
      @Override public boolean apply(HttpRoute route) {
        assert route != null;
        return Iterables.any(route.getRequiredFeatures(), new Predicate<Feature>() {
          @Override public boolean apply(Feature feature) {
            assert feature != null;
            return feature.admin() == admin;
          }});
      }});
  }
}
