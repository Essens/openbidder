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

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.xml.XmlNamespaceDictionary;
import com.google.api.client.xml.XmlObjectParser;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.openbidder.cloudstorage.GoogleCloudStorage;
import com.google.openbidder.cloudstorage.GoogleCloudStorageConstants;
import com.google.openbidder.cloudstorage.GoogleCloudStorageException;
import com.google.openbidder.cloudstorage.StorageObject;
import com.google.openbidder.cloudstorage.model.ListAllMyBucketsResult;
import com.google.openbidder.cloudstorage.model.ListBucketResult;

import org.joda.time.Duration;
import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Implements {@link GoogleCloudStorage}.
 */
public class GoogleCloudStorageImpl implements GoogleCloudStorage {
  private static final Logger logger = LoggerFactory.getLogger(GoogleCloudStorageImpl.class);

  private final HttpRequestFactory requestFactory;
  private final XmlObjectParser objectParser = new XmlObjectParser(new XmlNamespaceDictionary());

  /**
   * A HTTP request function
   *
   * @param <T> return type of the function
   */
  private abstract class HttpRequestFunction<T> {
    /**
     * @param url Cloud storage url
     * @return result of the function
     * @throws IOException and also the child class {@link HttpResponseException}.
     */
    public abstract T apply(GenericUrl url) throws IOException;
  }

  public GoogleCloudStorageImpl(
      HttpTransport httpTransport,
      HttpRequestInitializer httpRequestInitializer) {

    requestFactory = httpTransport.createRequestFactory(httpRequestInitializer);
  }

  @Override
  public ListAllMyBucketsResult listBuckets() throws HttpResponseException {
    return executeRequest(buildBucketListUrl(), new HttpRequestFunction<ListAllMyBucketsResult>() {
      @Override public ListAllMyBucketsResult apply(GenericUrl url) throws IOException {
        HttpRequest request = requestFactory.buildGetRequest(url);
        request.setParser(objectParser);
        HttpResponse response = execute(request);
        return response.parseAs(ListAllMyBucketsResult.class);
      }
    });
  }

  @Override
  public ListBucketResult listObjectsInBucket(
      String bucketName,
      @Nullable String objectNamePrefix) throws HttpResponseException {

    GenericUrl bucketUrl = buildObjectListUrl(bucketName);
    if (!Strings.isNullOrEmpty(objectNamePrefix)) {
      bucketUrl.put(GoogleCloudStorageConstants.PREFIX_PARAMETER, objectNamePrefix);
    }

    return executeRequest(bucketUrl, new HttpRequestFunction<ListBucketResult>() {
      @Override public ListBucketResult apply(GenericUrl url) throws IOException {
        HttpRequest request = requestFactory.buildGetRequest(url);
        request.setParser(objectParser);
        HttpResponse response = execute(request);
        return response.parseAs(ListBucketResult.class);
      }
    });
  }

  @Override
  public boolean bucketExists(String bucketName) throws HttpResponseException {
    GenericUrl bucketUrl = buildBucketUrl(bucketName);
    return executeRequest(bucketUrl, new HttpRequestFunction<Boolean>() {
      @Override public Boolean apply(GenericUrl url) throws IOException {
        HttpRequest request = requestFactory.buildHeadRequest(url);
        execute(request);
        return true;
      }
    });
  }

  @Override
  public void putBucket(String bucketName) throws HttpResponseException {
    GenericUrl bucketUrl = buildBucketUrl(bucketName);
    final HttpContent content = new UrlEncodedContent("");

    executeRequest(bucketUrl, new HttpRequestFunction<Void>() {
      @Override public @Nullable Void apply(GenericUrl url) throws IOException {
        HttpRequest request = requestFactory.buildPutRequest(url, content);
        request.setParser(objectParser);
        execute(request);
        return null;
      }
    });
  }

  @Override
  public StorageObject getObject(
      String bucketName,
      String objectName,
      final @Nullable Instant ifModifiedSince) throws HttpResponseException {

    GenericUrl objectUrl = buildObjectUrl(bucketName, objectName);

    return executeRequest(objectUrl, new HttpRequestFunction<StorageObject>() {
      @Override public StorageObject apply(GenericUrl url) throws IOException {
        HttpRequest request = requestFactory.buildGetRequest(url);

        if (ifModifiedSince != null) {
          // Only if the object has been modified later than the date specified, the object
          // is downloaded. If the object is not modified, a 304 Not Modified status code
          // will be returned.
          request.getHeaders().put(
              com.google.common.net.HttpHeaders.IF_MODIFIED_SINCE,
              asList(GoogleCloudStorageUtil.instantToLastModifiedString(ifModifiedSince)));
        }

        try {
          return buildStorageObject(execute(request));
        } catch (HttpResponseException e) {
          if (e.getStatusCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
            logger.debug("Object not modified: {}", url.build());
            return buildNotModified(e.getHeaders());
          } else {
            throw e;
          }
        }
      }
    });
  }

  @Override public StorageObject describeObject(
      String bucketName,
      String objectName) throws HttpResponseException {

    GenericUrl objectUrl = buildObjectUrl(bucketName, objectName);

    return executeRequest(objectUrl, new HttpRequestFunction<StorageObject>() {
      @Override public StorageObject apply(GenericUrl url) throws IOException {
        HttpRequest request = requestFactory.buildHeadRequest(url);
        return buildObjectDescription(execute(request));
      }
    });
  }

