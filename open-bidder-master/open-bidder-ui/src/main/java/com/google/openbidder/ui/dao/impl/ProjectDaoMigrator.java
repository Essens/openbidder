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

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.openbidder.ui.compute.BidderParameters;
import com.google.openbidder.ui.dao.ProjectDao;
import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.util.db.Transactable;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;

import java.util.Map;

import javax.inject.Inject;

/**
 * Enhanced implementation of {@link ProjectDao}, migrated obsolete data.
 */
public class ProjectDaoMigrator extends ProjectDaoImpl {
  private final BidderParameters bidderParameters;

  private final Function<Project, Project> MIGRATE = new Function<Project, Project>() {
    @Override public Project apply(Project project) {
      return project;
    }
  };

  @Inject
  public ProjectDaoMigrator(
      ObjectifyFactory objectifyFactory,
      BidderParameters bidderParameters) {
    super(objectifyFactory);
    this.bidderParameters = bidderParameters;
  }

  @Override
  public Project getProjectById(long projectId) {
    return MIGRATE.apply(super.getProjectById(projectId));
  }

  @Override
  public Map<Long, Project> getProjectsByIds(Iterable<Long> projectIds) {
    return Maps.transformValues(super.getProjectsByIds(projectIds), MIGRATE);
  }

  @Override
  public <T> T updateProject(long projectId, final Transactable<Project, T> worker) {
    return super.updateProject(projectId, new Transactable<Project, T>() {
      @Override public T work(Project item, Objectify ofy) {
        return worker.work(MIGRATE.apply(item), ofy);
      }
    });
  }
}
