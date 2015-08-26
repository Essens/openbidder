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

package com.google.openbidder.weather.config;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.common.base.Strings;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.util.Providers;
import com.google.openbidder.cloudstorage.GoogleCloudStorage;
import com.google.openbidder.weather.WeatherDao;
import com.google.openbidder.weather.WeatherDaoCloudStorage;
import com.google.openbidder.weather.WeatherInterceptor;
import com.google.openbidder.weather.WeatherService;
import com.google.openbidder.weather.WeatherServiceCache;
import com.google.openbidder.weather.WeatherServiceOpenWeatherMap;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
import javax.inject.Singleton;

/**
 * Module for the Weather Interceptor.
 */
@Parameters(separators = "=")
public class WeatherModule extends AbstractModule {
  private static final Logger logger = LoggerFactory.getLogger(WeatherModule.class);

  @Parameter(names = "--weather_bucket", required = false,
      description = "Google Cloud Storage bucket where Weather rules are to be loaded from")
  private String storageBucket;
  @Parameter(names = "--weather_cache_timeout", required = false,
      description = "Deadline for blocking reads to the weather data cache (ms; 0=never block)")
  private Integer cacheTimeout;

  @Override
  protected void configure() {
    if (!Strings.isNullOrEmpty(storageBucket)) {
      bind(String.class).annotatedWith(WeatherStorageBucket.class).toInstance(storageBucket);
      bind(Integer.class).annotatedWith(WeatherCacheTimeout.class)
          .toProvider(Providers.of(cacheTimeout));
      install(new ImplModule());
      logger.info("Weather module installed, bucket: {}", storageBucket);
    } else {
      logger.info("Weather module not installed");
    }
  }

  private static class ImplModule extends AbstractModule {
    @Override protected void configure() {}

    @Provides @Singleton
    public WeatherDao provideWeatherDao(
        GoogleCloudStorage cloudStorage,
        @WeatherStorageBucket String storageBucket) {
      return new WeatherDaoCloudStorage(cloudStorage, storageBucket);
    }

    @Provides @Singleton
    public WeatherService provideWeatherService(
        JsonFactory jsonFactory,
        HttpTransport httpTransport,
        MetricRegistry metricRegistry,
        @Nullable @WeatherCacheTimeout Integer cacheTimeout) {
      final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
      metricRegistry.register(
          MetricRegistry.name(WeatherInterceptor.class, "CacheQueue"),
          new Gauge<Integer>() {
            @Override public Integer getValue() { return queue.size(); }
          });
      final WeatherServiceCache cache = new WeatherServiceCache(
          cacheTimeout,
          new WeatherServiceOpenWeatherMap(jsonFactory, httpTransport),
          // Limit threads to not overload the server
          new ThreadPoolExecutor(1, 8, 60L, TimeUnit.SECONDS, queue));
      metricRegistry.register(
          MetricRegistry.name(WeatherInterceptor.class, "CacheSize"),
          new Gauge<Long>() {
            @Override public Long getValue() { return cache.size(); }
          });
      return cache;
    }
  }
}
