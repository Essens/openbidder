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

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Error accessing Cloud Storage
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class BucketForbiddenException extends CloudStorageException {

  private static final String MESSAGE = "Access to bucket '%s' forbidden";

  public BucketForbiddenException(String bucketName) {
    super(String.format(MESSAGE, bucketName), bucketName, /* object name */ null);
  }
}
