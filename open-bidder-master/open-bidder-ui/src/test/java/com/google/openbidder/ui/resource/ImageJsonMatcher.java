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

import com.google.api.services.compute.model.Image;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.openbidder.ui.resource.support.ResourceId;
import com.google.openbidder.ui.resource.support.ResourceType;

import net.minidev.json.JSONObject;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.joda.time.Instant;

/**
 * Verifies JSON output matches the underlying {@link Image}.
 */
public class ImageJsonMatcher extends BaseMatcher<Image> {

  private final long projectId;
  private final Image image;

  public ImageJsonMatcher(long projectId, Image image) {
    this.projectId = projectId;
    this.image = Preconditions.checkNotNull(image);
  }

  @Override
  public boolean matches(Object other) {
    if (!(other instanceof JSONObject)) {
      return false;
    }
    JSONObject object = (JSONObject) other;
    String resourceName = image.getName();
    ResourceId id = ResourceType.DEFAULT_IMAGE.getResourceId(
        Long.toString(projectId), resourceName);
    return Objects.equal(id.getResourceUri(), object.get("id"))
        && Objects.equal(ResourceType.DEFAULT_IMAGE.getResourceType(), object.get("resourceType"))
        && Objects.equal(resourceName, object.get("resourceName"))
        && Objects.equal(image.getDescription(), object.get("description"))
        && Objects.equal(parseDate(image.getCreationTimestamp()), object.get("createdAt"));
  }

  @Override
  public void describeTo(Description description) {
    description.appendValue(image);
  }

  private static Long parseDate(String text) {
    return text == null ? null : Instant.parse(text).getMillis();
  }
}