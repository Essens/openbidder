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

package com.google.openbidder.ui.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.openbidder.ui.entity.support.ProjectRole;
import com.google.openbidder.ui.entity.support.UserRole;
import com.google.openbidder.ui.user.exception.UserNotInProjectException;
import com.google.openbidder.ui.util.LocalAppEngineTestCase;

import com.googlecode.objectify.Key;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link UserPreference}.
 */
public class UserPreferenceTest extends LocalAppEngineTestCase {

  private static final String EMAIL = "foo@example.com";

  private Key<Project> project1;
  private Key<Project> project2;
  private Key<Project> project3;

  private UserPreference userPreference;

  @Before
  public void setUp() {
    project1 = Project.key(1123);
    project2 = Project.key(1153);
    project3 = Project.key(6123);

    userPreference = new UserPreference(EMAIL);
  }

  @Test
  public void constructor_empty_state() {
    assertEquals(EMAIL, userPreference.getEmail());
    assertNull(userPreference.getDefaultProject());
    assertTrue(userPreference.getUserRoles().isEmpty());
    assertUserRole(project1.getId(), ProjectRole.NONE);
  }

  @Test
  public void setProjectRole_emptyAddOneProject_setsDefault() {
    userPreference.setProjectRole(project1.getId(), ProjectRole.OWNER);

    assertEquals(project1, userPreference.getDefaultProject());
    assertTrue(userPreference.isProjectVisible(project1.getId()));
    assertTrue(userPreference.isDefaultProject(project1.getId()));
    assertUserRole(project1.getId(), ProjectRole.OWNER);
  }

  @Test
  public void setProjectRole_emptyAddTwoProject_firstIsDefault() {
    userPreference.setProjectRole(project1.getId(), ProjectRole.READ_WRITE);
    userPreference.setProjectRole(project2.getId(), ProjectRole.READ);

    assertEquals(project1, userPreference.getDefaultProject());
    assertTrue(userPreference.isProjectVisible(project1.getId()));
    assertTrue(userPreference.isDefaultProject(project1.getId()));
    assertUserRole(project1.getId(), ProjectRole.READ_WRITE);
  }

  @Test
  public void setProjectRole_defaultSetToNone_removesProjectClearsDefault() {
    userPreference.setProjectRole(project3.getId(), ProjectRole.READ);
    userPreference.setProjectRole(project3.getId(), ProjectRole.NONE);

    assertNull(userPreference.getDefaultProject());
    assertTrue(userPreference.getUserRoles().isEmpty());
    assertUserRole(project3.getId(), ProjectRole.NONE);
  }

  @Test
  public void removeFromProject_defaultProject_removesProjectClearsDefault() {
    userPreference.setProjectRole(project1.getId(), ProjectRole.READ_WRITE);
    userPreference.removeFromProject(project1.getId());

    assertNull(userPreference.getDefaultProject());
    assertTrue(userPreference.getUserRoles().isEmpty());
    assertUserRole(project1.getId(), ProjectRole.NONE);
  }

  @Test
  public void removeFromProject_nonDefaultProjectRemoved_defaultNotChanged() {
    userPreference.setProjectRole(project1.getId(), ProjectRole.READ_WRITE);
    userPreference.setProjectRole(project2.getId(), ProjectRole.OWNER);
    userPreference.removeFromProject(project2.getId());

    assertEquals(project1, userPreference.getDefaultProject());
    assertTrue(userPreference.isProjectVisible(project1.getId()));
    assertTrue(userPreference.isDefaultProject(project1.getId()));
    assertUserRole(project1.getId(), ProjectRole.READ_WRITE);
    assertUserRole(project2.getId(), ProjectRole.NONE);
  }

  @Test
  public void removeFromProject_defaultProjectRemoved_defaultChanged() {
    userPreference.setProjectRole(project1.getId(), ProjectRole.READ_WRITE);
    userPreference.setProjectRole(project2.getId(), ProjectRole.OWNER);
    userPreference.removeFromProject(project1.getId());

    assertEquals(project2, userPreference.getDefaultProject());
    assertTrue(userPreference.isProjectVisible(project2.getId()));
    assertTrue(userPreference.isDefaultProject(project2.getId()));
    assertUserRole(project1.getId(), ProjectRole.NONE);
    assertUserRole(project2.getId(), ProjectRole.OWNER);
  }

  @Test(expected = UserNotInProjectException.class)
  public void setDefaultProject_userNotInProject_throwsException() {
    userPreference.setProjectRole(project1.getId(), ProjectRole.READ_WRITE);

    userPreference.setDefaultProject(project2.getId());
  }

  @Test
  public void setDefaultProject_null_clearsDefaultProject() {
    userPreference.setProjectRole(project1.getId(), ProjectRole.READ_WRITE);
    userPreference.setDefaultProject(null);

    assertNull(userPreference.getDefaultProject());
  }

  @Test
  public void setDefaultProject_twoProjects_changesDefault() {
    userPreference.setProjectRole(project1.getId(), ProjectRole.READ_WRITE);
    userPreference.setProjectRole(project2.getId(), ProjectRole.OWNER);
    userPreference.setDefaultProject(project2);

    assertEquals(project2, userPreference.getDefaultProject());
  }

  private void assertUserRole(long projectId, ProjectRole role) {
    UserRole userRole = userPreference.getUserRole(projectId);
    assertEquals(projectId, userRole.getProjectId());
    assertEquals(projectId, userRole.getProject().getId());
    assertEquals(role, userRole.getProjectRole());
    assertEquals(role, userPreference.getProjectRole(projectId));
  }
}
