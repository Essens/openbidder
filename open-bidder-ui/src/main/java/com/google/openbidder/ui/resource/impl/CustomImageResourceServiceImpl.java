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
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.openbidder.ui.compute.ComputeClient;
import com.google.openbidder.ui.compute.ComputeService;
import com.google.openbidder.ui.project.ProjectService;
import com.google.openbidder.ui.project.ProjectUser;
import com.google.openbidder.ui.resource.CustomImageResourceService;
import com.google.openbidder.ui.resource.model.ImageResource;
import com.google.openbidder.ui.resource.support.ResourceCollectionId;
import com.google.openbidder.ui.resource.support.ResourceId;
import com.google.openbidder.ui.resource.support.ResourceMethod;
import com.google.openbidder.ui.resource.support.ResourceType;

import java.util.EnumSet;
import java.util.List;

import javax.inject.Inject;

/**
 * A {@link com.google.openbidder.ui.resource.ResourceService} for custom {ImageResource}s.
 */
public class CustomImageResourceServiceImpl
    extends AbstractImageResourceServiceImpl
    implements CustomImageResourceService {

  @Inject
  public CustomImageResourceServiceImpl(
      ProjectService projectService,
      ComputeService computeService) {
    super(ResourceType.CUSTOM_IMAGE,
        EnumSet.of(ResourceMethod.GET, ResourceMethod.LIST),
        projectService,
        computeService);
  }

  @Override
  protected ImageResource get(
      ComputeClient computeClient,
      ProjectUser projectUser,
      ResourceId resourceId,
      Multimap<String, String> params) {
    Image image = computeClient.getCustomImage((resourceId.getResourceName()));
    return build(resourceId.getParent(), image);
  }

  @Override
  protected List<ImageResource> list(
      ComputeClient computeClient,
      ProjectUser projectUser,
      final ResourceCollectionId resourceCollectionId,
      Multimap<String, String> params) {
    return Lists.transform(
        ImmutableList.copyOf(computeClient.listCustomImages()),
        new Function<Image, ImageResource>() {
          @Override public ImageResource apply(Image image) {
            return build(resourceCollectionId, image);
          }});
  }
}
