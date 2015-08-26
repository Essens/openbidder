package com.google.openbidder.weather;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonParser;
import com.google.api.client.util.escape.CharEscapers;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.openbidder.weather.model.Weather.WeatherConditions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;

import javax.annotation.Nullable;

/**
 * Obtains weather data from OpenWeatherMap.
 */
public class WeatherServiceOpenWeatherMap implements WeatherService {
  private static final Logger logger = LoggerFactory.getLogger(WeatherServiceOpenWeatherMap.class);
  private static final String API_URL =
      "http://api.openweathermap.org/data/2.5/weather?units=imperial&q={0}";
  private static final ImmutableSet<String> FIELDS_ROOT = ImmutableSet.of("main", "wind");
  private static final ImmutableSet<String> FIELDS_MAIN = ImmutableSet.of("temp", "humidity");
  private static final ImmutableSet<String> FIELDS_WIND = ImmutableSet.of("speed");

  private final JsonFactory jsonFactory;
  private final HttpTransport httpTransport;

  public WeatherServiceOpenWeatherMap(JsonFactory jsonFactory, HttpTransport httpTransport) {
    this.jsonFactory = jsonFactory;
    this.httpTransport = httpTransport;
  }

  @Override
  public @Nullable WeatherConditions getWeatherConditions(String location) {
    WeatherConditions.Builder cond = WeatherConditions.newBuilder();

    try (InputStream response = getWeatherApiResponse(location)) {
      JsonParser parser = jsonFactory.createJsonParser(response);
      String rootKey;

      do {
        String fieldKey;
        switch (Strings.nullToEmpty(rootKey = parser.skipToKey(FIELDS_ROOT))) {
          case "main":
            do {
              switch (Strings.nullToEmpty(fieldKey = parser.skipToKey(FIELDS_MAIN))) {
                case "temp":
                  cond.setTempFahrenheit((int) parser.getFloatValue());
                  break;

                case "humidity":
                  cond.setHumidityPercent(parser.getIntValue() / 100.0);
                  break;
              }
            } while (fieldKey != null && parser.nextToken() != null);
            break;

          case "wind":
            do {
              switch (Strings.nullToEmpty(fieldKey = parser.skipToKey(FIELDS_WIND))) {
                case "speed":
                  cond.setWindMph((int) parser.getFloatValue());
                  break;
              }
            } while (fieldKey != null && parser.nextToken() != null);
            break;
        }
      } while (rootKey != null && parser.nextToken() != null);
    } catch (IOException e) {
      logger.warn("{}: {}", location, e.toString());
      return null;
    }

    if (logger.isDebugEnabled() && cond.isInitialized()) {
      logger.debug("{}: {}F, {}mph, {}%", location,
          cond.getTempFahrenheit(), cond.getWindMph(), cond.getHumidityPercent());
    }
    return cond.isInitialized() ? cond.build() : null;
  }

  /**
   * Retrieve the Weather API response.
   * @return An {@link InputStream} containing the API response data.
   */
  protected InputStream getWeatherApiResponse(String location) throws IOException {
    HttpRequest request = httpTransport.createRequestFactory()
      .buildGetRequest(new GenericUrl(MessageFormat.format(
          API_URL, CharEscapers.escapeUriQuery(location))));
    return request.execute().getContent();
  }
}
