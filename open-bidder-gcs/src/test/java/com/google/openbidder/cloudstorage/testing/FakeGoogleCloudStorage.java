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

package com.google.openbidder.cloudstorage.testing;

import static org.mockito.Mockito.mock;

import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpStatusCodes;
import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.openbidder.cloudstorage.GoogleCloudStorage;
import com.google.openbidder.cloudstorage.GoogleCloudStorageException;
import com.google.openbidder.cloudstorage.StorageObject;
import com.google.openbidder.cloudstorage.model.ListAllMyBucketsResult;
import com.google.openbidder.cloudstorage.model.ListAllMyBucketsResult.Bucket;
import com.google.openbidder.cloudstorage.model.ListAllMyBucketsResult.Buckets;
import com.google.openbidder.cloudstorage.model.ListBucketResult;
import com.google.openbidder.cloudstorage.model.ListBucketResult.Content;
import com.google.openbidder.util.Clock;
import com.google.openbidder.util.ReflectionUtils;

import org.joda.time.DateTime;
import org.joda.time.Instant;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A fully-functional mock for {@link GoogleCloudStorage}; keeps all data in memory.
 */
@javax.annotation.ParametersAreNonnullByDefault
public class FakeGoogleCloudStorage implements GoogleCloudStorage {

  public final Clock clock;
  private final SortedMap<String, MockBucket> buckets = new TreeMap<>();
  private int forceHttpErrorCount;
  private int forceHttpErrorStatus;

  public FakeGoogleCloudStorage(Clock clock) {
    this.clock = clock;
  }

  public void forceHttpError(int status, int countdownOps) {
    this.forceHttpErrorStatus = status;
    this.forceHttpErrorCount = countdownOps;
  }

  private void failHttp() throws HttpResponseException {
    if (forceHttpErrorCount > 0 && --forceHttpErrorCount == 0) {
      throw newHttpResponseException(forceHttpErrorStatus);
    }
  }

  @Override
  public ListAllMyBucketsResult listBuckets() throws HttpResponseException {
    failHttp();
    return toListAllMyBucketsResult(toBuckets(Lists.newArrayList(
        Iterables.transform(buckets.values(), new Function<MockBucket, Bucket>() {
          @Override public Bucket apply(MockBucket bucket) {
            assert bucket != null;
            return bucket.toBucket();
          }
        })
    )));
  }

  @Override
  public ListBucketResult listObjectsInBucket(String bucketName, String objectNamePrefix)
      throws HttpResponseException {
    failHttp();
    MockBucket bucket = requireBucket(bucketName);

    return toListBucketResult(bucketName, objectNamePrefix, Lists.newArrayList(
        Iterables.transform(bucket.objects(objectNamePrefix), new Function<MockObject, Content> () {
          @Override public Content apply(MockObject object) {
            assert object != null;
            return object.toContent();
          }
        })
    ));
  }

  @Override
  public boolean bucketExists(String bucketName) throws HttpResponseException {
    failHttp();
    return buckets.containsKey(bucketName);
  }

  @Override
  public void putBucket(String bucketName) throws HttpResponseException {
    if (bucketExists(bucketName)) {
      throw newHttpResponseException(409); // CONFLICT
    }

    buckets.put(bucketName, new MockBucket(bucketName, clock.now()));
  }

  @Override
  public StorageObject getObject(String bucketName,
      String objectName, final Instant ifModifiedSince) throws HttpResponseException {
    failHttp();
    MockObject object = requireBucket(bucketName).objects.get(objectName);

    if (object == null) {
      throw newHttpResponseException(HttpStatusCodes.STATUS_CODE_NOT_FOUND);
    }

    StorageObject storageObject = new StorageObject();
    // Last modified on objects are truncated to the nearest second.
    storageObject.setLastModified(new Instant((object.timestamp.getMillis() / 1000) * 1000));
    if (ifModifiedSince != null && !ifModifiedSince.isBefore(storageObject.getLastModified())) {
      storageObject.setStatus(StorageObject.Status.NOT_MODIFIED);
    } else {
      storageObject.setInputStream(new ByteArrayInputStream(object.data));
      storageObject.setContentLength((long) object.data.length);
      storageObject.setContentType("binary/octet-stream");
      storageObject.setStatus(StorageObject.Status.OK);
    }
    return storageObject;
  }

  @Override
  public StorageObject describeObject(
      String bucketName,
      String objectName) throws HttpResponseException {
    failHttp();

    MockObject object = requireBucket(bucketName).objects.get(objectName);
    if (object == null) {
      throw newHttpResponseException(HttpStatusCodes.STATUS_CODE_NOT_FOUND);
    }

    StorageObject storageObject = new StorageObject();
    storageObject.setLastModified(object.timestamp);
    storageObject.setContentLength((long) object.data.length);
    storageObject.setContentType("binary/octet-stream");
    storageObject.setStatus(StorageObject.Status.OK);
    return storageObject;
  }

