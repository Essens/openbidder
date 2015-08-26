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

package com.google.openbidder.ui.util.validation;

import com.google.openbidder.ui.resource.support.ResourcePath;
import com.google.openbidder.ui.resource.support.ResourceType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validate if the {@link ResourcePath} is of the specified {@link ResourceType}.
 */
public class ResourceTypeValidator implements ConstraintValidator<ResourcePathType, ResourcePath> {
  private ResourceType resourceType;

  @Override
  public void initialize(ResourcePathType resourcePathType) {
    resourceType = resourcePathType.type();
  }

  @Override
  public boolean isValid(ResourcePath resourcePath, ConstraintValidatorContext context) {
    return resourcePath == null || resourcePath.getResourceType() == resourceType;
  }
}
