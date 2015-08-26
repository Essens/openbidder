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
import com.google.openbidder.ui.compute.ComputeResourceType;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Indicates the underlying API project doesn't exist or, more likely, the user did not or no
 * longer has access to it.
 */
@ResponseStatus(HttpStatus.PRECONDITION_FAILED)
public class ApiProjectNotFoundException extends ComputeResourceException {

  private static final String MESSAGE = "API Project '%s' not found";

  public ApiProjectNotFoundException(String apiProjectid) {
    super(String.format(MESSAGE, Preconditions.checkNotNull(apiProjectid)),
        apiProjectid,
        ComputeResourceType.PROJECT,
        /* short name */ null);
  }
}
