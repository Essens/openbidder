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

package com.google.openbidder.ui.resource.support;

/**
 * This represents an identifiable resource or set of related resources belonging to the
 * same {@link ResourceCollectionId}.
 */
public interface ResourcePath {

  /**
   * @return {@link com.google.openbidder.ui.resource.support.ResourceType}
   * of this resource or set of resources.
   */
  ResourceType getResourceType();

  /**
   * @return Identifiable URI for this resource of set of resources.
   */
  String getResourceUri();
}
