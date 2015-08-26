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

package com.google.openbidder.cloudstorage.impl;

import com.google.api.client.http.HttpHeaders;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;

import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Arrays;
import java.util.Map;
import java.util.TimeZone;

import javax.annotation.Nullable;

/**
 * Utilities to help Google Cloud Storage client.
 */
public class GoogleCloudStorageUtil {
  // Jodatime does not parse timezones represented with characters, so that is not included in
  // the input format.
  private static final String HTTP_DATE_FORMAT_INPUT = "EEE, dd MMM yyyy HH:mm:ss";
  private static final String HTTP_DATE_FORMAT = HTTP_DATE_FORMAT_INPUT + " z";
  private static final String CUSTOM_METADATA_PREFIX = "x-goog-meta-";

  private GoogleCloudStorageUtil() {
  }

  /**
   * Parses last modified from HTTP response header.
   */
  public static Instant parseLastModified(HttpHeaders httpResponseHeader) {
    return parseLastModified(httpResponseHeader.getLastModified());
  }

  /**
   * Converts last modified string to an Instant object.
   */
  public static Instant parseLastModified(String lastModified) {
    String[] dateParts = lastModified.split("\\s+");
    DateTimeZone timeZone = DateTimeZone.forTimeZone(
        TimeZone.getTimeZone(dateParts[dateParts.length - 1]));
    DateTimeFormatter formatter =
        DateTimeFormat.forPattern(HTTP_DATE_FORMAT_INPUT).withZone(timeZone);

    String lastModifiedWithoutTimeZone = Joiner.on(" ").
        join(Arrays.copyOfRange(dateParts, 0, dateParts.length - 1));
    return Instant.parse(lastModifiedWithoutTimeZone, formatter);
  }

  /**
   * Converts an Instant object to a formatted string.
   */
  public static String instantToLastModifiedString(Instant instant) {
    return DateTimeFormat.forPattern(HTTP_DATE_FORMAT).print(instant);
  }

  /**
   * Sets HTTP headers with custom metadata.
   */
  public static void setCustomMetadata(
      HttpHeaders httpHeaders,
      Map<String, Object> customMetadata) {

    for (Map.Entry<String, Object> entry : customMetadata.entrySet()) {
      httpHeaders.set(CUSTOM_METADATA_PREFIX + entry.getKey(), entry.getValue());
    }
  }

  /**
   * Gets custom metadata from HTTP headers.
   */
  public static Map<String, Object> getCustomMetadata(HttpHeaders httpHeaders) {
    ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
    for (Map.Entry<String, Object> entry : httpHeaders.entrySet()) {
      if (entry.getKey().startsWith(CUSTOM_METADATA_PREFIX)) {
        builder.put(entry.getKey().substring(CUSTOM_METADATA_PREFIX.length()), entry.getValue());
      }
    }
    return builder.build();
  }

  /**
   * Clean up a bucket URI that may come from user input with problems like undesired final slash
   * or heading/trailing spaces.
   */
  public static String cleanupBucketUri(@Nullable String uri) {
    return uri == null
        ? null
        : uri.replaceAll("/*$", "");
  }
}
