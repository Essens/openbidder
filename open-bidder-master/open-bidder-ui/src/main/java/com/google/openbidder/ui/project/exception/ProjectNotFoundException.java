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

package com.google.openbidder.ui.project.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Indicates that a requested {@link com.google.openbidder.ui.entity.Project}
 * (by ID) was not found.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProjectNotFoundException extends ProjectException {

  private static final String MESSAGE = "Project %s not found";

  public ProjectNotFoundException(long projectId) {
    this(Long.toString(projectId));
  }

  public ProjectNotFoundException(String projectId) {
    this(String.format(MESSAGE, projectId), projectId);
  }

  public ProjectNotFoundException(String message, String projectId) {
    super(message, projectId);
  }
}
