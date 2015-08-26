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

package com.google.openbidder.ui.resource.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.openbidder.ui.util.validation.Base64Encoded;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * DoubleClick-specific project resource.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DoubleClickProjectResource {

  @Base64Encoded(bytes = 32, message = "Must be base64 encoded string for 32 bytes")
  private String encryptionKey;

  @Base64Encoded(bytes = 32, message = "Must be base64 encoded string for 32 bytes")
  private String integrityKey;

  private String doubleClickReportingBucket;

  private boolean hasEncryptionKey;
  private boolean hasIntegrityKey;
  private boolean hasDoubleClickReportingBucket;

  public String getEncryptionKey() {
    return encryptionKey;
  }

  public void setEncryptionKey(String encryptionKey) {
    this.encryptionKey = encryptionKey;
    hasEncryptionKey = true;
  }

  public void clearEncryptionKey() {
    encryptionKey = null;
    hasEncryptionKey = false;
  }

  public boolean hasEncryptionKey() {
    return hasEncryptionKey;
  }

  public String getIntegrityKey() {
    return integrityKey;
  }

  public void setIntegrityKey(String integrityKey) {
    this.integrityKey = integrityKey;
    hasIntegrityKey = true;
  }

  public void clearIntegrityKey() {
    integrityKey = null;
    hasIntegrityKey = false;
  }

  public boolean hasIntegrityKey() {
    return hasIntegrityKey;
  }

  public String getDoubleClickReportingBucket() {
    return doubleClickReportingBucket;
  }

  public void setDoubleClickReportingBucket(String doubleClickReportingBucket) {
    this.doubleClickReportingBucket = doubleClickReportingBucket;
    hasDoubleClickReportingBucket = true;
  }

  public void clearDoubleClickReportingBucket() {
    doubleClickReportingBucket = null;
    hasDoubleClickReportingBucket = false;
  }

  public boolean hasDoubleClickReportingBucket() {
    return hasDoubleClickReportingBucket;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        encryptionKey,
        integrityKey,
        doubleClickReportingBucket
    );
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DoubleClickProjectResource other = (DoubleClickProjectResource) o;
    return Objects.equal(encryptionKey, other.encryptionKey)
        && Objects.equal(integrityKey, other.integrityKey)
        && Objects.equal(doubleClickReportingBucket, other.doubleClickReportingBucket)
        && Objects.equal(hasEncryptionKey, other.hasEncryptionKey)
        && Objects.equal(hasIntegrityKey, other.hasIntegrityKey)
        && Objects.equal(hasDoubleClickReportingBucket, other.hasDoubleClickReportingBucket);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("encryptionKey", encryptionKey)
        .add("integrityKey", integrityKey)
        .add("doubleClickReportingBucket", doubleClickReportingBucket)
        .toString();
  }
}
