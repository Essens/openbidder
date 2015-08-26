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

package com.google.openbidder.ui.resource.impl;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.openbidder.ui.dao.UserPreferenceDao;
import com.google.openbidder.ui.entity.UserPreference;
import com.google.openbidder.ui.project.ProjectService;
import com.google.openbidder.ui.project.ProjectUser;
import com.google.openbidder.ui.project.exception.ProjectNotFoundException;
import com.google.openbidder.ui.resource.UserResourceService;
import com.google.openbidder.ui.resource.exception.BadRequestException;
import com.google.openbidder.ui.resource.exception.NotProjectOwnerException;
import com.google.openbidder.ui.resource.exception.ProjectUserSelfUpdateException;
import com.google.openbidder.ui.resource.model.UserResource;
import com.google.openbidder.ui.resource.support.AbstractResourceService;
import com.google.openbidder.ui.resource.support.ResourceCollectionId;
import com.google.openbidder.ui.resource.support.ResourceId;
import com.google.openbidder.ui.resource.support.ResourceMethod;
import com.google.openbidder.ui.resource.support.ResourceType;
import com.google.openbidder.ui.user.UserIdService;
import com.google.openbidder.ui.util.db.Transactable;

import com.googlecode.objectify.Objectify;

import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * {@link com.google.openbidder.ui.resource.ResourceService}
 * for {@link com.google.openbidder.ui.resource.model.UserResource}s.
 */
public class UserResourceServiceImpl
    extends AbstractResourceService<ProjectUser, UserResource>
    implements UserResourceService {

  private static final Function<ProjectUser, UserResource> TO_PROJECT_USER =
      new Function<ProjectUser, UserResource>() {
        @Override
        public UserResource apply(ProjectUser projectUser) {
          return build(projectUser);
        }
      };

  private final ProjectService projectService;
  private final UserIdService userIdService;
  private final UserPreferenceDao userPreferenceDao;

  @Inject
  protected UserResourceServiceImpl(
      ProjectService projectService,
      UserIdService userIdService,
      UserPreferenceDao userPreferenceDao) {

    super(ResourceType.USER, EnumSet.allOf(ResourceMethod.class));
    this.projectService = Preconditions.checkNotNull(projectService);
    this.userIdService = Preconditions.checkNotNull(userIdService);
    this.userPreferenceDao = Preconditions.checkNotNull(userPreferenceDao);
  }

  @Override
  protected final ProjectUser getParent(String resourceName) {
    Preconditions.checkNotNull(resourceName);
    long projectId;
    try {
      projectId = Long.parseLong(resourceName);
    } catch (NumberFormatException e) {
      throw new ProjectNotFoundException(resourceName);
    }
    return projectService.getProject(projectId);
  }

  @Override
  protected UserResource get(
      ProjectUser projectUser,
      ResourceId projectUserId,
      Multimap<String, String> params) {

    return build(getProjectUser(projectUser, projectUserId.getResourceName()));
  }

  @Override
  protected List<? extends UserResource> list(
      ProjectUser projectUser,
      ResourceCollectionId resourceCollectionId,
      Multimap<String, String> params) {

    return Lists.transform(getProjectUsers(projectUser), TO_PROJECT_USER);
  }

  @Override
  protected UserResource create(
      ProjectUser projectUser,
      ResourceCollectionId resourceCollectionId,
      final UserResource childResource) {

    final long projectId = projectUser.getProject().getId();
    if (!projectUser.getUserRole().getProjectRole().isOwner()) {
      throw new NotProjectOwnerException(projectId);
    }
    String userEmail = childResource.getUserEmail();
    if (projectUser.getEmail().equals(userEmail)) {
      throw new ProjectUserSelfUpdateException(projectId, userEmail);
    }
    UserPreference updatedUserPreference = userPreferenceDao.updateUserPreferences(
        userEmail,
        new Transactable<UserPreference, UserPreference>() {
          @Override
          public UserPreference work(UserPreference userPreference, Objectify ofy) {
            userPreference.setProjectRole(projectId, childResource.getProjectRole());
            ofy.save().entity(userPreference);
            return userPreference;
          }
        });
    return build(userIdService.buildProjectUser(projectUser.getProject(), updatedUserPreference));
  }

  @Override
  protected UserResource update(
      ProjectUser projectUser,
      ResourceId childResourceId,
      final UserResource childResource) {

    final long projectId = projectUser.getProject().getId();
    if (!projectUser.getUserRole().getProjectRole().isOwner()) {
      throw new NotProjectOwnerException(projectId);
    }
    String resourceEmail = childResourceId.getResourceName();
    if (projectUser.getEmail().equals(resourceEmail)) {
      throw new ProjectUserSelfUpdateException(projectId, resourceEmail);
    }
    ProjectUser result = projectUser;
    if (childResource.hasProjectRole()) {
      if (childResource.getProjectRole() == null) {
        throw new BadRequestException("projectRole");
      }
      UserPreference updatedUserPreference = userPreferenceDao.updateUserPreferences(
          resourceEmail,
          new Transactable<UserPreference, UserPreference>() {
            @Override
            public UserPreference work(UserPreference userPreference, Objectify ofy) {
              userPreference.setProjectRole(projectId, childResource.getProjectRole());
              ofy.save().entity(userPreference);
              return userPreference;
            }
          });
      result = userIdService.buildProjectUser(projectUser.getProject(), updatedUserPreference);
    }
    return build(result);
  }

  @Override
  protected void delete(
      ProjectUser projectUser,
      ResourceId childResourceId) {

    final long projectId = projectUser.getProject().getId();
    if (!projectUser.getUserRole().getProjectRole().isOwner()) {
      throw new NotProjectOwnerException(projectId);
    }
    String resourceEmail = childResourceId.getResourceName();
    if (projectUser.getEmail().equals(resourceEmail)) {
      throw new ProjectUserSelfUpdateException(projectId, resourceEmail);
    }
    userPreferenceDao.updateUserPreferences(
        childResourceId.getResourceName(),
        new Transactable<UserPreference, Void>() {
          @Override public @Nullable Void work(UserPreference userPreference, Objectify ofy) {
            userPreference.removeFromProject(projectId);
            if (userPreference.hasNoProjects()) {
              ofy.delete().entity(userPreference);
            } else {
              ofy.save().entity(userPreference);
            }
            return null;
          }
        });
  }

  private ProjectUser getProjectUser(ProjectUser projectUser, String email) {
    return projectService.getProjectForUser(projectUser.getProject().getId(), email);
  }

  private List<ProjectUser> getProjectUsers(ProjectUser projectUser) {
    return projectService.getProjectUsers(projectUser.getProject().getId());
  }

  private static UserResource build(ProjectUser projectUser) {
    UserResource userResource = new UserResource();
    userResource.setId(ResourceType.USER.getResourceId(
        Long.toString(projectUser.getProject().getId()),
        projectUser.getEmail()
    ));
    userResource.setUserEmail(projectUser.getEmail());
    userResource.setProjectRole(projectUser.getUserRole().getProjectRole());
    return userResource;
  }
}
