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

package com.google.openbidder.cloudstorage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.net.HttpHeaders;
import com.google.openbidder.cloudstorage.impl.GoogleCloudStorageUtil;
import com.google.openbidder.cloudstorage.model.ListAllMyBucketsResult;
import com.google.openbidder.cloudstorage.model.ListAllMyBucketsResult.Bucket;
import com.google.openbidder.cloudstorage.model.ListAllMyBucketsResult.Buckets;
import com.google.openbidder.cloudstorage.model.ListBucketResult;
import com.google.openbidder.util.Clock;
import com.google.openbidder.util.testing.FakeClock;
import com.google.openbidder.util.testing.TestUtil;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Unit tests for {@link GoogleCloudStorage}.
 */
public class GoogleCloudStorageTest {
  private static final Clock clock = new FakeClock(Instant.now(), Duration.millis(0));

  @Test
  public void testException() {
    TestUtil.testCommonException(GoogleCloudStorageException.class);
  }

  @Test
  public void testListBucketResult_commonMethods() {
    DateTime now = new DateTime(clock.now());
    ListBucketResult.Content content1 = new ListBucketResult.Content();
    content1.setKey("key");
    content1.setSize(0L);
    content1.setLastModified(now);
    ListBucketResult.Content content2 = new ListBucketResult.Content();
    content2.setKey("key");
    content2.setSize(0L);
    content2.setLastModified(now);
    ListBucketResult.Content content3 = new ListBucketResult.Content();
    content3.setKey("key");
    content3.setSize(0L);
    content3.setLastModified(new DateTime(clock.now().getMillis() + 1));

    TestUtil.testCommonMethods(content1, content2, content3);

    ListBucketResult list1 = new ListBucketResult();
    list1.setName("name");
    list1.setPrefix("prefix");
    list1.setContents(ImmutableList.of(content1));
    ListBucketResult list2 = new ListBucketResult();
    list2.setName("name");
    list2.setPrefix("prefix");
    list2.setContents(ImmutableList.of(content2));
    ListBucketResult list3 = new ListBucketResult();
    list3.setName("name");
    list3.setPrefix("prefix");
    list3.setContents(ImmutableList.of(content3));

    TestUtil.testCommonMethods(list1, list2, list3);
  }

  @Test
  public void testListObjectsInBucket() throws HttpResponseException {
    GoogleCloudStorage cloudStorage = createCloudStorageMock(
        "application/xml", "/google-cloud-storage/get-bucket-result.xml");
    ListBucketResult bucketResult = cloudStorage.listObjectsInBucket("test-bucket", "test-prefix");

    assertEquals("test-bucket", bucketResult.getName());
    assertEquals("test-prefix", bucketResult.getPrefix());
    assertFalse(bucketResult.isTruncated());
    assertEquals(2, bucketResult.getContents().size());

    ListBucketResult.Content content = bucketResult.getContents().get(0);
    assertEquals("test-host-1", content.getKey());
    assertEquals(Long.valueOf(38), content.getSize());
    assertEquals(ISODateTimeFormat.dateTime().parseDateTime("2012-02-10T18:11:07.655Z"),
        content.getLastModified());
    bucketResult.setContents(bucketResult.getContents());
    bucketResult.setName(bucketResult.getName());
    bucketResult.setPrefix(bucketResult.getPrefix());
    bucketResult.setTruncated(bucketResult.isTruncated());

    GoogleCloudStorage cloudStorage2 = createCloudStorageMock(
        "application/xml", "/google-cloud-storage/get-bucket-result.xml");
    ListBucketResult bucketResult2 = cloudStorage2.listObjectsInBucket("test-bucket", null);
    assertEquals("test-bucket", bucketResult2.getName());
    assertEquals("test-prefix", bucketResult2.getPrefix());
    assertEquals(bucketResult.getContents(), bucketResult2.getContents());
  }

  @Test(expected = HttpResponseException.class)
  public void testListObjectsInBucket_noBucket() throws HttpResponseException {
    GoogleCloudStorage cloudStorage = createCloudStorageMock(
        null, HttpStatusCodes.STATUS_CODE_NOT_FOUND);
    cloudStorage.listObjectsInBucket("BAD-bucket", "test-prefix");
  }

