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

package com.google.openbidder.ui.resource;

import com.google.common.collect.Multimap;
import com.google.openbidder.ui.resource.support.ExternalResource;
import com.google.openbidder.ui.resource.support.ResourceMethod;

import java.util.List;

/**
 * Service for performing REST operations on {@link ExternalResource}s that are
 * per-zone resources</a>.
 */
public interface GrandChildResourceService<T extends ExternalResource> {

  /**
   * @return {@code true} if this service supports the {@code resourceMethod},
   * otherwise {@code false}.
   */
  boolean supports(ResourceMethod resourceMethod);

  /**
   * For example, a URI of "/projects/234/zones/rtb1/instances/bidder-123"
   * has a {@code parentResourceName} of "234" and a {@code childResourceName} of "rtb1"
   * a {@code grandChildResourceName} of "bidder-123".
   *
   * @return A grandchild resource identified by the unique name of its parent resource,
   * child resource and itself
   */
  T get(
      String parentResourceName,
      String childResourceName,
      String grandChildResourceName,
      Multimap<String, String> params);

  /**
   * @return {@link java.util.Collection} of all grandchild resources belonging to the
   * parent resource and the child resource.
   */
  List<? extends T> list(
      String parentResourceName,
      String childResourceName,
      Multimap<String, String> params);

  /**
   * @return A newly created resource based on the {@code newResource} passed in. Note: the
   * service is free to modify the resource as desired.
   */
  T create(String parentResourceName, String childResourceName, T newResource);

  /**
   * Delete a specific resource identified by unique name.
   */
  void delete(String parentResourceName, String childResourceName, String grandChildResourceName);
}
