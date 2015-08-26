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

package com.google.openbidder.ui.resource.exception;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.openbidder.ui.resource.support.ResourceMethod;
import com.google.openbidder.ui.resource.support.ResourcePath;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Set;

/**
 * Unsupported HTTP method.
 */
@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
public class ResourceMethodNotAllowedException extends RuntimeException {

  private static final String MESSAGE = "%s %s not supported (%s)";
  private static final Function<ResourceMethod, String> TO_NAME =
      new Function<ResourceMethod, String>() {
        @Override
        public String apply(ResourceMethod resourceMethod1) {
          return resourceMethod1.name();
        }
      };

  private final ResourcePath resourcePath;
  private final ResourceMethod resourceMethod;
  private final ImmutableSet<ResourceMethod> supportedMethods;

  public ResourceMethodNotAllowedException(
      ResourcePath resourcePath,
      ResourceMethod resourceMethod,
      Set<ResourceMethod> supportedMethods) {

    super(String.format(MESSAGE,
        Preconditions.checkNotNull(resourceMethod, "resourceMethod is null"),
        Preconditions.checkNotNull(resourcePath, "resourceId is null"),
        Joiner.on(", ").join(Iterables.transform(
            Preconditions.checkNotNull(supportedMethods, "supportedMethods is null"), TO_NAME))));
    this.resourcePath = resourcePath;
    this.resourceMethod = resourceMethod;
    this.supportedMethods = ImmutableSet.copyOf(supportedMethods);
  }

  public ResourcePath getResourcePath() {
    return resourcePath;
  }

  public ResourceMethod getResourceMethod() {
    return resourceMethod;
  }

  public ImmutableSet<ResourceMethod> getSupportedMethods() {
    return supportedMethods;
  }
}