  @Test
  public void testListAllMyBucketsResult_commonMethods() {
    DateTime now = new DateTime(clock.now());
    ListAllMyBucketsResult.Bucket bucket1 = new ListAllMyBucketsResult.Bucket();
    bucket1.setName("name");
    bucket1.setCreationDate(now);
    ListAllMyBucketsResult.Bucket bucket2 = new ListAllMyBucketsResult.Bucket();
    bucket2.setName("name");
    bucket2.setCreationDate(now);
    ListAllMyBucketsResult.Bucket bucket3 = new ListAllMyBucketsResult.Bucket();
    bucket3.setName("name");
    bucket3.setCreationDate(new DateTime(clock.now().getMillis() + 1));

    TestUtil.testCommonMethods(bucket1, bucket2, bucket3);

    ListAllMyBucketsResult.Buckets buckets1 = new ListAllMyBucketsResult.Buckets();
    buckets1.setBuckets(ImmutableList.of(bucket1));
    ListAllMyBucketsResult.Buckets buckets2 = new ListAllMyBucketsResult.Buckets();
    buckets2.setBuckets(ImmutableList.of(bucket2));
    ListAllMyBucketsResult.Buckets buckets3 = new ListAllMyBucketsResult.Buckets();
    buckets3.setBuckets(ImmutableList.of(bucket3));

    TestUtil.testCommonMethods(buckets1, buckets2, buckets3);

    ListAllMyBucketsResult list1 = new ListAllMyBucketsResult();
    list1.setBuckets(buckets1);
    ListAllMyBucketsResult list2 = new ListAllMyBucketsResult();
    list2.setBuckets(buckets2);
    ListAllMyBucketsResult list3 = new ListAllMyBucketsResult();
    list3.setBuckets(buckets3);

    TestUtil.testCommonMethods(list1, list2, list3);
  }

  @Test
  public void testListBuckets() throws HttpResponseException {
    GoogleCloudStorage cloudStorage = createCloudStorageMock(
        "application/xml", "/google-cloud-storage/get-buckets.xml");
    ListAllMyBucketsResult bucketResult = cloudStorage.listBuckets();
    Buckets buckets = bucketResult.getBuckets();
    Bucket bucket = buckets.getBuckets().get(0);
    assertEquals(1, buckets.getBuckets().size());
    assertEquals("test-bucket", bucket.getName());
    assertNotNull(bucket.getCreationDate());
    bucketResult.setBuckets(buckets);
    buckets.setBuckets(buckets.getBuckets());
    bucket.setName(bucket.getName());
    bucket.setCreationDate(bucket.getCreationDate());
  }

  @Test(expected = HttpResponseException.class)
  public void testListBuckets_bad() throws HttpResponseException {
    GoogleCloudStorage cloudStorage = createCloudStorageMock(
        null, HttpStatusCodes.STATUS_CODE_NOT_FOUND);
    cloudStorage.listBuckets();
  }

  @Test
  public void testBucketExists() throws HttpResponseException {
    GoogleCloudStorage cloudStorage = createCloudStorageMock(null, HttpStatusCodes.STATUS_CODE_OK);
    assertTrue(cloudStorage.bucketExists("test-bucket"));
  }

  @Test
  public void testPutBucket() throws HttpResponseException {
    GoogleCloudStorage cloudStorage = createCloudStorageMock(null, HttpStatusCodes.STATUS_CODE_OK);
    cloudStorage.putBucket("test-bucket-2");
  }

  @Test(expected = HttpResponseException.class)
  public void testPutBucket_dupe() throws HttpResponseException {
    GoogleCloudStorage cloudStorage = createCloudStorageMock(
        null, 409); // CONFLICT
    cloudStorage.putBucket("test-bucket");
  }

  @Test
  public void testGetObject() throws IOException {
    GoogleCloudStorage cloudStorage = createCloudStorageMock(
        "text/plain", "/google-cloud-storage/get-object.txt");
    StorageObject object = cloudStorage.getObject("test-bucket", "test-object", null);
    assertEquals(StorageObject.Status.OK, object.getStatus());
    assertNotNull(object.getContentLength());
    assertTrue(object.getContentLength() > 0);
    assertEquals(clock.now().toDateTime().withMillisOfSecond(0).toInstant(),
        object.getLastModified());
    assertEquals("text/plain", object.getContentType());
    assertEquals(12, object.getInputStream().available());
    assertNotNull(object.toString());
    assertTrue(object.getCustomMetadata().isEmpty());
  }

  @Test(expected = HttpResponseException.class)
  public void testGetObject_notFound() throws HttpResponseException {
    GoogleCloudStorage cloudStorage = createCloudStorageMock(
        null, HttpStatusCodes.STATUS_CODE_NOT_FOUND);
    cloudStorage.getObject("BAD-bucket", "BAD-object", null);
  }

  @Test
  public void testGetObjectIfModified() throws HttpResponseException {
    GoogleCloudStorage cloudStorage = createCloudStorageMock(
        DateTime.parse("2012-01-05").toInstant(), HttpStatusCodes.STATUS_CODE_OK);
    assertEquals(StorageObject.Status.OK, cloudStorage.getObject(
        "test-bucket", "test-object", DateTime.parse("2012-01-03").toInstant()).getStatus());
    assertEquals(StorageObject.Status.NOT_MODIFIED, cloudStorage.getObject(
        "test-bucket", "test-object", DateTime.parse("2012-01-05").toInstant()).getStatus());
  }

