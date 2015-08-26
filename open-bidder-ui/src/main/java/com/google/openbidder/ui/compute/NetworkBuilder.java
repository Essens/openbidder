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

import com.google.api.services.compute.model.Network;
import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.project.ProjectUser;

import org.springframework.beans.factory.annotation.Value;

import javax.inject.Inject;

/**
 * Constructs a {@link Network} for a given project.
 */
public class NetworkBuilder {

  private static final String NAME = "network-%s";
  private static final String DESCRIPTION = "Network for project %s.  Created by %s.";

  private final String networkIpRange;

  @Inject
  public NetworkBuilder(@Value("${Network.IP.Range}") String networkIpRange) {
    this.networkIpRange = checkNotNull(networkIpRange);
  }

  public Network build(ProjectUser projectUser) {
    Project project = projectUser.getProject();
    Network network = new Network();
    network.setName(buildNetworkName(project));
    network.setDescription(String.format(DESCRIPTION,
        project.getProjectName(), projectUser.getEmail()));
    network.setIPv4Range(networkIpRange);
    return network;
  }

  private String buildNetworkName(Project project) {
    return String.format(NAME, project.getProjectUuid());
  }
}
