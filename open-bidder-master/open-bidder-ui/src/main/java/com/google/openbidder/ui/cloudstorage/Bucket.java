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

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import org.joda.time.Instant;

/**
 * A Google Cloud Storage bucket.
 */
public class Bucket {

  private final String name;
  private final Instant creationDate;

  public Bucket(String name, Instant creationDate) {
    this.name = Preconditions.checkNotNull(name);
    this.creationDate = Preconditions.checkNotNull(creationDate);
  }

  public String getName() {
    return name;
  }

  public Instant getCreationDate() {
    return creationDate;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("name", name)
        .add("creationDate", creationDate)
        .toString();
  }
}