  @Test
  public void testPutObject() throws HttpResponseException {
    GoogleCloudStorage cloudStorage = createCloudStorageMock(null, HttpStatusCodes.STATUS_CODE_OK);
    cloudStorage.putObject("test-bucket", "test-object",
        new ByteArrayContent("binary/octet-stream", "SOME STUFF".getBytes(Charsets.UTF_8)), null);
  }

  @Test
  public void testPutObject_customMetadata() throws HttpResponseException {
    GoogleCloudStorage cloudStorage = createCloudStorageMock(null, HttpStatusCodes.STATUS_CODE_OK);
    cloudStorage.putObject("test-bucket", "test-object",
        new ByteArrayContent("binary/octet-stream", "SOME STUFF".getBytes(Charsets.UTF_8)),
        ImmutableMap.<String, Object>of());
    cloudStorage.putObject("test-bucket", "test-object",
        new ByteArrayContent("binary/octet-stream", "SOME STUFF".getBytes(Charsets.UTF_8)),
        ImmutableMap.<String, Object>of("aaa", 10));
  }

  @Test(expected = HttpResponseException.class)
  public void testPutObject_noBucket() throws HttpResponseException {
    GoogleCloudStorage cloudStorage = createCloudStorageMock(
        null, HttpStatusCodes.STATUS_CODE_NOT_FOUND);
    cloudStorage.putObject("BAD-bucket", "test-object",
        new ByteArrayContent("binary/octet-stream", "SOME STUFF".getBytes(Charsets.UTF_8)), null);
  }

  @Test
  public void testRemoveObject() throws HttpResponseException {
    GoogleCloudStorage cloudStorage = createCloudStorageMock(null, HttpStatusCodes.STATUS_CODE_OK);
    assertTrue(cloudStorage.removeObject("BAD-bucket", "BAD-object"));
    assertTrue(cloudStorage.removeObject("test-bucket", "BAD-object"));
    assertTrue(cloudStorage.removeObject("test-bucket", "test-object"));
    assertTrue(cloudStorage.removeObject("test-bucket", "test-object"));
  }

  private static GoogleCloudStorage createCloudStorageMock(Instant lastModified, int statusCode) {
    return GoogleCloudStorageFactory.newFactory()
        .setHttpTransport(createStorageTransport(null, null, lastModified, statusCode))
        .setApiProjectNumber(123L)
        .setCredential(new Credential(BearerToken.authorizationHeaderAccessMethod())).build();
  }

  private static GoogleCloudStorage createCloudStorageMock(
      String contentType, String contentStream) {
    return GoogleCloudStorageFactory.newFactory()
        .setHttpTransport(createStorageTransport(contentType,
            GoogleCloudStorageTest.class.getResourceAsStream(contentStream),
            null, HttpStatusCodes.STATUS_CODE_OK))
        .setApiProjectNumber(123L)
        .setCredential(new Credential(BearerToken.authorizationHeaderAccessMethod()))
        .setRequestInitializers(ImmutableSet.<HttpRequestInitializer>of(
            new HttpRequestInitializer() {
          @Override public void initialize(HttpRequest request) throws IOException {
        }}))
        .build();
  }

  private static HttpTransport createStorageTransport(final String contentType,
      final InputStream contentStream, final Instant lastModified, final int statusCode) {
    return new MockHttpTransport() {
      @Override public LowLevelHttpRequest buildRequest(final String method, final String url) {
        return new MockLowLevelHttpRequest() {
          @Override public LowLevelHttpResponse execute() throws IOException {
            MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
            Instant effectiveLastModified = lastModified == null ? clock.now() : lastModified;
            List<String> headerNames = Lists.newArrayList("Content-Type", "Last-Modified");
            List<String> headerValues = Lists.newArrayList(contentType,
                GoogleCloudStorageUtil.instantToLastModifiedString(effectiveLastModified));

            List<String> ifModifiedSinceHeader = getHeaderValues(HttpHeaders.IF_MODIFIED_SINCE);
            Instant ifModifiedSince =
                ifModifiedSinceHeader == null || ifModifiedSinceHeader.isEmpty()
                    ? null
                    : GoogleCloudStorageUtil.parseLastModified(ifModifiedSinceHeader.get(0));
            if (ifModifiedSince == null || effectiveLastModified.isAfter(ifModifiedSince)) {
              response.setStatusCode(statusCode);
              response.setContentType(contentType);
              response.setContent(contentStream);
              response.setContentLength(contentStream == null ? 0 : contentStream.available());
              headerNames.add("Content-Length");
              headerValues.add(String.valueOf(response.getContentLength()));
            } else {
              response.setStatusCode(HttpStatusCodes.STATUS_CODE_NOT_MODIFIED);
            }

            response.setHeaderNames(headerNames);
            response.setHeaderValues(headerValues);
            return response;
          }};
      }};
  }
}
