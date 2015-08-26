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

package com.google.openbidder.exchange.doubleclick.match;

/**
 * DoubleClick pixel match parameters.
 */
public final class DoubleClickMatchTag {
  /**
   * Reserved prefix for tags.
   */
  public static final String GOOGLE_RESERVED_TAG = "google_";

  // known parameters
  public static final String GOOGLE_COOKIE_MATCH = GOOGLE_RESERVED_TAG + "cm";
  public static final String GOOGLE_COOKIE_VERSION = GOOGLE_RESERVED_TAG + "cver";
  public static final String GOOGLE_HOSTED_MATCH = GOOGLE_RESERVED_TAG + "hm";
  public static final String GOOGLE_NID = GOOGLE_RESERVED_TAG + "nid";
  public static final String GOOGLE_GID = GOOGLE_RESERVED_TAG + "gid";
  public static final String GOOGLE_PUSH = GOOGLE_RESERVED_TAG + "push";
  public static final String GOOGLE_SET_COOKIE = GOOGLE_RESERVED_TAG + "sc";
  public static final String GOOGLE_USER_LIST = GOOGLE_RESERVED_TAG + "ula";

  private DoubleClickMatchTag() {
  }
}
