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

import com.google.openbidder.ui.compute.ComputeResourceType;
import com.google.openbidder.ui.compute.ResourceName;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * The given resource already exists.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ComputeResourceAlreadyExistsException extends ComputeResourceException {

  private static final String MESSAGE = "%s '%s' already exists for project '%s'";

  public ComputeResourceAlreadyExistsException(ResourceName resourceName) {
    this(resourceName.getApiProjectId(),
        resourceName.getResourceType(),
        resourceName.getResourceName());
  }

  public ComputeResourceAlreadyExistsException(
      String apiProjectId,
      ComputeResourceType resourceType,
      String resourceName) {

    super(String.format(MESSAGE, resourceType.getTitle(), resourceName, apiProjectId),
        apiProjectId,
        resourceType,
        resourceName);
  }
}
