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

package com.google.openbidder.ui.resource.support;

import com.google.openbidder.ui.adexchangebuyer.AdExchangeBuyerClient;
import com.google.openbidder.ui.adexchangebuyer.AdExchangeBuyerService;
import com.google.openbidder.ui.project.ProjectService;
import com.google.openbidder.ui.project.ProjectUser;

import java.util.Set;

/**
 * An extension of {@link AbstractResourceService} that has bindings for
 * {@link AdExchangeBuyerService} to talk to the AdWords API and {@link ProjectUser} for resolving
 * project dependencies. All subclasses are children of projects.
 */
public class AbstractAdExchangeBuyerResourceService<T extends ExternalResource>
    extends AbstractProjectResourceService<T, AdExchangeBuyerClient> {

  private final AdExchangeBuyerService adExchangeBuyerService;

  public AbstractAdExchangeBuyerResourceService(
      ResourceType resourceType,
      Set<ResourceMethod> supportedMethods,
      ProjectService projectService,
      AdExchangeBuyerService adExchangeBuyerService) {

    super(resourceType, supportedMethods, projectService);
    this.adExchangeBuyerService = adExchangeBuyerService;
  }

  @Override
  protected final AdExchangeBuyerClient getService(ProjectUser projectUser) {
    return adExchangeBuyerService.connect(projectUser);
  }
}
