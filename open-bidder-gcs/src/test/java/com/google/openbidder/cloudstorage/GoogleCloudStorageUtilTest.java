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

package com.google.openbidder.cloudstorage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.api.client.http.HttpHeaders;
import com.google.openbidder.cloudstorage.impl.GoogleCloudStorageUtil;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for {@link GoogleCloudStorageUtil}.
 */
public class GoogleCloudStorageUtilTest {

  @Test
  public void testLastModifiedParsing() {
    HttpHeaders headers = new HttpHeaders();
    headers.setLastModified("Fri, 19 Feb 2010 22:04:23 GMT");
    Instant expectedLastModified =
        new DateTime(2010, 2, 19, 22, 4, 23, DateTimeZone.UTC).toInstant();
    assertEquals(expectedLastModified, GoogleCloudStorageUtil.parseLastModified(headers));
  }

  @Test
  public void testLastModifiedToString() {
    Instant lastModified = new DateTime(2010, 2, 19, 22, 4, 23, DateTimeZone.UTC).toInstant();
    assertEquals("Fri, 19 Feb 2010 22:04:23 UTC",
        GoogleCloudStorageUtil.instantToLastModifiedString(lastModified));
  }

  @Test
  public void testCustomMetadata() {
    HttpHeaders expectedHeaders = new HttpHeaders();
    HttpHeaders actualHeaders = new HttpHeaders();
    Map<String, Object> actualMetadata = new HashMap<>();
    Map<String, Object> expectedMetadata = new HashMap<>();
    GoogleCloudStorageUtil.setCustomMetadata(actualHeaders, actualMetadata);

    assertEquals(expectedHeaders, actualHeaders);
    actualMetadata = GoogleCloudStorageUtil.getCustomMetadata(actualHeaders);
    assertEquals(expectedMetadata, actualMetadata);

    expectedMetadata.put("field-name", "field value");
    expectedHeaders.put("x-goog-meta-field-name", "field value");
    GoogleCloudStorageUtil.setCustomMetadata(actualHeaders, expectedMetadata);
    assertEquals(expectedHeaders, actualHeaders);
    actualMetadata = GoogleCloudStorageUtil.getCustomMetadata(actualHeaders);
    assertEquals(expectedMetadata, actualMetadata);
  }

  @Test
  public void testCleanupBucket() {
    assertEquals("gs://files", GoogleCloudStorageUtil.cleanupBucketUri("gs://files"));
    assertEquals("gs://files", GoogleCloudStorageUtil.cleanupBucketUri("gs://files/"));
    assertNull(GoogleCloudStorageUtil.cleanupBucketUri(null));
  }
}
