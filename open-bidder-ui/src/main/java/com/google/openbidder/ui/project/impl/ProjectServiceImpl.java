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

package com.google.openbidder.ui.project.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.openbidder.ui.dao.ProjectDao;
import com.google.openbidder.ui.dao.UserPreferenceDao;
import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.entity.UserPreference;
import com.google.openbidder.ui.entity.support.ProjectRole;
import com.google.openbidder.ui.entity.support.UserRole;
import com.google.openbidder.ui.project.ProjectService;
import com.google.openbidder.ui.project.ProjectUser;
import com.google.openbidder.ui.project.exception.NoProjectWriteAccessException;
import com.google.openbidder.ui.project.exception.ProjectNotFoundException;
import com.google.openbidder.ui.project.exception.ProjectUserNotFoundException;
import com.google.openbidder.ui.user.UserIdService;
import com.google.openbidder.ui.user.exception.UserNotFoundException;
import com.google.openbidder.ui.util.db.Transactable;

import com.googlecode.objectify.Objectify;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Default implementation for {@link ProjectService}.
 */
public class ProjectServiceImpl implements ProjectService {

  private static final Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);

  private final UserIdService userIdService;
  private final UserPreferenceDao userPreferenceDao;
  private final ProjectDao projectDao;

  @Inject
  public ProjectServiceImpl(
      UserIdService userIdService,
      UserPreferenceDao userPreferenceDao,
      ProjectDao projectDao) {

    this.userIdService = checkNotNull(userIdService);
    this.userPreferenceDao = checkNotNull(userPreferenceDao);
    this.projectDao = checkNotNull(projectDao);
  }

  @Override
  // TODO(wshields): use Spring Security annotations to verify authorization
  public ProjectUser getProject(long projectId) {
    String email = userIdService.getUserId();
    return getProjectForUser(projectId, email);
  }

  @Override
  public ProjectUser getProjectForUser(long projectId, String userEmail) {
    checkNotNull(userEmail);
    Project project = projectDao.getProjectById(projectId);
    UserPreference userPreference = userPreferenceDao.getUserPreferences(userEmail);
    if (!userPreference.getProjectRole(projectId).isRead()) {
      throw new ProjectUserNotFoundException(projectId, userEmail);
    }
    return userIdService.buildProjectUser(project, userPreference);
  }

  @Override
  public UserPreference setAsDefaultProject(final ProjectUser projectUser) {
    return userPreferenceDao.updateUserPreferences(projectUser.getEmail(),
        new Transactable<UserPreference, UserPreference>() {
          @Override public UserPreference work(UserPreference userPreference, Objectify ofy) {
            userPreference.setDefaultProject(projectUser.getProject().getId());
            ofy.save().entity(userPreference);
            return userPreference;
          }});
  }

  @Override
  public List<ProjectUser> getAllProjectsForUser() {
    // First, build a list of projects the user should have access to.
    final String email = userIdService.getUserId();
    final UserPreference userPreference;
    try {
      userPreference = userPreferenceDao.getUserPreferences(email);
    } catch (UserNotFoundException e) {
      return new ArrayList<>();
    }
    final Map<Long, UserRole> userRoles = new HashMap<>();
    for (UserRole userRole : userPreference.getUserRoles()) {
      if (userRole.getProjectRole().isRead()) {
        userRoles.put(userRole.getProject().getId(), userRole);
      }
    }

    // Batch get all projects and build the summaries.
    Map<Long, Project> projects = projectDao.getProjectsByIds(userRoles.keySet());
    Iterable<ProjectUser> projectUsers = Iterables.transform(projects.entrySet(),
        new Function<Entry<Long, Project>, ProjectUser>() {
          @Override public ProjectUser apply(Entry<Long, Project> entry) {
            return userIdService.buildProjectUser(entry.getValue(), userPreference);
          }});

    // The rest of this is to ensure a consistent ordering
    List<ProjectUser> projectUserList = Lists.newArrayList(projectUsers);
    Collections.sort(projectUserList);
    return projectUserList;
  }

  @Override
  public List<ProjectUser> getProjectUsers(final long projectId) {
    final Project project = projectDao.getProjectById(projectId);
    List<UserPreference> userPreferences = Lists.newArrayList(
        userPreferenceDao.getAllUsersForProject(projectId));
    return Lists.transform(userPreferences, new Function<UserPreference, ProjectUser>() {
      @Override public ProjectUser apply(UserPreference userPreference) {
        return userIdService.buildProjectUser(project, userPreference);
      }});
  }

  @Override
  public ProjectUser insertProject(Project project) {
    String email = userIdService.getUserId();
    checkNotNull(project);
    final long projectId = projectDao.createProject(project);
    UserPreference userPreference = userPreferenceDao.updateUserPreferences(email,
        new Transactable<UserPreference, UserPreference>() {
          @Override public UserPreference work(UserPreference item, Objectify ofy) {
            item.setProjectRole(projectId, ProjectRole.OWNER);
            item.setDefaultProject(projectId);
            ofy.save().entity(item);
            return item;
          }});
    return userIdService.buildProjectUser(project, userPreference);
  }

  @Override
  public Project updateProject(
      long projectId,
      final Function<Project, Project> projectTransformer) {

    checkNotNull(projectTransformer);
    ProjectUser projectUser = getProject(projectId);
    if (!projectUser.getUserRole().getProjectRole().isWrite()) {
      throw new NoProjectWriteAccessException(projectId, userIdService.getUserId());
    }
    Project project = projectDao.updateProject(projectId, new Transactable<Project, Project>() {
      @Override public Project work(Project existingProject, Objectify ofy) {
        Project updatedProject = projectTransformer.apply(existingProject);
        ofy.save().entity(updatedProject);
        return updatedProject;
      }});
    if (project == null) {
      throw new ProjectNotFoundException(projectId);
    }
    return project;
  }

  @Override
  public void deleteProject(final long projectId) {
    final boolean[] found = {false};
    projectDao.updateProject(projectId, new Transactable<Project, Void>() {
      @Override public @Nullable Void work(Project item, Objectify ofy) {
        found[0] = true;
        ofy.delete().entity(item);
        return null;
      }
    });
    if (!found[0]) {
      throw new ProjectNotFoundException(projectId);
    }

    userPreferenceDao.removeProjectFromAllUserPreferences(projectId);
    logger.info("Project {} has been successfully deleted", projectId);
  }
}
