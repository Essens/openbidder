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

package com.google.openbidder.googlecompute;

/**
 * Thrown when an OAuth2 scope was not found in the meta-data server
 */
public class MetadataOAuth2ScopeNotFoundException extends RuntimeException {
  private static final String ERROR_TEMPLATE =
      "OAuth2 scope '%s' was not found in the instance meta-data";

  public MetadataOAuth2ScopeNotFoundException(String oauth2Scope) {
    super(String.format(ERROR_TEMPLATE, oauth2Scope));
  }
}
