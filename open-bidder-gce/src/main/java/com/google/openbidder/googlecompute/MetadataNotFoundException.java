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
 * Thrown when a meta-data key is not found on a Compute Engine instance.
 */
public class MetadataNotFoundException extends RuntimeException {
  private static final String ERROR_TEMPLATE = "Meta-data key '%s' was not found.";

  public MetadataNotFoundException(String keyName) {
    super(String.format(ERROR_TEMPLATE, keyName));
  }
}
