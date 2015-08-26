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

package com.google.openbidder.ui.cloudstorage.exception;

import javax.annotation.Nullable;

/**
 * Parent exception for Cloud Storage errors.
 */
public class CloudStorageException extends RuntimeException {
  private final String bucketName;
  private final @Nullable String objectName;

  public CloudStorageException(String bucketName, @Nullable String objectName) {
    this(/* message */ null, bucketName, objectName, /* throwable */ null);
  }

  public CloudStorageException(String message, String bucketName, @Nullable String objectName) {
    this(message, bucketName, objectName, /* object name */ null);
  }

  public CloudStorageException(
      String message,
      String bucketName,
      @Nullable String objectName,
      Throwable throwable) {

    super(message, throwable);
    this.bucketName = bucketName;
    this.objectName = objectName;
  }

  public String getBucketName() {
    return bucketName;
  }

  public @Nullable String getObjectName() {
    return objectName;
  }
}
