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

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.openbidder.ui.project.ProjectUser;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Fake implementation of {@link StorageService}.
 */
public class FakeStorageService implements StorageService {

  private final Table<String, String, BucketObject> objects = HashBasedTable.create();

  @Override
  public List<Bucket> listAllBuckets(ProjectUser projectUser) {
    throw new UnsupportedOperationException("listAllBuckets method unsupported");
  }

  @Override
  public BucketContents listAllObjectsInBucket(ProjectUser projectUser, String bucketName) {
    return listAllObjectsInBucket(projectUser, bucketName, null);
  }

  @Override
  public BucketContents listAllObjectsInBucket(
      ProjectUser projectUser,
      String bucketName,
      @Nullable final String objectPrefix) {

    Collection<BucketObject> objectCollection = objects.row(bucketName).values();
    if (!Strings.isNullOrEmpty(objectPrefix) && !objectCollection.isEmpty()) {
      objectCollection = Collections2.filter(objectCollection, new Predicate<BucketObject>() {
        @Override public boolean apply(BucketObject bucket) {
          return bucket.getName().startsWith(objectPrefix);
        }});
    }
    return new BucketContents(bucketName, objectCollection);
  }

  @Override
  public void deleteObject(ProjectUser projectUser, String bucketName, final String objectName) {
    objects.remove(bucketName, objectName);
  }

  public void putBucketObject(String bucketName, BucketObject object) {
    objects.put(bucketName, object.getName(), object);
  }

  public void clear() {
    objects.clear();
  }
}
