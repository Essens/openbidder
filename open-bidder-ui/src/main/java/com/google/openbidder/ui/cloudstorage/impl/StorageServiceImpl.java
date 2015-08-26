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

package com.google.openbidder.ui.cloudstorage.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.openbidder.cloudstorage.GoogleCloudStorage;
import com.google.openbidder.cloudstorage.GoogleCloudStorageFactory;
import com.google.openbidder.cloudstorage.model.ListAllMyBucketsResult;
import com.google.openbidder.cloudstorage.model.ListBucketResult;
import com.google.openbidder.ui.cloudstorage.Bucket;
import com.google.openbidder.ui.cloudstorage.BucketContents;
import com.google.openbidder.ui.cloudstorage.BucketObject;
import com.google.openbidder.ui.cloudstorage.StorageService;
import com.google.openbidder.ui.cloudstorage.exception.BucketForbiddenException;
import com.google.openbidder.ui.cloudstorage.exception.BucketNotFoundException;
import com.google.openbidder.ui.cloudstorage.exception.ObjectNotFoundException;
import com.google.openbidder.ui.project.ProjectUser;
import com.google.openbidder.ui.user.AuthorizationService;

import org.joda.time.Instant;
import org.springframework.http.HttpStatus;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Default implementation of {@link StorageService}.
 */
public class StorageServiceImpl implements StorageService {

  private static final Function<ListAllMyBucketsResult.Bucket, Bucket> CONVERT_TO_BUCKET =
      new Function<ListAllMyBucketsResult.Bucket, Bucket>() {
        @Override public Bucket apply(ListAllMyBucketsResult.Bucket buckets) {
          return new Bucket(buckets.getName(), new Instant(buckets.getCreationDate()));
        }};

  private static final Function<ListBucketResult.Content, BucketObject> CONVERT_TO_OBJECT =
      new Function<ListBucketResult.Content, BucketObject>() {
        @Override public BucketObject apply(ListBucketResult.Content content) {
          return new BucketObject(
              content.getKey(),
              content.getSize(),
              new Instant(content.getLastModified()));
        }};

  private final AuthorizationService authorizationService;
  private final HttpTransport httpTransport;

  @Inject
  public StorageServiceImpl(
      AuthorizationService authorizationService,
      HttpTransport httpTransport) {

    this.authorizationService = checkNotNull(authorizationService);
    this.httpTransport = checkNotNull(httpTransport);
  }

  @Override
  public List<Bucket> listAllBuckets(ProjectUser projectUser) {
    GoogleCloudStorage googleCloudStorage = buildClient(projectUser);
    try {
      ListAllMyBucketsResult bucketsResult = googleCloudStorage.listBuckets();
      return Lists.transform(bucketsResult.getBuckets().getBuckets(), CONVERT_TO_BUCKET);
    } catch (HttpResponseException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public BucketContents listAllObjectsInBucket(ProjectUser projectUser, String bucketName) {
    return listAllObjectsInBucket(projectUser, bucketName, null);
  }

  @Override
  public BucketContents listAllObjectsInBucket(
      ProjectUser projectUser,
      String bucketName,
      @Nullable String objectPrefix) {

    GoogleCloudStorage googleCloudStorage = buildClient(projectUser);
    try {
      ListBucketResult listBucketResult =
          googleCloudStorage.listObjectsInBucket(bucketName, objectPrefix);
      if (listBucketResult.getContents() == null) {
        // Annoyingly, if there are no objects 'contents' is null not empty.
        return new BucketContents(listBucketResult.getName());
      } else {
        return new BucketContents(listBucketResult.getName(),
            Iterables.transform(listBucketResult.getContents(), CONVERT_TO_OBJECT));
      }
    } catch (HttpResponseException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND.value()) {
        throw new BucketNotFoundException(bucketName);
      } else if (e.getStatusCode() == HttpStatus.FORBIDDEN.value()) {
        throw new BucketForbiddenException(bucketName);
      }
      throw new RuntimeException(e);
    }
  }

  @Override
  public void deleteObject(ProjectUser projectUser, String bucketName, String objectName) {
    GoogleCloudStorage googleCloudStorage = buildClient(projectUser);
    try {
      googleCloudStorage.removeObject(bucketName, objectName);
    } catch (HttpResponseException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND.value()) {
        throw new ObjectNotFoundException(bucketName, objectName);
      }
      throw new RuntimeException(e);
    }
  }

  private GoogleCloudStorage buildClient(ProjectUser projectUser) {
    Credential credential = authorizationService.getCredentialsForProject(projectUser);
    return GoogleCloudStorageFactory.newFactory()
        .setHttpTransport(httpTransport)
        .setApiProjectNumber(projectUser.getProject().getApiProjectNumber())
        .setCredential(credential)
        .build();
  }
}
