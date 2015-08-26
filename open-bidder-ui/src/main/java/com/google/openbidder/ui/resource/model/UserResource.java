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

package com.google.openbidder.ui.resource.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.openbidder.ui.entity.support.ProjectRole;
import com.google.openbidder.ui.resource.support.ExternalResource;
import com.google.openbidder.ui.util.json.ProjectRoleDeserializer;
import com.google.openbidder.ui.util.validation.Create;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.hibernate.validator.constraints.Email;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * Represents a user within the context of a given {@link ProjectResource}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserResource extends ExternalResource {

  @NotNull(groups = {Create.class})
  @Email(groups = {Create.class})
  private String userEmail;

  @NotNull(groups = {Create.class})
  private ProjectRole projectRole;

  private boolean hasUserEmail;
  private boolean hasProjectRole;

  public String getUserEmail() {
    return userEmail;
  }

  public void setUserEmail(String userEmail) {
    this.userEmail = userEmail;
    hasUserEmail = true;
  }

  public void clearUserEmail() {
    userEmail = null;
    hasUserEmail = false;
  }

  public boolean hasUserEmail() {
    return hasUserEmail;
  }

  public ProjectRole getProjectRole() {
    return projectRole;
  }

  @JsonDeserialize(using = ProjectRoleDeserializer.class)
  public void setProjectRole(ProjectRole projectRole) {
    this.projectRole = projectRole;
    hasProjectRole = true;
  }

  public void clearProjectRole() {
    projectRole = null;
    hasProjectRole = false;
  }

  public boolean hasProjectRole() {
    return hasProjectRole;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(super.hashCode(), userEmail, projectRole);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof UserResource) || !super.equals(o)) {
      return false;
    }
    UserResource other = (UserResource) o;
    return Objects.equal(userEmail, other.userEmail)
        && Objects.equal(projectRole, other.projectRole)
        && Objects.equal(hasUserEmail, other.hasUserEmail)
        && Objects.equal(hasProjectRole, other.hasProjectRole);
  }

  @Override
  protected MoreObjects.ToStringHelper toStringHelper() {
    return super.toStringHelper()
        .add("userEmail", userEmail)
        .add("projectRole", projectRole);
  }
}
