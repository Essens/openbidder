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

package com.google.openbidder.ui.compute.exception;

import com.google.common.base.Preconditions;

import javax.annotation.Nullable;

/**
 * Superclass for Compute Engine exceptions.
 */
public class ComputeEngineException extends RuntimeException {

  private final String apiProjectId;

  public ComputeEngineException(String apiProjectId) {
    this(/* message */ null, apiProjectId, /* cause */ null);
  }

  public ComputeEngineException(String message, String apiProjectId) {
    this(message, apiProjectId, /* cause */ null);
  }

  public ComputeEngineException(String apiProjectId, Throwable cause) {
    this(/* message */ null, apiProjectId, cause);
  }

  public ComputeEngineException(
      @Nullable String message, String apiProjectId, @Nullable Throwable cause) {
    super(message, cause);
    this.apiProjectId = Preconditions.checkNotNull(apiProjectId);
  }

  public String getApiProjectId() {
    return apiProjectId;
  }
}
