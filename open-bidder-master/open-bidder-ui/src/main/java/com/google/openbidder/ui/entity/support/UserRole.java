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

package com.google.openbidder.ui.entity.support;

import com.google.api.client.auth.oauth2.Credential;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.openbidder.ui.entity.Project;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Index;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.Serializable;

import javax.annotation.Nullable;

/**
 * The role of a user on a specific {@link Project}..
 */
public class UserRole implements Serializable {

  @Index
  private Key<Project> project;

  private ProjectRole projectRole;

  @Nullable
  private String accessToken;

  @Nullable
  private String refreshToken;

  @Nullable
  private Long expirationTimeMillis;

  @SuppressWarnings("unused")
  private UserRole() {
  }

  public UserRole(long projectId, ProjectRole projectRole) {
    this(Project.key(projectId), projectRole);
  }

  public UserRole(Key<Project> project, ProjectRole projectRole) {
    this.project = Preconditions.checkNotNull(project);
    this.projectRole = Preconditions.checkNotNull(projectRole);
  }

  /**
   * @return {@link Project} {@link Key}.
   */
  @JsonIgnore
  public Key<Project> getProject() {
    return project;
  }

  /**
   * @return {@link Project} ID.
   */
  public long getProjectId() {
    return project.getId();
  }

  /**
   * Sets the {@link Project} {@link Key}.
   */
  public void setProject(Key<Project> project) {
    this.project = Preconditions.checkNotNull(project);
  }

  /**
   * @return {@link ProjectRole} for this {@link Project}.
   */
  public ProjectRole getProjectRole() {
    return projectRole;
  }

  /**
   * Sets the {@link ProjectRole} for a {@link Project}.
   */
  public void setProjectRole(ProjectRole projectRole) {
    this.projectRole = Preconditions.checkNotNull(projectRole);
  }

  @Nullable
  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(@Nullable String accessToken) {
    this.accessToken = accessToken;
  }

  @Nullable
  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(@Nullable String refreshToken) {
    this.refreshToken = refreshToken;
  }

  @Nullable
  public Long getExpirationTimeMillis() {
    return expirationTimeMillis;
  }

  public void setExpirationTimeMillis(@Nullable Long expirationTimeMillis) {
    this.expirationTimeMillis = expirationTimeMillis;
  }

  /**
   * Does this user have saved OAuth2 credentials?
   */
  public boolean isAuthorized() {
    return refreshToken != null
        && accessToken != null
        && expirationTimeMillis != null;
  }

  /**
   * Load OAuth credentials into the supplied {@link Credential}.
   */
  public boolean loadCredentials(Credential credential) {
    Preconditions.checkNotNull(credential);
    if (!isAuthorized()) {
      return false;
    }
    credential.setRefreshToken(refreshToken);
    credential.setAccessToken(accessToken);
    credential.setExpirationTimeMilliseconds(expirationTimeMillis);
    return true;
  }

  /**
   * Save {@link Credential} into this role.
   */
  public void storeCredentials(Credential credential) {
    refreshToken = credential.getRefreshToken();
    accessToken = credential.getAccessToken();
    expirationTimeMillis = credential.getExpirationTimeMilliseconds();
  }

  /**
   * Delete {@link Credential} for this role.
   */
  @SuppressWarnings("unused")
  public void deleteCredentials(Credential credential) {
    refreshToken = null;
    accessToken = null;
    expirationTimeMillis = null;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("project", project)
        .add("projectRole", projectRole)
        .toString();
  }
}
