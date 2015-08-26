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

package com.google.openbidder.ui.compute;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.project.ProjectUser;
import com.google.openbidder.ui.user.exception.NoCredentialsForProjectException;
import com.google.openbidder.util.testing.FakeClock;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

/**
 * Fake implementation of {@link ComputeService} that returns {@link FakeComputeClient}s.
 */
public class FakeComputeService implements ComputeService {

  private final Multimap<Long, String> authorizedUsers = HashMultimap.create();
  private final Map<Long, FakeComputeClient> computeClients = new HashMap<>();
  private final FakeClock fakeClock;

  @Inject
  public FakeComputeService(FakeClock fakeClock) {
    this.fakeClock = checkNotNull(fakeClock);
  }

  @Override
  public ComputeClient connect(ProjectUser projectUser) {
    long projectId = projectUser.getProject().getId();
    String email = projectUser.getEmail();
    if (!authorizedUsers.containsEntry(projectId, email)) {
      throw new NoCredentialsForProjectException(projectId, email);
    }
    return getOrCreateClient(projectUser);
  }

  public FakeComputeClient getOrCreateClient(ProjectUser projectUser) {
    Project project = projectUser.getProject();
    return getOrCreateClient(project.getId(), project.getApiProjectId());
  }

  public FakeComputeClient getOrCreateClient(long projectId, String apiProjectId) {
    FakeComputeClient computeClient = computeClients.get(projectId);
    if (computeClient == null) {
      computeClient = new FakeComputeClient(projectId, apiProjectId, fakeClock);
      computeClients.put(projectId, computeClient);
    }
    return computeClient;
  }

  public FakeComputeClient getClient(long projectId) {
    return computeClients.get(projectId);
  }

  public void authorize(ProjectUser projectUser) {
    authorize(projectUser.getProject().getId(), projectUser.getEmail());
  }

  public void authorize(long projectId, String email) {
    authorizedUsers.put(projectId, email);
  }

  public void clear() {
    authorizedUsers.clear();
    computeClients.clear();
  }
}
