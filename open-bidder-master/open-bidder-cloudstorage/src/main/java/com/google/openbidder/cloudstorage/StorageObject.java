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

package com.google.openbidder.cloudstorage;

import com.google.common.base.MoreObjects;

import org.joda.time.Instant;

import java.io.InputStream;
import java.util.Map;

/**
 * Google Cloud Storage object which includes the input stream content of the
 * HTTP response and response headers.
 */
public class StorageObject {

  /**
   * Status of the retrieve operation.
   */
  public static enum Status {
    OK,
    NOT_MODIFIED
  }

  private InputStream inputStream;
  private Long contentLength;
  private String contentType;
  private Instant lastModified;
  private Status status;
  private Map<String, Object> customMetadata;

  /**
   * @return Input stream content of the HTTP response
   */
  public final InputStream getInputStream() {
    return inputStream;
  }

  public final void setInputStream(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  /**
   * @return Content length of the HTTP response
   */
  public final Long getContentLength() {
    return contentLength;
  }

  public final void setContentLength(Long contentLength) {
    this.contentLength = contentLength;
  }

  /**
   * @return Content type of the HTTP response
   */
  public final String getContentType() {
    return contentType;
  }

  public final void setContentType(String contentType) {
    this.contentType = contentType;
  }

  /**
   * @return Last modified time and date
   */
  public final Instant getLastModified() {
    return lastModified;
  }

  public final void setLastModified(Instant lastModified) {
    this.lastModified = lastModified;
  }

  /**
   * @return Status of the retrieve operation
   */
  public final Status getStatus() {
    return status;
  }

  public final void setStatus(Status status) {
    this.status = status;
  }

  /**
   * @return Custom metadata of the HTTP response
   */
  public final Map<String, Object> getCustomMetadata() {
    return customMetadata;
  }

  public final void setCustomMetadata(Map<String, Object> customMetadata) {
    this.customMetadata = customMetadata;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("contentLength", contentLength)
        .add("contentType", contentType)
        .add("lastModified", lastModified)
        .add("status", status)
        .add("customMetadata", customMetadata)
        .toString();
  }
}
