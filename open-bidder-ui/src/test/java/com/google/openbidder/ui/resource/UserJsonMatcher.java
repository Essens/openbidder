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

package com.google.openbidder.ui.resource;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;
import com.google.openbidder.ui.entity.support.ProjectRole;
import com.google.openbidder.ui.resource.model.UserResource;
import com.google.openbidder.ui.resource.support.ResourceId;
import com.google.openbidder.ui.resource.support.ResourceType;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.util.Map;

/**
 * Verifies JSON output matches the underlying
 * {@link com.google.openbidder.ui.resource.model.UserResource}.
 */
public class UserJsonMatcher extends BaseMatcher<UserResource> {

  private final long projectId;
  private final String email;
  private final ProjectRole projectRole;

  public UserJsonMatcher(long projectId, String email, ProjectRole projectRole) {
    this.projectId = projectId;
    this.email = checkNotNull(email);
    this.projectRole = checkNotNull(projectRole);
  }

  @Override
  public boolean matches(Object other) {
    if (!(other instanceof Map<?, ?>)) {
      return false;
    }
    Map<?, ?> object = (Map<?, ?>) other;
    ResourceId id = ResourceType.USER.getResourceId(Long.toString(projectId), email);
    return Objects.equal(id.getResourceUri(), object.get("id"))
        && Objects.equal(ResourceType.USER.getResourceType(), object.get("resourceType"))
        && Objects.equal(email, object.get("resourceName"))
        && Objects.equal(email, object.get("userEmail"))
        && Objects.equal(projectRole.getRoleType(), object.get("projectRole"));
  }

  @Override
  public void describeTo(Description description) {
    description.appendValue(projectId).appendValue(email).appendValue(projectRole);
  }
}
