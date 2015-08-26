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

package com.google.openbidder.ui.user.exception;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Indicates an action was attempted on a project the user is not in.
 */
public class UserNotInProjectException extends RuntimeException {

  private static final String MESSAGE = "User %s is not in project %d";

  private final String email;
  private final long projectId;

  public UserNotInProjectException(String email, long projectId) {
    super(String.format(MESSAGE, email, projectId));
    this.email = checkNotNull(email);
    this.projectId = projectId;
  }

  public String getEmail() {
    return email;
  }

  public long getProjectId() {
    return projectId;
  }
}