  @Override
  public StorageObject putObject(
      String bucketName,
      String objectName,
      final HttpContent httpContent,
      final @Nullable Map<String, Object> customMetadata) throws HttpResponseException {
    GenericUrl objectUrl = buildObjectUrl(bucketName, objectName);

    executeRequest(objectUrl, new HttpRequestFunction<Void>() {
      @Override public @Nullable Void apply(GenericUrl url) throws IOException {
        HttpRequest request = requestFactory.buildPutRequest(url, httpContent);
        if (customMetadata != null && !customMetadata.isEmpty()) {
          GoogleCloudStorageUtil.setCustomMetadata(request.getHeaders(), customMetadata);
        }
        execute(request);
        return null;
      }
    });

    // in the JSON API this can be done as a single round trip as the response includes the
    // last modified time.
    return describeObject(bucketName, objectName);
  }

  @Override
  public boolean removeObject(String bucketName, String objectName) throws HttpResponseException {
    GenericUrl objectUrl = buildObjectUrl(bucketName, objectName);

    return executeRequest(objectUrl, new HttpRequestFunction<Boolean>() {
      @Override public Boolean apply(GenericUrl url) throws IOException {
        HttpRequest request = requestFactory.buildDeleteRequest(url);
        HttpResponse response = execute(request);
        return response.isSuccessStatusCode();
      }
    });
  }

  private <T> T executeRequest(GenericUrl url, HttpRequestFunction<T> function)
      throws HttpResponseException {
    try {
      return function.apply(url);
    } catch (HttpResponseException e) {
      throw e;
    } catch (IOException e) {
      throw new GoogleCloudStorageException("Error executing Google Storage request: " + url, e);
    }
  }

  private HttpResponse execute(HttpRequest request) throws IOException {
    Instant start = Instant.now();
    HttpResponse response = request.execute();
    Duration delay = new Duration(start, Instant.now());
    if (logger.isDebugEnabled()) {
      logger.debug("Storage: {} {} - executed in {}ms, status={}",
          request.getRequestMethod(),
          request.getUrl(),
          delay.getMillis(),
          response.getStatusCode());
    }
    return response;
  }

  // TODO(wshields): convert the below to the JSON API when it goes out of experimental
  // Note: the below structure is to support the slightly different URL structure of the
  // JSON API for Google Cloud Storage.

  private static GenericUrl buildBucketListUrl() {
    return buildBaseUrl(buildBucketListPath());
  }

  private static GenericUrl buildBucketUrl(String bucketName) {
    return buildBaseUrl(buildBucketPath(bucketName));
  }

  private static GenericUrl buildObjectListUrl(String bucketName) {
    return buildBaseUrl(buildObjectListPath(bucketName));
  }

  private static GenericUrl buildObjectUrl(String bucketName, String objectName) {
    return buildBaseUrl(buildObjectPath(bucketName, objectName));
  }

  private static GenericUrl buildBaseUrl(List<String> pathParts) {
    GenericUrl url = new GenericUrl();
    url.setHost(GoogleCloudStorageConstants.API_HOST);
    url.setScheme(GoogleCloudStorageConstants.HTTP_SCHEME);
    url.setPathParts(pathParts);
    return url;
  }

  private static List<String> buildBasePath() {
    return Lists.newArrayList("");
  }

  private static List<String> buildBucketListPath() {
    return buildBasePath();
  }

  private static List<String> buildBucketPath(String bucketName) {
    checkNotNull(bucketName);
    List<String> pathPaths = buildBucketListPath();
    pathPaths.add(bucketName);
    return pathPaths;
  }

  private static List<String> buildObjectListPath(String bucketName) {
    return buildBucketPath(bucketName);
  }

  private static List<String> buildObjectPath(String bucketName, String objectName) {
    checkNotNull(bucketName);
    checkNotNull(objectName);
    List<String> pathPaths = buildObjectListPath(bucketName);
    pathPaths.add(objectName);
    return pathPaths;
  }

  private static StorageObject buildNotModified(HttpHeaders httpHeaders) {
    StorageObject storageObject = new StorageObject();
    storageObject.setStatus(StorageObject.Status.NOT_MODIFIED);
    storageObject.setLastModified(
        GoogleCloudStorageUtil.parseLastModified(httpHeaders.getLastModified()));
    return storageObject;
  }

  private static StorageObject buildObjectDescription(HttpResponse response) {
    return buildBaseStorageObject(response.getHeaders());
  }

  private static StorageObject buildStorageObject(HttpResponse response) throws IOException {
    StorageObject storageObject = buildBaseStorageObject(response.getHeaders());
    storageObject.setInputStream(response.getContent());
    return storageObject;
  }

  private static StorageObject buildBaseStorageObject(HttpHeaders httpHeaders) {
    StorageObject storageObject = new StorageObject();
    storageObject.setContentLength(httpHeaders.getContentLength());
    storageObject.setContentType(httpHeaders.getContentType());
    storageObject.setCustomMetadata(GoogleCloudStorageUtil.getCustomMetadata(httpHeaders));
    storageObject.setLastModified(
        GoogleCloudStorageUtil.parseLastModified(httpHeaders.getLastModified()));
    storageObject.setStatus(StorageObject.Status.OK);
    return storageObject;
  }
}
