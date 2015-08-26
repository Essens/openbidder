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
import com.google.common.collect.Lists;

import org.joda.time.DateTime;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Response to a GET bucket request.
 *
 * @see com.google.openbidder.cloudstorage.GoogleCloudStorage#listObjectsInBucket(String, String)
 * listObjectsInBucket(String, String)
 */
public class ListBucketResult {

  @Key("Contents")
  private List<Content> contents = Lists.<Content>newArrayList();

  @Key("IsTruncated")
  private boolean isTruncated;

  @Key("Name")
  private String name;

  @Key("Prefix")
  private String prefix;

  public final List<Content> getContents() {
    return contents;
  }

  public final void setContents(List<Content> contents) {
    this.contents = contents;
  }

  public final boolean isTruncated() {
    return isTruncated;
  }

  public final void setTruncated(boolean isTruncated) {
    this.isTruncated = isTruncated;
  }

  public final String getName() {
    return name;
  }

  public final void setName(String name) {
    this.name = name;
  }

  public final String getPrefix() {
    return prefix;
  }

  public final void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (obj == this) {
      return true;
    } else if (!(obj instanceof ListBucketResult)) {
      return false;
    }

    ListBucketResult other = (ListBucketResult) obj;
    return equal(name, other.name)
        && equal(prefix, other.prefix)
        && isTruncated == other.isTruncated
        && equal(contents, other.contents);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(name, prefix, isTruncated, contents);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("name", name)
        .add("prefix", prefix)
        .add("isTruncated", isTruncated)
        .add("contents", contents)
        .toString();
  }

  /**
   * Information about a listed object.
   */
  public static class Content {
    @Key("Key")
    private String key;

    @Key("LastModified")
    private com.google.api.client.util.DateTime lastModified;

    @Key("Size")
    private Long size;

    public final String getKey() {
      return key;
    }

    public final void setKey(String key) {
      this.key = key;
    }

    public final DateTime getLastModified() {
      return new DateTime(lastModified.getValue());
    }

    public final void setLastModified(DateTime lastModified) {
      this.lastModified = new com.google.api.client.util.DateTime(lastModified.getMillis());
    }

    public final Long getSize() {
      return size;
    }

    public final void setSize(Long size) {
      this.size = size;
    }

    @Override public boolean equals(@Nullable Object obj) {
      if (obj == this) {
        return true;
      } else if (!(obj instanceof Content)) {
        return false;
      }

      Content other = (Content) obj;
      return equal(key, other.key)
          && equal(lastModified, other.lastModified)
          && equal(size, other.size);
    }

    @Override public int hashCode() {
      return Objects.hashCode(key, lastModified, size);
    }

    @Override public String toString() {
      return MoreObjects.toStringHelper(this).omitNullValues()
          .add("key", key)
          .add("lastModified", lastModified)
          .add("size", size)
          .toString();
    }
  }
}
