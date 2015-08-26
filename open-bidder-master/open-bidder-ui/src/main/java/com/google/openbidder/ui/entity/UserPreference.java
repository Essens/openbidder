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

package com.google.openbidder.ui.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.openbidder.ui.entity.support.ProjectRole;
import com.google.openbidder.ui.entity.support.UserRole;
import com.google.openbidder.ui.user.exception.UserNotInProjectException;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

/**
 * User preferences.
 */
@Entity
public class UserPreference implements Serializable {

  private static final Predicate<UserRole> IS_VISIBLE = new Predicate<UserRole>() {
    @Override
    public boolean apply(UserRole userRole) {
      return userRole.getProjectRole() != null && userRole.getProjectRole().isRead();
    }
  };

  @Id
  @Index
  private String email;

  @Index
  private Key<Project> defaultProject;

  private boolean admin;

  @Index
  // Role on each project. The first project is the user's default project
  private List<UserRole> userRoles = new ArrayList<>();

  @SuppressWarnings("unused")
  private UserPreference() {
  }

  public UserPreference(String email) {
    this.email = Preconditions.checkNotNull(email);
  }

  public String getEmail() {
    return email;
  }

  public Key<Project> getDefaultProject() {
    return defaultProject;
  }

  public boolean isDefaultProject(long projectId) {
    return defaultProject != null && defaultProject.getId() == projectId;
  }

  /**
   * Sets the default project for this user. Passing in {@code null} will clear the default
   * project.
   *
   * @throws UserNotInProjectException if the user isn't in the given project.
   */
  public void setDefaultProject(@Nullable Key<Project> defaultProject) {
    if (defaultProject != null) {
      if (!isProjectVisible(defaultProject.getId())) {
        throw new UserNotInProjectException(email, defaultProject.getId());
      }
    }
    this.defaultProject = defaultProject;
  }

  public void setDefaultProject(long projectId) {
    setDefaultProject(Project.key(projectId));
  }

  /**
   * This needs to be saved because there is no way for the App Engine environment to test if
   * a given User is an admin or not, only the current user.
   * @return {@code true} if this user is an admin, otherwise {@code false}.
   */
  public boolean isAdmin() {
    return admin;
  }

  public void setAdmin(boolean admin) {
    this.admin = admin;
  }

  public List<UserRole> getUserRoles() {
    return Collections.unmodifiableList(userRoles);
  }

  public void setUserRoles(List<UserRole> userRoles) {
    this.userRoles = new ArrayList<>(userRoles);
  }

  public boolean hasNoProjects() {
    return userRoles == null || userRoles.isEmpty();
  }

  // The implementation of the methods is temporary as Objectify 4's @Mapify will greatly
  // simplify it and release is (allegedly) imminent.

  /**
   * Gets the {@link UserRole} for a given project. If the user no role an instance is
   * returned with a {@link UserRole#getProjectRole()} of {@link ProjectRole#NONE}.
   */
  public UserRole getUserRole(final long projectId) {
    return Iterables.find(userRoles, new Predicate<UserRole>() {
      @Override
      public boolean apply(UserRole userRole) {
        return userRole.getProjectId() == projectId;
      }
    }, new UserRole(projectId, ProjectRole.NONE));
  }

  /**
   * As per {@link #getUserRole(long)} but only returns the {@link ProjectRole}.
   */
  public ProjectRole getProjectRole(long projectId) {
    return getUserRole(projectId).getProjectRole();
  }

  /**
   * Can this user see the given project?
   */
  public boolean isProjectVisible(long projectId) {
    return getProjectRole(projectId).isRead();
  }

  /**
   * @return Number of projects the user has visible.
   */
  public int getVisibleProjectCount() {
    return userRoles == null ? 0 : Collections2.filter(userRoles, IS_VISIBLE).size();
  }

  /**
   * Grant a {@link ProjectRole} to a user on a given {@link Project}. If the role is
   * {@link ProjectRole#NONE} then remove them from that project.
   */
  public void setProjectRole(long projectId, ProjectRole projectRole) {
    Preconditions.checkNotNull(projectRole);
    if (projectRole == ProjectRole.NONE) {
      removeFromProject(projectId);
      return;
    } else {
      // this method also does some cleanup removing any userRoles where the role is NONE
      boolean found = false;
      for (Iterator<UserRole> iter = userRoles.iterator(); iter.hasNext(); ) {
        UserRole role = iter.next();
        if (role.getProject().getId() == projectId) {
          role.setProjectRole(projectRole);
          found = true;
        } else if (role.getProjectRole() == ProjectRole.NONE) {
          if (defaultProject != null && defaultProject.getId() == projectId) {
            defaultProject = null;
          }
          iter.remove();
        }
      }
      if (!found) {
        userRoles.add(new UserRole(projectId, projectRole));
      }
    }
    if (defaultProject == null) {
      // userRoles cannot be empty here. If it started empty an item was added.
      defaultProject = userRoles.get(0).getProject();
    }
  }

  /**
   * Remove a user from a given {@link Project}.
   */
  public void removeFromProject(long projectId) {
    // this method also does some cleanup removing any userRoles where the role is NONE
    for (Iterator<UserRole> iter = userRoles.iterator(); iter.hasNext(); ) {
      UserRole role = iter.next();
      if (role.getProject().getId() == projectId ||
          role.getProjectRole() == ProjectRole.NONE) {
        iter.remove();
      }
    }

    if (defaultProject != null && defaultProject.getId() == projectId) {
      defaultProject = null;
    }

    // defaultProject may already be null, hence the separation from the previous block
    if (defaultProject == null && !userRoles.isEmpty()) {
      defaultProject = userRoles.get(0).getProject();
    }
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("email", email)
        .add("defaultProject", defaultProject)
        .add("userRoles", userRoles)
        .toString();
  }

  public static Key<UserPreference> key(String email) {
    return Key.create(UserPreference.class, Preconditions.checkNotNull(email));
  }
}
