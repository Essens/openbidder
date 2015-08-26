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

package com.google.openbidder.ui.cloudstorage;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;

import org.joda.time.Instant;

import javax.annotation.Nullable;

/**
 * Represents one object inside a {@link com.google.openbidder.ui.cloudstorage.Bucket}.
 */
public class BucketObject {

  private final String name;
  private final long size;
  private final Instant lastModified;
  private final @Nullable String uploadedBy;

  public BucketObject(
      String name,
      long size,
      Instant lastModified,
      @Nullable String uploadedBy) {

    this.name = checkNotNull(name);
    this.size = size;
    this.lastModified = checkNotNull(lastModified);
    this.uploadedBy = uploadedBy;
  }

  public BucketObject(String name, long size, Instant lastModified) {
    this(name,
        size,
        lastModified,
        /* uploaded by */ null);
  }

  public String getName() {
    return name;
  }

  public long getSize() {
    return size;
  }

  public Instant getLastModified() {
    return lastModified;
  }

  public @Nullable String getUploadedBy() {
    return uploadedBy;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("name", name)
        .add("size", size)
        .add("lastModified", lastModified)
        .add("uploadedBy", uploadedBy)
        .toString();
  }
}
