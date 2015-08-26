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

package com.google.openbidder.ui.dao.impl;

import com.google.common.base.Preconditions;
import com.google.openbidder.ui.dao.ProjectDao;
import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.project.exception.ProjectNotFoundException;
import com.google.openbidder.ui.util.db.Db;
import com.google.openbidder.ui.util.db.Transactable;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;

import java.util.Map;

import javax.inject.Inject;

/**
 * Default implementation of {@link ProjectDao}.
 */
public class ProjectDaoImpl implements ProjectDao {
  private final ObjectifyFactory objectifyFactory;

  @Inject
  public ProjectDaoImpl(ObjectifyFactory objectifyFactory) {
    this.objectifyFactory = Preconditions.checkNotNull(objectifyFactory);
  }

  @Override
  public Project getProjectById(long projectId) {
    Objectify ofy = objectifyFactory.begin();
    return getProjectById(ofy, projectId);
  }

  private Project getProjectById(Objectify ofy, long projectId) {
    Project project = ofy.load().type(Project.class).id(projectId).now();
    if (project == null) {
      throw new ProjectNotFoundException(projectId);
    }
    return project;
  }

  @Override
  public Map<Long, Project> getProjectsByIds(Iterable<Long> projectIds) {
    Objectify ofy = objectifyFactory.begin();
    return ofy.load().type(Project.class).ids(projectIds);
  }

  @Override
  public long createProject(Project project) {
    Preconditions.checkNotNull(project);
    Objectify ofy = objectifyFactory.begin();
    return ofy.save().entity(project).now().getId();
  }

  @Override
  public <T> T updateProject(final long projectId, final Transactable<Project, T> worker) {
    return Db.updateInTransaction(objectifyFactory, Project.key(projectId), worker);
  }
}
