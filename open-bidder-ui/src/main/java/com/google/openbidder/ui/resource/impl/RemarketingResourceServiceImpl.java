/*
 * Copyright 2013 Google Inc. All Rights Reserved.
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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.openbidder.remarketing.model.Remarketing.Action;
import com.google.openbidder.storage.dao.CloudStorageDao;
import com.google.openbidder.ui.cloudstorage.StorageService;
import com.google.openbidder.ui.project.ProjectService;
import com.google.openbidder.ui.project.ProjectUser;
import com.google.openbidder.ui.resource.RemarketingResourceService;
import com.google.openbidder.ui.resource.model.RemarketingResource;
import com.google.openbidder.ui.resource.support.AbstractProjectResourceService;
import com.google.openbidder.ui.resource.support.ResourceCollectionId;
import com.google.openbidder.ui.resource.support.ResourceId;
import com.google.openbidder.ui.resource.support.ResourceMethod;
import com.google.openbidder.ui.resource.support.ResourceType;
import com.google.openbidder.ui.util.DaoFactory;
import com.google.protobuf.MessageLite;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * {@link com.google.openbidder.ui.resource.ResourceService} for {@link RemarketingResource}s.
 */
public class RemarketingResourceServiceImpl
    extends AbstractProjectResourceService<RemarketingResource, StorageService>
    implements RemarketingResourceService {

  private static final ImmutableSet<ResourceMethod> ALLOWED_RESOURCE_METHODS =
      Sets.immutableEnumSet(
          ResourceMethod.CREATE,
          ResourceMethod.DELETE,
          ResourceMethod.LIST,
          ResourceMethod.UPDATE
      );
  private static final String ACTION_PREFIX = "action";

  private final StorageService storageService;
  private final DaoFactory daoFactory;

  @Inject
  protected RemarketingResourceServiceImpl(
      ProjectService projectService,
      StorageService storageService,
      DaoFactory daoFactory) {

    super(ResourceType.ACTION, ALLOWED_RESOURCE_METHODS, projectService);
    this.storageService = checkNotNull(storageService);
    this.daoFactory = checkNotNull(daoFactory);
  }

  @Override
  protected StorageService getService(ProjectUser projectUser) {
    return storageService;
  }

  @Override
  protected List<? extends RemarketingResource> list(
      StorageService service,
      ProjectUser projectUser,
      ResourceCollectionId resourceCollectionId,
      Multimap<String, String> params) {
    String projectId = resourceCollectionId.getParent().getResourceName();
    List<RemarketingResource> actionResources = new ArrayList<>();
    CloudStorageDao<MessageLite> dao = daoFactory.buildDao(projectUser);
    Iterable<Action> actions = dao.findAll(
        Action.class,
        getActionObjectName(projectId, null),
        getBucketName(projectUser.getProject().getUserDistUri()));

    for (Action action : actions) {
      actionResources.add(generateRemarketingResource(action));
    }

    return actionResources;
  }

  @Override
  protected RemarketingResource create(
      StorageService service,
      ProjectUser projectUser,
      ResourceCollectionId resourceCollectionId,
      RemarketingResource newResource) {
    String projectId = resourceCollectionId.getParent().getResourceName();
    Action action = generateAction(newResource);
    CloudStorageDao<MessageLite> dao = daoFactory.buildDao(projectUser);
    dao.createObject(
        action,
        getBucketName(projectUser.getProject().getUserDistUri()),
        getActionObjectName(projectId, action.getActionId()));
    return newResource;
  }

  @Override
  protected void delete(
      StorageService service,
      ProjectUser projectUser,
      ResourceId resourceId) {
    String projectId = resourceId.getParent().getParent().getResourceName();
    String actionId = resourceId.getResourceName();
    CloudStorageDao<MessageLite> dao = daoFactory.buildDao(projectUser);
    dao.deleteObject(
        getBucketName(projectUser.getProject().getUserDistUri()),
        getActionObjectName(projectId, actionId));
  }

  @Override
  protected RemarketingResource update(
      StorageService service,
      ProjectUser projectUser,
      ResourceId childResourceId,
      RemarketingResource updatedResource) {
    String projectId = childResourceId.getParent().getParent().getResourceName();
    Action action = generateAction(updatedResource);
    CloudStorageDao<MessageLite> dao = daoFactory.buildDao(projectUser);
    dao.createObject(
        action,
        getBucketName(projectUser.getProject().getUserDistUri()),
        getActionObjectName(projectId, action.getActionId()));
    return updatedResource;
  }

  private RemarketingResource generateRemarketingResource(Action action) {
    RemarketingResource actionResource = new RemarketingResource();
    actionResource.setDescription(action.getDescription());
    actionResource.setIsEnabled(action.getIsEnabled());
    actionResource.setActionId(action.getActionId());
    actionResource.setMaxCpm(action.getMaxCpm());
    actionResource.setClickThroughUrl(action.getClickThroughUrl());
    actionResource.setCreative(action.getCreative());
    return actionResource;
  }

  private Action generateAction(RemarketingResource remarketingResource) {
    return Action.newBuilder()
        .setActionId(CharMatcher.WHITESPACE.removeFrom(remarketingResource.getActionId()))
        .setDescription(remarketingResource.getDescription())
        .setIsEnabled(remarketingResource.getIsEnabled())
        .setMaxCpm(remarketingResource.getMaxCpm())
        .setClickThroughUrl(remarketingResource.getClickThroughUrl())
        .setCreative(remarketingResource.getCreative())
        .build();
  }

  private String getActionObjectName(String projectId, @Nullable String actionId) {
    return Joiner.on("-").skipNulls().join(ACTION_PREFIX, projectId, actionId);
  }

  private String getBucketName(String bucketPath) {
    return  bucketPath.substring(bucketPath.lastIndexOf('/') + 1);
  }
}