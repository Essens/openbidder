package com.google.openbidder.weather;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.openbidder.weather.model.Weather.WeatherConditions;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Tests for {@link WeatherServiceOpenWeatherMap}.
 */
public class WeatherServiceOpenWeatherMapTest {
  private static final Logger logger =
      LoggerFactory.getLogger(WeatherServiceOpenWeatherMapTest.class);
  private static final String LOCATION = "New York, US";
  private static final Integer TIMEOUT = 200;

  /** Enable this via JVM property for testing; potentially slow/flaky. */
  private static final boolean ENABLED = Boolean.getBoolean("OpenWeatherMapTest");

  private static WeatherService service;

  @BeforeClass
  public static void startUp() {
    if (!ENABLED) { return; }

    service = new WeatherServiceOpenWeatherMap(new JacksonFactory(), new NetHttpTransport());
  }

  @Test
  public void testConditions() {
    if (!ENABLED) { return; }

    WeatherConditions cond = service.getWeatherConditions(LOCATION);
    assertNotNull(cond);
    assertNotNull(cond.getTempFahrenheit());
    assertNotNull(cond.getHumidityPercent());
    assertNotNull(cond.getWindMph());
  }

  @Test
  public void testCache() {
    if (!ENABLED) { return; }

    WeatherService cache = new WeatherServiceCache(
        TIMEOUT, service, MoreExecutors.newDirectExecutorService());
    Stopwatch stopwatch1 = Stopwatch.createStarted();
    assertNotNull(cache.getWeatherConditions(LOCATION));
    logger.info("Weather lookup time (cache miss): {}ms",
        stopwatch1.elapsed(TimeUnit.MILLISECONDS));

    Stopwatch stopwatch2 = Stopwatch.createStarted();
    assertNotNull(cache.getWeatherConditions(LOCATION));
    long time2 = stopwatch2.elapsed(TimeUnit.MILLISECONDS);
    logger.info("Weather lookup time (cache hit): {}ms", time2);
    if (time2 >= 1000) {
      logger.warn("That was slow!");
    }
  }

  @Test
  public void testCacheTimeout() throws InterruptedException {
    if (!ENABLED) { return; }

    WeatherService cache = new WeatherServiceCache(
        0, service, Executors.newFixedThreadPool(1, new ThreadFactory() {
          @Override public Thread newThread(final Runnable r) {
            return new Thread() {
              @Override public void run() {
                try { sleep(20L); } catch (InterruptedException e) {}
                r.run();
              }};
          }}));

    assertNull(cache.getWeatherConditions(LOCATION));

    Thread.sleep(TIMEOUT);
    assertNotNull(cache.getWeatherConditions(LOCATION));
  }
}
