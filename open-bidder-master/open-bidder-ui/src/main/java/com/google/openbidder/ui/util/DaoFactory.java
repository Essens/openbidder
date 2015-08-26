/*
 * Copyright 2014 Google Inc. All Rights Reserved.
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

package com.google.openbidder.ui.util;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import com.google.common.base.Preconditions;
import com.google.openbidder.cloudstorage.GoogleCloudStorage;
import com.google.openbidder.cloudstorage.GoogleCloudStorageFactory;
import com.google.openbidder.storage.dao.CloudStorageDao;
import com.google.openbidder.storage.dao.Dao;
import com.google.openbidder.storage.utils.ProtobufConverter;
import com.google.openbidder.ui.project.ProjectUser;
import com.google.openbidder.ui.user.AuthorizationService;
import com.google.protobuf.MessageLite;

import javax.inject.Inject;

/**
 * Simple factory for creating {@link Dao}s.
 */
public class DaoFactory {

  private final HttpTransport httpTransport;
  private final AuthorizationService authorizationService;

  @Inject
  public DaoFactory(
    HttpTransport httpTransport,
    AuthorizationService authorizationService) {
    this.httpTransport = Preconditions.checkNotNull(httpTransport);
    this.authorizationService = Preconditions.checkNotNull(authorizationService);
  }

  public CloudStorageDao<MessageLite> buildDao(ProjectUser projectUser) {
    Credential credential = authorizationService.getCredentialsForProject(projectUser);
    GoogleCloudStorage cloudStorage = GoogleCloudStorageFactory.newFactory()
        .setHttpTransport(httpTransport)
        .setApiProjectNumber(projectUser.getProject().getApiProjectNumber())
        .setCredential(credential)
        .build();
    return new CloudStorageDao<>(cloudStorage, new ProtobufConverter());
  }
}