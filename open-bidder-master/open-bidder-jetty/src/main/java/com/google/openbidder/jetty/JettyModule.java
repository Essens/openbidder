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

package com.google.openbidder.jetty;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import com.google.openbidder.config.server.BidderAdminPort;
import com.google.openbidder.config.server.BidderListenPort;
import com.google.openbidder.config.server.BindHost;
import com.google.openbidder.config.server.ContextRoot;
import com.google.openbidder.config.server.WebserverRuntime;
import com.google.openbidder.config.system.AvailableProcessors;
import com.google.openbidder.http.route.HttpRouter;
import com.google.openbidder.jetty.config.Acceptors;
import com.google.openbidder.jetty.config.IdleTimeout;
import com.google.openbidder.jetty.config.MaxThreads;
import com.google.openbidder.jetty.config.MinThreads;
import com.google.openbidder.jetty.config.Selectors;
import com.google.openbidder.servlet.ReceiverServlet;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Jetty HTTP server module.
 */
@Parameters(separators = "=")
public class JettyModule extends AbstractModule {

  private static final Logger logger = LoggerFactory.getLogger(JettyModule.class);

  @Parameter(names = "--min_threads",
      description = "Minimum number of server threads")
  private Integer minThreads;

  @Parameter(names = "--min_threads_per_cpu",
      description = "Minimum number of server threads per CPU")
  private Double minThreadsPerCpu;

  @Parameter(names = "--max_threads",
      description = "Maximum number of server threads")
  private Integer maxThreads;

  @Parameter(names = "--max_threads_per_cpu",
      description = "Maximum number of server threads per CPU")
  private Double maxThreadsPerCpu;

  @Parameter(names = "--idle_timeout", description = "Connection idle timeout in milliseconds")
  private int idleTimeout = IdleTimeout.DEFAULT;

  @Parameter(names = "--acceptors",
      description = "Connection acceptors (use -1 for Jetty's default)")
  private Integer acceptors;

  @Parameter(names = "--acceptors_per_cpu", description = "Connection acceptors per CPU")
  private Double acceptorsPerCpu;

  @Parameter(names = "--selectors",
      description = "Connection selectors (use -1 for Jetty's default)")
  private Integer selectors;

  @Parameter(names = "--selectors_per_cpu", description = "Connection selectors per CPU")
  private Double selectorsPerCpu;

  @Override
  protected void configure() {
    bind(Service.class).annotatedWith(WebserverRuntime.class)
        .to(JettyServer.class);
    bind(int.class).annotatedWith(IdleTimeout.class).toInstance(idleTimeout);
    Multibinder.newSetBinder(binder(), Connector.class)
        .addBinding().toProvider(HttpConnectorProvider.class).in(Scopes.SINGLETON);
    Multibinder.newSetBinder(binder(), Connector.class)
        .addBinding().toProvider(HttpAdminConnectorProvider.class).in(Scopes.SINGLETON);
    Multibinder.newSetBinder(binder(), ConnectionFactory.class)
        .addBinding().to(HttpConnectionFactory.class).in(Scopes.SINGLETON);
  }

  @Provides
  @Singleton
  public Server provideServer(ThreadPool threadPool, Handler handler) {
    Server server = new Server(threadPool);
    server.setHandler(handler);
    return server;
  }

  @Provides
  @Singleton
  public Handler provideHandler(
      @ContextRoot String contextPath,
      @BidderListenPort int listenPort,
      @BidderListenPort HttpRouter listenRouter,
      @BidderAdminPort int adminPort,
      @BidderAdminPort HttpRouter adminRouter) {

    ServletContextHandler handler = new ServletContextHandler();
    handler.setContextPath(contextPath);
    handler.addServlet(new ServletHolder("router", new ReceiverServlet(ImmutableMap.of(
        listenPort, listenRouter,
        adminPort, adminRouter))), "/*");
    return handler;
  }

  @Provides
  @Singleton
  @MinThreads
  public int provideMinimumThreads(@AvailableProcessors int availableProcessors) {
    checkArgument(minThreads == null || minThreadsPerCpu == null,
        "Cannot provide both --min_threads and --min_threads_per_cpu");
    return poolSize(minThreads, minThreadsPerCpu, availableProcessors, MinThreads.DEFAULT);
  }

  @Provides
  @Singleton
  @MaxThreads
  public int provideMaximumThreads(@AvailableProcessors int availableProcessors) {
    checkArgument(maxThreads == null || maxThreadsPerCpu == null,
        "Cannot provide both --max_threads and --max_threads_per_cpu");
    return poolSize(maxThreads, maxThreadsPerCpu, availableProcessors, MaxThreads.DEFAULT);
  }

