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

import com.google.common.base.Preconditions;
import com.google.openbidder.ui.cloudstorage.FakeStorageService;
import com.google.openbidder.ui.compute.FakeComputeClient;
import com.google.openbidder.ui.compute.FakeComputeService;
import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.entity.UserPreference;
import com.google.openbidder.ui.entity.support.ProjectRole;
import com.google.openbidder.ui.notify.Topic;
import com.google.openbidder.ui.resource.ProjectResourceService;
import com.google.openbidder.ui.resource.impl.FakeNotificationService;
import com.google.openbidder.ui.util.WebFunctionalTestCase;
import com.google.openbidder.util.testing.FakeClock;

import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.Objectify;

import org.joda.time.Instant;
import org.junit.After;
import org.junit.Before;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * {@link WebFunctionalTestCase} that adds logic specific to Open Bidder entities and services.
 */
public class OpenBidderFunctionalTestCase extends WebFunctionalTestCase {

  static final String API_PROJECT1 = "google.com:project1";
  static final String API_PROJECT2 = "google.com:project2";
  static final String NETWORK_NAME1 = "network-123987234223523";
  static final String NETWORK_NAME2 = "network-987897632400908";
  static final String FIREWALL_NAME1 = "firewall-123987234223523";
  static final String FIREWALL_NAME2 = "firewall-579872234011120";
  static final String PROJECT1 = "Test Project #1";
  static final String PROJECT2 = "Test Project #2";
  static final String MACHINE_TYPE1 = "standard-1-cpu";
  static final String MACHINE_TYPE2 = "standard-2-cpu";
  static final String DEFAULT_IMAGE1 = "centos-7-v20140926";
  static final String DEFAULT_IMAGE2 = "debian-7-wheezy-v20130522";
  static final String BIDDER1 = "bidder-324782342";
  static final String BIDDER2 = "bidder-324992342";
  static final String BALANCER1 = "balancer-336782342-rtb-us-east1";
  static final String EMAIL_OWNER_PROJECT_1_READ_PROJECT_2 = "user1@example.com";
  static final String EMAIL_OWNER_PROJECT_2 = "user2@example.com";
  static final String EMAIL_READ_WRITE_PROJECT_1 = "user3@example.com";
  static final String EMAIL_NO_PROJECTS = "user4@example.com";
  static final long NOW = 29873287242423324L;
  static final String REGION1 = "rtb-us-east1";
  static final String REGION2 = "rtb-us-east2";
  static final String ZONE1 = "rtb-us-east1-a";
  static final String ZONE2 = "rtb-us-east2-b";
  static final String USER_DIST_URI = "gs://PuppetModuleUri";

  private Map<String, Project> projects;
  Project project1;
  Project project2;
  UserPreference user1;
  UserPreference user2;
  UserPreference user3;
  String token;

  @Inject
  FakeComputeService computeService;

  @Inject
  FakeNotificationService notificationService;

  @Inject
  FakeStorageService storageService;

  @Inject
  ProjectResourceService projectResourceService;

  @Inject
  FakeClock clock;

  @Before
  public void initializeProjectsMapAndClock() {
    projects = new HashMap<>();
    clock.setNow(NOW);
  }

  @After
  public void resetMocks() {
    computeService.clear();
  }

  @Override
  protected void login(String email, boolean isAdmin) {
    super.login(email, isAdmin);
    token = null;
  }

  void setProject(String projectName) {
    long projectId = getProject(projectName).getId();
    if (token == null) {
      token = notificationService.createToken(projectId);
    }
    notificationService.setProject(token, projectId);
  }

  void authorizeComputeService(String projectName, String email) {
    Preconditions.checkNotNull(projectName);
    Preconditions.checkNotNull(email);
    Project project = projects.get(projectName);
    computeService.authorize(project.getId(), email);
  }

  FakeComputeClient getComputeClient(String projectName) {
    return getComputeClient(getProject(projectName));
  }

  FakeComputeClient getComputeClient(Project project) {
    return computeService.getOrCreateClient(project.getId(), project.getApiProjectId());
  }

  void standardFixtures() {
    clock.setNow(new Instant(NOW));
    project1 = createProject(PROJECT1, API_PROJECT1, NETWORK_NAME1);
    project2 = createProject(PROJECT2, API_PROJECT2);

    user1 = updatePreference(
        project1.getId(),
        EMAIL_OWNER_PROJECT_1_READ_PROJECT_2,
        ProjectRole.OWNER,
        /* default project */ true);
    user1 = updatePreference(
        project2.getId(),
        EMAIL_OWNER_PROJECT_1_READ_PROJECT_2,
        ProjectRole.READ,
        /* default project */ false);
    user2 = updatePreference(
        project2.getId(),
        EMAIL_OWNER_PROJECT_2,
        ProjectRole.OWNER,
        /* default project */ true);
    user3 = updatePreference(
        project1.getId(),
        EMAIL_READ_WRITE_PROJECT_1,
        ProjectRole.READ_WRITE,
        /* default project */ true);
  }

  Project createProject(String projectName, String apiProjectId) {
    return createProject(
        projectName,
        apiProjectId,
        /* network name */ null);
  }

  Project createProject(
      String projectName,
      String apiProjectId,
      @Nullable String networkName) {

    Project project = new Project();
    project.getProjectUuid(); // generate UUID
    project.setProjectName(projectName);
    project.setApiProjectId(apiProjectId);
    project.setApiProjectNumber(Long.valueOf(apiProjectId.hashCode()));
    project.setNetworkName(networkName);
    project.setUserDistUri(USER_DIST_URI);
    projectResourceService.addDefaultBidderConfig(project);
    Project createdProject = putAndGet(project);
    projects.put(createdProject.getProjectName(), createdProject);
    return createdProject;
  }

  Map<String, Project> getAllProjects() {
    return Collections.unmodifiableMap(projects);
  }

  Project getProject(String projectName) {
    Project project = projects.get(projectName);
    return Preconditions.checkNotNull(project);
  }

  UserPreference updatePreference(
      long projectId,
      String email,
      ProjectRole role,
      boolean isDefault) {

    Objectify ofy = factory.begin();
    UserPreference userPreference;
    try {
      userPreference = ofy.load().type(UserPreference.class).id(email).safe();
    } catch (NotFoundException e) {
      userPreference = new UserPreference(email);
    }
    userPreference.setProjectRole(projectId, role);
    if (isDefault) {
      userPreference.setDefaultProject(projectId);
    }
    return putAndGet(userPreference);
  }

  void verifyNotification(Topic topic, Object message) {
    Preconditions.checkNotNull(token, "Notification project not set");
    notificationService.verifyMessage(token, topic, message);
  }
}
