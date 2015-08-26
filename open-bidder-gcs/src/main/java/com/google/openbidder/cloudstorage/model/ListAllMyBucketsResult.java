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

package com.google.openbidder.cloudstorage.model;

import static com.google.common.base.Objects.equal;

import com.google.api.client.util.Key;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Wrapper class for binding to a query to list all buckets in a project.
 *
 * @see com.google.openbidder.cloudstorage.GoogleCloudStorage#listBuckets()
 */
public class ListAllMyBucketsResult {

  @Key("Buckets")
  private Buckets buckets;

  public final Buckets getBuckets() {
    return buckets;
  }

  public final void setBuckets(Buckets buckets) {
    this.buckets = buckets;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (obj == this) {
      return true;
    } else if (!(obj instanceof ListAllMyBucketsResult)) {
      return false;
    }

    ListAllMyBucketsResult other = (ListAllMyBucketsResult) obj;
    return equal(buckets, other.buckets);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(buckets);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("buckets", buckets.buckets)
        .toString();
  }

  /**
   * The result's list of buckets.
   */
  public static class Buckets {
    @Key("Bucket")
    private List<Bucket> buckets = new ArrayList<>();

    public final List<Bucket> getBuckets() {
      return buckets;
    }

    public final void setBuckets(List<Bucket> buckets) {
      this.buckets = buckets;
    }

    @Override public boolean equals(@Nullable Object obj) {
      if (obj == this) {
        return true;
      } else if (!(obj instanceof Buckets)) {
        return false;
      }

      Buckets other = (Buckets) obj;
      return equal(buckets, other.buckets);
    }

    @Override public int hashCode() {
      return Objects.hashCode(buckets);
    }

    @Override public String toString() {
      return MoreObjects.toStringHelper(this).omitNullValues()
          .add("buckets", buckets)
          .toString();
    }
  }

  /**
   * A result bucket.
   */
  public static class Bucket {
    @Key("Name")
    private String name;

    @Key("CreationDate")
    private com.google.api.client.util.DateTime creationDate;

    public final String getName() {
      return name;
    }

    public final void setName(String name) {
      this.name = name;
    }

    public final DateTime getCreationDate() {
      return new DateTime(creationDate.getValue());
    }

    public final void setCreationDate(DateTime creationDate) {
      this.creationDate = new com.google.api.client.util.DateTime(creationDate.getMillis());
    }

    @Override public boolean equals(@Nullable Object obj) {
      if (obj == this) {
        return true;
      } else if (!(obj instanceof Bucket)) {
        return false;
      }

      Bucket other = (Bucket) obj;
      return equal(name, other.name)
          && equal(creationDate, other.creationDate);
    }

    @Override public int hashCode() {
      return Objects.hashCode(name, creationDate);
    }

    @Override public String toString() {
      return MoreObjects.toStringHelper(this).omitNullValues()
          .add("name", name)
          .add("creationDate", creationDate)
          .toString();
    }
  }
}
