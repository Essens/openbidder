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

package com.google.openbidder.ui.entity.support;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.codehaus.jackson.annotate.JsonValue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Access-level of the user to a {@link com.google.openbidder.ui.entity.Project}.
 */
public enum ProjectRole {
  OWNER("owner", "project.role.owner", Permission.OWNER, Permission.READ, Permission.WRITE),
  READ_WRITE("read_write", "project.role.readwrite", Permission.READ, Permission.WRITE),
  READ("read", "project.role.read", Permission.READ),
  NONE("none", "project.role.none");

  public static final List<ProjectRole> VALUES = ImmutableList.copyOf(Arrays.asList(values()));

  private static final Map<String, ProjectRole> LOOKUP = ImmutableMap.copyOf(
      Maps.uniqueIndex(VALUES, new Function<ProjectRole, String>() {
        @Override
        public String apply(ProjectRole projectRole) {
          return projectRole.getRoleType();
        }
      }));

  private final String roleType;
  private final String messageCode;
  private final ImmutableSet<Permission> permissions;

  private ProjectRole(String roleType, String messageCode, Permission... permissions) {
    this.roleType = Preconditions.checkNotNull(roleType);
    this.messageCode = Preconditions.checkNotNull(messageCode);
    this.permissions = ImmutableSet.copyOf(
        Sets.newEnumSet(Arrays.asList(permissions), Permission.class));
  }

  @JsonValue
  public String getRoleType() {
    return roleType;
  }

  public String getMessageCode() {
    return messageCode;
  }

  public ImmutableSet<Permission> getPermissions() {
    return permissions;
  }

  /**
   * A synonym for {@link #name} that exists purely so JSTL can read it.
   */
  public String getName() {
    return name();
  }

  public boolean isOwner() {
    return permissions.contains(Permission.OWNER);
  }

  public boolean isRead() {
    return permissions.contains(Permission.READ);
  }

  public boolean isWrite() {
    return permissions.contains(Permission.WRITE);
  }

  @Nullable
  public static ProjectRole fromRoleType(String roleType) {
    return LOOKUP.get(roleType);
  }
}
