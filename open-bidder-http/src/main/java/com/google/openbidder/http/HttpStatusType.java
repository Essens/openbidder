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

package com.google.openbidder.http;

/**
 * HTTP status code categories.
 */
public enum HttpStatusType {
  INFO(100, 199),
  SUCCESS(200, 299),
  REDIRECT(300, 399),
  CLIENT_ERROR(400, 499),
  SERVER_ERROR(500, 599);

  private final int min;
  private final int max;

  private HttpStatusType(int min, int max) {
    this.min = min;
    this.max = max;
  }

  public final boolean contains(int statusCode) {
    return statusCode >= min && statusCode <= max;
  }

  public final int min() {
    return min;
  }

  public final int max() {
    return max;
  }
}
