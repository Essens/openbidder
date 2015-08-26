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

import static com.google.common.base.Preconditions.checkNotNull;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Could not find a given user and {@link com.google.openbidder.ui.entity.Project} ID.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProjectUserNotFoundException extends ProjectUserException {

  private static final String MESSAGE = "User '%s' not found for project '%s'";

  public ProjectUserNotFoundException(long projectId, String email) {
    this(Long.toString(projectId), email);
  }

  public ProjectUserNotFoundException(String projectId, String email) {
    super(String.format(MESSAGE,
        checkNotNull(email, "email is null"),
        checkNotNull(projectId, "projectId is null")),
        projectId, email);
  }
}