  @Provides
  @Singleton
  @Acceptors
  public int provideAcceptors(@AvailableProcessors int availableProcessors) {
    checkArgument(acceptors == null || acceptorsPerCpu == null,
        "Cannot provide both --acceptors and --acceptors_per_cpu");
    return poolSize(acceptors, acceptorsPerCpu, availableProcessors, Acceptors.DEFAULT);
  }

  @Provides
  @Singleton
  @Selectors
  public int provideSelectors(@AvailableProcessors int availableProcessors) {
    checkArgument(selectors == null || selectorsPerCpu == null,
        "Cannot provide both --selectors and --selectors_per_cpu");
    return poolSize(selectors, selectorsPerCpu, availableProcessors, Selectors.DEFAULT);
  }

  @Provides
  @Singleton
  public ThreadPool provideThreadPool(
      @MinThreads int minThreads,
      @MaxThreads int maxThreads,
      @IdleTimeout int idleTimeout) {

    logger.info("Thread pool minimum threads: {}, maximum threads: {}, idle timeout: {} ms",
        minThreads, maxThreads, idleTimeout);
    return new QueuedThreadPool(maxThreads, minThreads, idleTimeout);
  }

  private static int poolSize(Integer absolute, Double relative, int relBase, int defAbsValue) {
    return absolute == null
        ? relative == null ? defAbsValue : (int) Math.round(relative * relBase)
        : absolute;
  }

  private static int poolSize(Integer absolute, Double relative, int relBase, double defRelValue) {
    return absolute == null
        ? (int) Math.round(relative == null ? defRelValue : relative) * relBase
        : absolute;
  }

  public static class HttpConnectorProvider implements Provider<Connector> {
    private final Server server;
    private final String host;
    private final int port;
    private final int acceptors;
    private final int selectors;
    private final ImmutableSet<ConnectionFactory> connectionFactories;

    @Inject
    HttpConnectorProvider(
        Server server,
        @BindHost String host,
        @BidderListenPort int port,
        @Selectors int selectors,
        @Acceptors int acceptors,
        Set<ConnectionFactory> connectionFactories) {

      this.server = checkNotNull(server);
      this.host = checkNotNull(host);
      this.port = port;
      this.selectors = selectors;
      this.acceptors = acceptors;
      this.connectionFactories = ImmutableSet.copyOf(connectionFactories);
    }

    @Override
    public Connector get() {
      ServerConnector connector = new ServerConnector(
          server,
          /* executor */ null,
          /* scheduler */ null,
          /* byte buffer pool */ null,
          acceptors,
          selectors,
          Iterables.toArray(connectionFactories, ConnectionFactory.class));
      connector.setName("http");
      if (!Strings.isNullOrEmpty(host)) {
        connector.setHost(host);
      }
      connector.setPort(port);
      logger.info("HTTP connector created with acceptors: {}, selectors: {}",
          connector.getAcceptors(), connector.getSelectorManager().getSelectorCount());
      logger.info("HTTP connector bound to host: {}, port: {}", host, port);
      logger.info("HTTP connection factories: {}", connectionFactories);
      return connector;
    }
  }

  public static class HttpAdminConnectorProvider implements Provider<Connector> {
    private final Server server;
    private final String host;
    private final int port;
    private final ImmutableSet<ConnectionFactory> connectionFactories;

    @Inject
    HttpAdminConnectorProvider(
        Server server,
        @BindHost String host,
        @BidderAdminPort int port,
        Set<ConnectionFactory> connectionFactories) {

      this.server = checkNotNull(server);
      this.host = checkNotNull(host);
      this.port = port;
      this.connectionFactories = ImmutableSet.copyOf(connectionFactories);
    }

    @Override
    public Connector get() {
      logger.info("HTTP Admin connector created with acceptors: {}, selectors: {}", 1, 1);
      logger.info("HTTP Admin connector bound to host: {}, port: {}", host, port);
      logger.info("HTTP Admin connection factories: {}", connectionFactories);
      ServerConnector connector = new ServerConnector(
          server,
          /* executor */ null,
          /* scheduler */ null,
          /* byte buffer pool */ null,
          /* acceptors */ 1,
          /* selectors */ 1,
          Iterables.toArray(connectionFactories, ConnectionFactory.class));
      connector.setName("http-admin");
      if (!Strings.isNullOrEmpty(host)) {
        connector.setHost(host);
      }
      connector.setPort(port);
      return connector;
    }
  }
}
