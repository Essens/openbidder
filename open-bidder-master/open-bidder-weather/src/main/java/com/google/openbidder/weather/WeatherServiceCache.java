/*
 * Copyright 2012 Google Inc. All Rights Reserved.
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

package com.google.openbidder.weather;

import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.Futures;
import com.google.openbidder.weather.model.Weather.WeatherConditions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nullable;

/**
 * Caches weather lookup to avoid latency during requests.
 * <p>
 * The implementation here is too simple for a production service: we just populate the cache
 * on demand, the first time each location is requested. Entries are expired after some time,
 * because weather conditions change.  Ideally, we should also preload the weather conditions
 * for all locations we're expected to serve, so the very first request to each location
 * wouldn't pay the price of a slow call to the weather-reporting service.
 */
public class WeatherServiceCache implements WeatherService {
  private static final Logger logger = LoggerFactory.getLogger(WeatherServiceCache.class);
  private final Integer deadlineMs;
  private final LoadingCache<String, Future<WeatherConditions>> cache;

  public WeatherServiceCache(
      Integer deadlineMs, final WeatherService weatherSource, final ExecutorService executor) {
    this.deadlineMs = deadlineMs;
    this.cache = CacheBuilder.newBuilder()
        .refreshAfterWrite(2, TimeUnit.HOURS)
        .maximumSize(1000000)
        .build(new CacheLoader<String, Future<WeatherConditions>>() {
          @Override public Future<WeatherConditions> load(final String location) {
            try {
              return executor.submit(new Callable<WeatherConditions>() {
                @Override public WeatherConditions call() throws Exception {
                  return weatherSource.getWeatherConditions(location);
              }});
            } catch (RejectedExecutionException e) {
              logger.warn("{}: {}", location, e.toString());
              return Futures.immediateFuture(null);
            }
          }});
  }

  public long size() {
    return cache.size();
  }

  @Override
  public @Nullable WeatherConditions getWeatherConditions(String location) {
    try {
      Stopwatch stopwatch = Stopwatch.createStarted();
      Future<WeatherConditions> future = cache.getUnchecked(location);
      WeatherConditions ret = deadlineMs == null
          ? (future.isDone() ? future.get() : null)
          : future.get(deadlineMs, TimeUnit.MILLISECONDS);

      if (ret != null && logger.isDebugEnabled()) {
        logger.debug("{}: Obtained weather conditions in {}ms:\n{}",
            location, stopwatch.elapsed(TimeUnit.MILLISECONDS), ret);
      }

      return ret;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return null;
    } catch (ExecutionException | TimeoutException e) {
      logger.warn("{}: {}", location, e.toString());
      return null;
    }
  }
}
