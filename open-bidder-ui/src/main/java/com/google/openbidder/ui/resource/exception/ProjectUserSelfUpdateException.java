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

package com.google.openbidder.ui.resource.exception;

import static com.google.common.base.Preconditions.checkNotNull;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * User tried to update their own record.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ProjectUserSelfUpdateException extends RuntimeException {

  private static final String MESSAGE = "User %s cannot edit themselves for project %d";

  private final long projectId;
  private final String email;

  public ProjectUserSelfUpdateException(long projectId, String email) {
    super(String.format(MESSAGE, email, projectId));
    this.projectId = projectId;
    this.email = checkNotNull(email);
  }

  public long getProjectId() {
    return projectId;
  }

  public String getEmail() {
    return email;
  }
}
