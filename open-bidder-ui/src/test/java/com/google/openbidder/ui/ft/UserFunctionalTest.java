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

package com.google.openbidder.ui.ft;

import static com.google.openbidder.ui.resource.ResourceMatchers.user;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.google.openbidder.ui.entity.UserPreference;
import com.google.openbidder.ui.entity.support.ProjectRole;
import com.google.openbidder.ui.resource.model.UserResource;
import com.google.openbidder.ui.resource.support.ResourceType;

import com.googlecode.objectify.NotFoundException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Collection;

/**
 * Tests for {@link com.google.openbidder.ui.controller.UserController}.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
    locations = {
        "file:src/main/webapp/WEB-INF/applicationContext.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-security.xml",
        "file:src/main/webapp/WEB-INF/ui-servlet.xml",
        "classpath:/bean-overrides.xml"
    })
@WebAppConfiguration
public class UserFunctionalTest extends OpenBidderFunctionalTestCase {

  @Test
  public void get_projectDoesNotExist_notFound() {
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectNotFound(get(userIdUri(1234, EMAIL_NO_PROJECTS)));
  }

  @Test
  public void get_userDoesNotExist_notFound() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectNotFound(get(userIdUri(PROJECT1, EMAIL_OWNER_PROJECT_2)));
  }

  @Test
  public void get_userExists_ok() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectJson(get(userIdUri(PROJECT1, EMAIL_OWNER_PROJECT_1_READ_PROJECT_2)),
        jsonPath("$", user(project1.getId(),
            EMAIL_OWNER_PROJECT_1_READ_PROJECT_2,
            ProjectRole.OWNER)));
  }

  @Test
  public void list_projectDoesNotExist_notFound() {
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectNotFound(get(userCollectionUri(1234)));
  }

  @Test
  public void list_noAccessToProject_notFound() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    expectNotFound(get(userCollectionUri(PROJECT1)));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  public void list_twoUsers_ok() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectJson(get(userCollectionUri(PROJECT2)),
        jsonPath("$").isArray(),
        jsonPath("$", hasSize(2)),
        jsonPath("$", containsInAnyOrder((Collection) asList(
            user(project2.getId(), EMAIL_OWNER_PROJECT_1_READ_PROJECT_2, ProjectRole.READ),
            user(project2.getId(), EMAIL_OWNER_PROJECT_2, ProjectRole.OWNER)
        ))));
  }

  @Test
  public void postJson_noAccess_notFound() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    expectNotFound(postUser(PROJECT1, EMAIL_NO_PROJECTS, ProjectRole.READ));
  }

  @Test
  public void postJson_readAccess_forbidden() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectForbidden(postUser(PROJECT2, EMAIL_NO_PROJECTS, ProjectRole.READ));
  }

  @Test
  public void postJson_writeAccess_forbidden() {
    standardFixtures();
    login(EMAIL_READ_WRITE_PROJECT_1);
    expectForbidden(postUser(PROJECT1, EMAIL_NO_PROJECTS, ProjectRole.READ_WRITE));
  }

  @Test
  public void postJson_newUserToProject_ok() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    testCreateUser(PROJECT2, EMAIL_NO_PROJECTS, ProjectRole.READ_WRITE);
  }

  @Test
  public void postJson_updateUserInProject_ok() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    testCreateUser(PROJECT2, EMAIL_OWNER_PROJECT_1_READ_PROJECT_2, ProjectRole.OWNER);
  }

  @Test
  public void postJson_changeCurrentUser_conflict() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    expectConflict(postUser(PROJECT2, EMAIL_OWNER_PROJECT_2, ProjectRole.READ_WRITE));
  }

  @Test
  public void put_noAccess_notFound() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    expectNotFound(putUser(PROJECT1, EMAIL_NO_PROJECTS, ProjectRole.READ));
  }

  @Test
  public void put_readAccess_forbidden() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectForbidden(putUser(PROJECT2, EMAIL_NO_PROJECTS, ProjectRole.READ));
  }

  @Test
  public void put_writeAccess_forbidden() {
    standardFixtures();
    login(EMAIL_READ_WRITE_PROJECT_1);
    expectForbidden(putUser(PROJECT1, EMAIL_NO_PROJECTS, ProjectRole.READ_WRITE));
  }

  @Test
  public void put_newUserToProject_ok() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    testUpdateUser(PROJECT2, EMAIL_NO_PROJECTS, ProjectRole.READ_WRITE);
  }

  @Test
  public void put_updateUserInProject_ok() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    testUpdateUser(PROJECT2, EMAIL_OWNER_PROJECT_1_READ_PROJECT_2, ProjectRole.OWNER);
  }

  @Test
  public void put_changeCurrentUser_conflict() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    expectConflict(putUser(PROJECT2, EMAIL_OWNER_PROJECT_2, ProjectRole.READ_WRITE));
  }

  @Test
  public void delete_noAccess_notFound() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    expectNotFound(delete(userIdUri(PROJECT1, EMAIL_OWNER_PROJECT_2)));
  }

  @Test
  public void delete_readAccess_forbidden() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectForbidden(delete(userIdUri(PROJECT2, EMAIL_OWNER_PROJECT_2)));
  }

  @Test
  public void delete_writeAccess_forbidden() {
    standardFixtures();
    login(EMAIL_READ_WRITE_PROJECT_1);
    expectForbidden(delete(userIdUri(PROJECT1, EMAIL_OWNER_PROJECT_2)));
  }

  @Test
  public void delete_userNotFound_ok() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    testDeleteUser(PROJECT2, EMAIL_NO_PROJECTS);
  }

  @Test
  public void delete_userFound_ok() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    testDeleteUser(PROJECT2, EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
  }

  @Test
  public void delete_self_conflict() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    expectConflict(delete(userIdUri(PROJECT2, EMAIL_OWNER_PROJECT_2)));
  }

  private String userIdUri(String projectName, String email) {
    return userIdUri(getProject(projectName).getId(), email);
  }

  private String userIdUri(long projectId, String email) {
    return ResourceType.USER
        .getResourceId(Long.toString(projectId), email)
        .getResourceUri();
  }

  private String userCollectionUri(String projectName) {
    return userCollectionUri(getProject(projectName).getId());
  }

  private String userCollectionUri(long projectId) {
    return ResourceType.USER
        .getResourceCollectionId(Long.toString(projectId))
        .getResourceUri();
  }

  private MockHttpServletRequestBuilder postUser(String projectName, String email,
      ProjectRole projectRole) {
    return postUser(getProject(projectName).getId(), email, projectRole);
  }

  private MockHttpServletRequestBuilder postUser(long projectId, String userEmail,
      ProjectRole role) {
    UserResource userResource = new UserResource();
    userResource.setUserEmail(userEmail);
    userResource.setProjectRole(role);
    return postObjectJson(userCollectionUri(projectId), userResource);
  }

  private MockHttpServletRequestBuilder putUser(String projectName, String email,
      ProjectRole projectRole) {
    return putUser(getProject(projectName).getId(), email, projectRole);
  }

  private MockHttpServletRequestBuilder putUser(long projectId, String userEmail,
      ProjectRole role) {
    UserResource userResource = new UserResource();
    userResource.setProjectRole(role);
    return putObjectJson(userIdUri(projectId, userEmail), userResource);
  }

  // TODO(opinali): this test is flaky, investigate
  private void testCreateUser(String projectName, String email, ProjectRole projectRole) {
    long projectId = getProject(projectName).getId();
    expectJson(postUser(projectId, email, projectRole),
        jsonPath("$", user(projectId, email, projectRole)));
    UserPreference userPreference = getEntity(UserPreference.key(email));
    assertEquals(projectRole, userPreference.getProjectRole(projectId));
  }

  private void testUpdateUser(String projectName, String email, ProjectRole projectRole) {
    long projectId = getProject(projectName).getId();
    expectJson(putUser(projectId, email, projectRole),
        jsonPath("$", user(projectId, email, projectRole)));
    UserPreference userPreference = getEntity(UserPreference.key(email));
    assertEquals(projectRole, userPreference.getProjectRole(projectId));
  }

  private void testDeleteUser(String projectName, String email) {
    long projectId = getProject(projectName).getId();
    expectOk(delete(userIdUri(projectId, email)));
    try {
      UserPreference userPreference = getEntity(UserPreference.key(email));
      assertFalse(userPreference.isProjectVisible(projectId));
    } catch (NotFoundException e) {
      // pass
    }
  }
}
