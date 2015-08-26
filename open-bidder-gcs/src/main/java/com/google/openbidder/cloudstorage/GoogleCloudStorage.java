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

import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpResponseException;
import com.google.openbidder.cloudstorage.model.ListAllMyBucketsResult;
import com.google.openbidder.cloudstorage.model.ListBucketResult;

import org.joda.time.Instant;

import java.util.Map;

import javax.annotation.Nullable;

/**
 * A Google Cloud Storage client, which uses OAuth2 for authentication.
 *
 * <p>Implementations of this class are thread safe.
 *
 * <p>See http://code.google.com/apis/storage/ for more details about Cloud Storage.
 */
public interface GoogleCloudStorage {

  /**
   * Find all the buckets in the current project.
   *
   * @throws HttpResponseException If response is not HTTP 2xx
   */
  ListAllMyBucketsResult listBuckets() throws HttpResponseException;

  /**
   * Retrieves the list of objects in a bucket.
   *
   * @param objectNamePrefix Prefix string to filter for in the keys
   * @return List of objects in the bucket
   * @throws HttpResponseException If response is not HTTP 2xx
   */
  ListBucketResult listObjectsInBucket(String bucketName, @Nullable String objectNamePrefix)
      throws HttpResponseException;

  /**
   * Checks if a bucket exists.
   *
   * @param bucketName Name of the bucket
   * @return {@code true} if the bucket exists
   * @throws HttpResponseException If response is not HTTP 2xx
   */
  boolean bucketExists(String bucketName) throws HttpResponseException;

  /**
   * Creates a bucket.
   *
   * @param bucketName Name of the bucket to create
   * @throws HttpResponseException If response is not HTTP 2xx
   */
  void putBucket(String bucketName) throws HttpResponseException;

  /**
   * Retrieves an object from a cloud storage bucket.
   *
   * @param bucketName Name of the bucket
   * @param objectName Name of the object
   * @param ifModifiedSince (Optional) filters only objects that have been modified after this time
   * @return Contents of the object
   * @throws HttpResponseException If response is not HTTP 2xx
   */
  StorageObject getObject(String bucketName, String objectName, @Nullable Instant ifModifiedSince)
      throws HttpResponseException;

  /**
   * Describe an object without returning its contents.
   *
   * @param bucketName Name of the bucket
   * @param objectName Name of the object
   * @return Metadata description of the object
   * @throws HttpResponseException if HTTP response not 200
   */
  StorageObject describeObject(String bucketName, String objectName) throws HttpResponseException;

  /**
   * Stores an object in a cloud storage bucket. Operation fails if the bucket does not exist.
   *
   * @param bucketName Name of the bucket
   * @param objectName Name of the object
   * @param httpContent Contents to place in the object
   * @param customMetadata (Optional) Custom metadata to store against the object
   * @return The stored object
   * @throws HttpResponseException If response is not HTTP 2xx
   */
  StorageObject putObject(String bucketName, String objectName, HttpContent httpContent,
      @Nullable Map<String, Object> customMetadata) throws HttpResponseException;

  /**
   * Removes an object from a cloud storage bucket.
   *
   * @param bucketName Name of the bucket
   * @param objectName Name of the object
   * @return <code>true</code> if the object was removed successfully
   * @throws HttpResponseException If response is not HTTP 2xx
   */
  boolean removeObject(String bucketName, String objectName) throws HttpResponseException;
}