  @Override
  public StorageObject putObject(
      String bucketName,
      String objectName,
      HttpContent httpContent,
      Map<String, Object> customMetadata) throws HttpResponseException {
    failHttp();

    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    try {
      httpContent.writeTo(baos);
    } catch (IOException e) {
      throw new GoogleCloudStorageException(e); // Never happens
    }

    MockObject object = new MockObject(objectName, baos.toByteArray(), clock.now(), customMetadata);
    requireBucket(bucketName).objects.put(objectName, object);
    StorageObject storageObject = new StorageObject();
    storageObject.setLastModified(object.timestamp);
    storageObject.setContentLength((long) object.data.length);
    storageObject.setContentType("binary/octet-stream");
    storageObject.setStatus(StorageObject.Status.OK);
    return storageObject;
  }

  @Override
  public boolean removeObject(String bucketName, String objectName) throws HttpResponseException {
    failHttp();
    return requireBucket(bucketName).objects.remove(objectName) != null;
  }

  private MockBucket requireBucket(String bucketName) throws HttpResponseException {
    MockBucket bucket = buckets.get(bucketName);

    if (bucket == null) {
      throw newHttpResponseException(HttpStatusCodes.STATUS_CODE_NOT_FOUND);
    }

    return bucket;
  }

  private static HttpResponseException newHttpResponseException(int status) {
    try {
      // Horrible reflection & security violation necessary, because the HTTP request,
      // response and exception classes are full of 'final's so Mockito cannot mock them.
      // We still use Mockito as a workaround for public constructors that would fail.
      HttpResponseException e = mock(HttpResponseException.class);
      Field field = HttpResponseException.class.getDeclaredField("statusCode");
      ReflectionUtils.forceAccessible(field).setInt(e, status);
      return e;
    } catch (ReflectiveOperationException ee) {
      throw new IllegalStateException(ee);
    }
  }

  private ListAllMyBucketsResult toListAllMyBucketsResult(Buckets buckets) {
    ListAllMyBucketsResult ret = new ListAllMyBucketsResult();
    ret.setBuckets(buckets);
    return ret;
  }

  private static Buckets toBuckets(List<Bucket> buckets) {
    Buckets ret = new Buckets();
    ret.setBuckets(buckets);
    return ret;
  }

  private ListBucketResult toListBucketResult(
      String bucketName, String prefix, List<Content> contents) {
    ListBucketResult ret = new ListBucketResult();
    ret.setName(bucketName);
    ret.setPrefix(prefix);
    // In real response, no contents result in the default for ListBucketResult
    if (!contents.isEmpty()) {
      ret.setContents(contents);
    }
    return ret;
  }

  public static class MockBucket {
    public final String name;
    public final Instant timestamp;
    public final SortedMap<String, MockObject> objects = new TreeMap<>();

    public MockBucket(String name, Instant timestamp) {
      this.name = name;
      this.timestamp = timestamp;
    }

    public Bucket toBucket() {
      Bucket ret = new Bucket();
      ret.setName(name);
      ret.setCreationDate(new DateTime(timestamp.getMillis()));
      return ret;
    }

    public Iterable<MockObject> objects(String prefix) {
      return Strings.isNullOrEmpty(prefix)
          ? objects.values()
          : objects.subMap(prefix,
              prefix.substring(0, prefix.length() - 1) + (prefix.charAt(prefix.length() - 1) + 1))
              .values();
    }

    @Override public String toString() {
      return MoreObjects.toStringHelper(this).omitNullValues()
          .add("name", name)
          .add("timestamp", timestamp)
          .add("objects", objects)
          .toString();
    }
  }

  @Override public String toString() {
    return buckets.toString();
  }

  public static class MockObject {
    public final String name;
    public final Instant timestamp;
    public final byte[] data;
    public final Map<String, Object> metadata;

    public MockObject(String name, byte[] data, Instant timestamp, Map<String, Object> metadata) {
      this.name = name;
      this.data = data;
      this.timestamp = timestamp;
      this.metadata = metadata == null
          ? ImmutableMap.<String, Object>of()
          : ImmutableMap.copyOf(metadata);
    }

    public Content toContent() {
      Content ret = new Content();
      ret.setKey(name);
      ret.setLastModified(new DateTime(timestamp.getMillis()));
      ret.setSize((long) data.length);
      return ret;
    }

    @Override public String toString() {
      return MoreObjects.toStringHelper(this).omitNullValues()
          .add("name", name)
          .add("timestamp", timestamp)
          .add("data#", data.length)
          .add("metadata", metadata)
          .toString();
    }
  }
}
