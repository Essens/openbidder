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

import com.google.api.services.compute.model.Image;
import com.google.openbidder.ui.compute.ComputeService;
import com.google.openbidder.ui.project.ProjectService;
import com.google.openbidder.ui.resource.model.ImageResource;
import com.google.openbidder.ui.resource.support.AbstractComputeResourceService;
import com.google.openbidder.ui.resource.support.ResourceCollectionId;
import com.google.openbidder.ui.resource.support.ResourceMethod;
import com.google.openbidder.ui.resource.support.ResourceType;
import com.google.openbidder.ui.util.web.WebUtils;

import java.util.Set;

import javax.inject.Inject;

/**
 * Base implementation of {@link com.google.openbidder.ui.resource.ResourceService}
 * for {ImageResource}s.
 */
public abstract class AbstractImageResourceServiceImpl
    extends AbstractComputeResourceService<ImageResource> {

  @Inject
  public AbstractImageResourceServiceImpl(
      ResourceType resourceType,
      Set<ResourceMethod> supportedMethods,
      ProjectService projectService,
      ComputeService computeService) {
    super(resourceType, supportedMethods, projectService, computeService);
  }

  protected static ImageResource build(ResourceCollectionId resourceCollectionId, Image image) {
    ImageResource imageResource = new ImageResource();
    imageResource.setId(resourceCollectionId.getResourceId((image.getName())));
    imageResource.setDescription(image.getDescription());
    imageResource.setCreatedAt(WebUtils.parse8601(image.getCreationTimestamp()));
    return imageResource;
  }
}
