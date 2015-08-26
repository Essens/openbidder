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

package com.google.openbidder.ui.compute.impl;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.compute.Compute;
import com.google.common.base.Preconditions;
import com.google.openbidder.ui.compute.ComputeClient;
import com.google.openbidder.ui.compute.ComputeService;
import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.project.ProjectUser;
import com.google.openbidder.ui.user.AuthorizationService;

import javax.inject.Inject;

/**
 * Default implementation for {@link ComputeService}.
 */
public class ComputeServiceImpl implements ComputeService {

  private final AuthorizationService authorizationService;
  private final HttpTransport httpTransport;
  private final JsonFactory jsonFactory;

  @Inject
  public ComputeServiceImpl(
      AuthorizationService authorizationService,
      HttpTransport httpTransport,
      JsonFactory jsonFactory) {

    this.authorizationService = Preconditions.checkNotNull(authorizationService);
    this.httpTransport = Preconditions.checkNotNull(httpTransport);
    this.jsonFactory = Preconditions.checkNotNull(jsonFactory);
  }

  @Override
  public ComputeClient connect(ProjectUser projectUser) {
    Preconditions.checkNotNull(projectUser);
    Compute compute = new Compute.Builder(
            httpTransport,
            jsonFactory,
            authorizationService.getCredentialsForProject(projectUser))
        .setApplicationName("Open Bidder")
        .build();
    Project project = projectUser.getProject();
    return new ComputeClientImpl(compute, project.getId(), project.getApiProjectId());
  }
}
