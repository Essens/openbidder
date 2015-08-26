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

package com.google.openbidder.metrics;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import com.google.openbidder.config.http.Feature;
import com.google.openbidder.config.template.AdminTemplate;
import com.google.openbidder.http.route.AbstractHttpRouteProvider;
import com.google.openbidder.http.route.HttpRoute;
import com.google.openbidder.http.template.Template;
import com.google.openbidder.http.template.TemplateEngine;
import com.google.openbidder.http.util.HttpUtil;
import com.google.openbidder.metrics.config.BasePath;
import com.google.openbidder.metrics.config.HealthCheckPath;
import com.google.openbidder.metrics.config.MetricsPath;
import com.google.openbidder.metrics.config.PingPath;
import com.google.openbidder.metrics.config.ThreadsPath;
import com.google.openbidder.metrics.http.AdminHttpReceiver;
import com.google.openbidder.metrics.http.HealthCheckHttpReceiver;
import com.google.openbidder.metrics.http.MetricsHttpReceiver;
import com.google.openbidder.metrics.http.PingHttpReceiver;
import com.google.openbidder.metrics.http.ThreadDumpHttpReceiver;
import com.google.openbidder.util.ReflectionUtils;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Guice module for installing instrumentation.
 */
@Parameters(separators = "=")
public class MetricsModule extends AbstractModule {
  private static final Logger logger = LoggerFactory.getLogger(MetricsModule.class);

  @Parameter(names = "--metrics", arity = 1,
      description = "Whether or not to install metrics handlers")
  private boolean metrics = true;

  @Parameter(names = "--metrics_base_path",
      description = "Context root for metrics pages")
  private String metricsBasePath = BasePath.DEFAULT;

  @Parameter(names = "--metrics_path",
      description = "Metrics Web page path under context root")
  private String metricsPath = MetricsPath.DEFAULT;

  @Parameter(names = "--health_check_path",
      description = "Metrics health check Web page path under context root")
  private String healthCheckPath = HealthCheckPath.DEFAULT;

  @Parameter(names = "--threads_path",
      description = "Metrics threads Web page path under context root")
  private String threadsPath = ThreadsPath.DEFAULT;

  @Parameter(names = "--ping_path",
      description = "Metrics ping Web page path under context root")
  private String pingPath = PingPath.DEFAULT;

  @Parameter(names = "--admin_template",
      description = "Admin template name")
  private String adminTemplate = AdminTemplate.DEFAULT;

  @Override
  protected void configure() {
    MetricRegistry metricRegistry = new MetricRegistry();
    metricRegistry.registerAll(new GarbageCollectorMetricSet());
    metricRegistry.registerAll(new ThreadStatesGaugeSet());
    metricRegistry.registerAll(new MemoryUsageGaugeSet());
    bind(MetricRegistry.class).toInstance(metricRegistry);

    if (metrics) {
      logger.info("Installing metrics support, base path: {}", metricsBasePath);
      bind(String.class).annotatedWith(BasePath.class).toInstance(metricsBasePath);
      String metricsPath = HttpUtil.concatPaths(metricsBasePath, this.metricsPath);
      String healthCheckPath = HttpUtil.concatPaths(metricsBasePath, this.healthCheckPath);
      String threadsPath = HttpUtil.concatPaths(metricsBasePath, this.threadsPath);
      String pingPath = HttpUtil.concatPaths(metricsBasePath, this.pingPath);
      bind(String.class).annotatedWith(MetricsPath.class).toInstance(metricsPath);
      bind(String.class).annotatedWith(HealthCheckPath.class).toInstance(healthCheckPath);
      bind(String.class).annotatedWith(ThreadsPath.class).toInstance(threadsPath);
      bind(String.class).annotatedWith(PingPath.class).toInstance(pingPath);
      bind(String.class).annotatedWith(AdminTemplate.class).toInstance(adminTemplate);
      bind(Template.class).annotatedWith(AdminTemplate.class)
          .toProvider(AdminTemplateProvider.class).in(Scopes.SINGLETON);

      Multibinder<HttpRoute> routeBinder = Multibinder.newSetBinder(binder(), HttpRoute.class);
      routeBinder.addBinding().toProvider(AdminHttpRouteProvider.class).in(Scopes.SINGLETON);
      routeBinder.addBinding().toProvider(MetricsHttpRouteProvider.class).in(Scopes.SINGLETON);
      routeBinder.addBinding().toProvider(PingHttpRouteProvider.class).in(Scopes.SINGLETON);
      routeBinder.addBinding().toProvider(HealthCheckHttpRouteProvider.class).in(Scopes.SINGLETON);
      routeBinder.addBinding().toProvider(ThreadDumpHttpRouteProvider.class).in(Scopes.SINGLETON);
      Multibinder.newSetBinder(binder(), Feature.class).addBinding().toInstance(Feature.ADMIN);
      Multibinder.newSetBinder(binder(), Feature.class).addBinding().toInstance(Feature.OTHER);
    } else {
      logger.info("Metrics support not installed");
    }
    Multibinder.newSetBinder(binder(), HealthCheck.class);
  }

  @Provides
  @Singleton
  public HealthCheckRegistry provideHealthCheckRegistry(Set<HealthCheck> healthChecks) {
    HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();

    for (HealthCheck healthCheck : healthChecks) {
      healthCheckRegistry.register(
          ReflectionUtils.getPrettyName(healthCheck.getClass()), healthCheck);
    }

    return healthCheckRegistry;
  }

  public static class AdminHttpRouteProvider extends AbstractHttpRouteProvider {
    @Inject
    private AdminHttpRouteProvider(@BasePath String path, AdminHttpReceiver receiver) {
      super(HttpRoute.get("admin", path, receiver, Feature.ADMIN));
    }
  }

  public static class PingHttpRouteProvider extends AbstractHttpRouteProvider {
    @Inject
    private PingHttpRouteProvider(@PingPath String path, PingHttpReceiver receiver) {
      super(HttpRoute.get("admin_ping", path, receiver, Feature.ADMIN, Feature.OTHER));
    }
  }

  public static class MetricsHttpRouteProvider extends AbstractHttpRouteProvider {
    @Inject
    private MetricsHttpRouteProvider(@MetricsPath String path, MetricsHttpReceiver receiver) {
      super(HttpRoute.get("admin_metrics", path, receiver, Feature.ADMIN));
    }
  }

  public static class HealthCheckHttpRouteProvider extends AbstractHttpRouteProvider {
    @Inject
    private HealthCheckHttpRouteProvider(
        @HealthCheckPath String path,
        HealthCheckHttpReceiver receiver) {
      super(HttpRoute.get("admin_health", path, receiver, Feature.ADMIN, Feature.OTHER));
    }
  }

  public static class ThreadDumpHttpRouteProvider extends AbstractHttpRouteProvider {
    @Inject
    private ThreadDumpHttpRouteProvider(@ThreadsPath String path, ThreadDumpHttpReceiver receiver) {
      super(HttpRoute.get("admin_threads", path, receiver, Feature.ADMIN));
    }
  }

  public static class AdminTemplateProvider implements Provider<Template> {
    private final TemplateEngine templateEngine;
    private final String adminTemplate;

    @Inject
    public AdminTemplateProvider(
        TemplateEngine templateEngine,
        @AdminTemplate String adminTemplate) {

      this.templateEngine = checkNotNull(templateEngine);
      this.adminTemplate = checkNotNull(adminTemplate);
    }

    @Override public Template get() {
      return templateEngine.load(adminTemplate);
    }
  }
}
