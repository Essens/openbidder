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

package com.google.openbidder.ui.cloudstorage;

import com.google.openbidder.ui.project.ProjectUser;

import java.util.List;

/**
 * UI service for interacting with Google Cloud Storage.
 */
public interface StorageService {

  /**
   * List all the {@link Bucket}s in a given {@link com.google.openbidder.ui.entity.Project}.
   */
  List<Bucket> listAllBuckets(ProjectUser projectUser);

  /**
   * List all the objects in a given {@link Bucket}.
   */
  BucketContents listAllObjectsInBucket(ProjectUser projectUser, String bucketName);

  /**
   * List all objects in a given {@link Bucket} with a prefix.
   */
  BucketContents listAllObjectsInBucket(
      ProjectUser projectUser,
      String bucketName,
      String objectPrefix);

  /**
   * Remove the specified object.
   */
  void deleteObject(ProjectUser projectUser, String bucketName, String objectName);
}
