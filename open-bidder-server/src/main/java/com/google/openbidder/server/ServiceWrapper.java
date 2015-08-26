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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.Service;
import com.google.inject.ConfigurationException;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;
import com.google.openbidder.config.server.WebserverRuntime;
import com.google.openbidder.flags.FlagsModuleBuilder;
import com.google.openbidder.metrics.ServiceHealthCheck;
import com.google.openbidder.util.ReflectionUtils;

import com.codahale.metrics.health.HealthCheckRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Handler;
import java.util.logging.LogManager;

import javax.annotation.Nullable;

/**
 * Base class for a {@link Service} that is a "top-level" service. It may (and probably will)
 * contain child services that are started and stopped with this, the parent service.
 */
public abstract class ServiceWrapper extends AbstractIdleService {
  private static final Logger logger = LoggerFactory.getLogger(ServiceWrapper.class);

  private final String[] args;
  private @Nullable Thread shutdownHook;
  private Set<Service> services;
  private Service openBidderServer;
  private Runtime runtime = Runtime.getRuntime();

  static {
    java.util.logging.Logger rootLoggerJdk = LogManager.getLogManager().getLogger("");
    for (Handler handler : rootLoggerJdk.getHandlers()) {
      rootLoggerJdk.removeHandler(handler);
    }
    SLF4JBridgeHandler.install();
  }

  public ServiceWrapper(String[] args) {
    this.args = Arrays.copyOf(args, args.length);
  }

  protected final void setRuntime(@Nullable Runtime runtime) {
    this.runtime = runtime;
  }

  protected void main() {
    Thread.currentThread().setName("main");

    try {
      logger.info("Bidder initializing...");
      startAsync().awaitRunning();
    } finally {
      if (isRunning()) {
        if (isAlive()) {
          logger.info("Bidder initialization complete.");
          return;
        }
      } else {
        //logger.error("Bidder initialization failure: {}", this.failureCause());
      }
    }

    if (runtime != null) {
      runtime.exit(1);
    }
  }

  public boolean isAlive() {
    return shutdownHook != null;
  }

  /**
   * Forces the server to a complete stop.
   */
  @Override
  protected void shutDown() {
    logger.info("Bidder shutdown initiated...");

    try {
      openBidderServer.stopAsync().awaitTerminated();
    } finally {
      if (!applyAndWait(services, Service.State.TERMINATED, ServiceUtil.STOP)) {
        logger.error("Bidder shutdown failed to stop services.");
      }
    }
  }

  /**
   * Starts the server.
   */
  @Override
  protected void startUp() {
    Injector injector;

    try {
      Module rootModule = new FlagsModuleBuilder().addModules(getModules()).build(args);
      injector = Guice.createInjector(Stage.PRODUCTION, rootModule);
    } catch (CreationException e) {
      if (!(e.getCause() instanceof FlagsModuleBuilder.HelpException)) {
        logger.error("Startup error", e);
      }
      return;
    }

    @SuppressWarnings("unchecked")
    TypeLiteral<Set<Service>> typeLiteral = (TypeLiteral<Set<Service>>)
        TypeLiteral.get(Types.newParameterizedType(Set.class, Service.class));
    services = injector.getInstance(Key.get(typeLiteral));
    openBidderServer = injector.getInstance(Key.get(Service.class, WebserverRuntime.class));

    Runtime.getRuntime().addShutdownHook(shutdownHook = new Thread() {
      @Override public void run() {
        stopAsync().awaitTerminated();
      }
    });

    if (!services.isEmpty()) {
      logger.info("Services installed: {}",
          Joiner.on(", ").join(Iterables.transform(services, ServiceUtil.NAME)));
    }

    if (applyAndWait(services, Service.State.RUNNING, ServiceUtil.START)) {
      openBidderServer.startAsync().awaitRunning();
      logger.info("Services initialized successfully.");

      try {
        HealthCheckRegistry healthCheckRegistry = injector.getInstance(HealthCheckRegistry.class);

        for (Service service : Iterables.concat(services, ImmutableSet.of(openBidderServer))) {
          healthCheckRegistry.register(
              ReflectionUtils.getPrettyName(service.getClass()), new ServiceHealthCheck(service));
        }

        logger.info("Service HealthChecks installed");
      } catch (ConfigurationException e) {
        logger.info("service HealthChecks not installed");
      }
    } else {
      throw new IllegalStateException("Services initialization failed");
    }
  }

  private boolean applyAndWait(
      Collection<Service> services,
      Service.State expectedState,
      Function<Service, Future<Service.State>> function) {

    boolean success = true;

    try {
      Collection<Future<Service.State>> futures = Collections2.transform(services, function);
      for (Future<Service.State> future : futures) {
        try {
          if (future.get() != expectedState) {
            success = false;
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          logger.warn("Service control failure", e);
        }
      }
    } catch (ExecutionException e) {
      logger.warn("Service control failure", e);
    }

    return success;
  }

  protected abstract List<Module> getModules();
}
