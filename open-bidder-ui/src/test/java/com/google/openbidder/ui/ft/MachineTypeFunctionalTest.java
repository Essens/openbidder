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

import static com.google.openbidder.ui.resource.ResourceMatchers.machineType;
import static com.google.openbidder.ui.resource.ResourceMatchers.machineTypes;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.google.api.services.compute.model.MachineType;
import com.google.api.services.compute.model.Zone;
import com.google.openbidder.ui.compute.ComputeResourceType;
import com.google.openbidder.ui.compute.ResourceName;
import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.resource.support.ResourceType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Collection;

/**
 * Machine type resource tests.
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
public class MachineTypeFunctionalTest extends OpenBidderFunctionalTestCase {

  @Test
  public void get_noAccess_notFound() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    expectNotFound(get(machineTypeIdUri(PROJECT1, ZONE1, "standard-1-cpu")));
  }

  @Test
  public void get_noCredentials_unauthorized() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    expectUnauthorized(get(machineTypeIdUri(PROJECT2, ZONE1, MACHINE_TYPE1)));
  }

  @Test
  public void get_notFound_notFound() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    authorizeComputeService(PROJECT2, EMAIL_OWNER_PROJECT_2);
    expectNotFound(get(machineTypeIdUri(PROJECT2, ZONE1, MACHINE_TYPE1)));
  }

  @Test
  public void get_found_ok() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    authorizeComputeService(PROJECT2, EMAIL_OWNER_PROJECT_2);
    Zone zone = buildZone(API_PROJECT2, ZONE1);
    MachineType machineType = addMachineType(PROJECT2, ZONE1, MACHINE_TYPE1, 4);
    expectJson(get(machineTypeIdUri(PROJECT2, ZONE1, MACHINE_TYPE1)),
        jsonPath("$", machineType(project2.getId(), zone, machineType)));
  }

  @Test
  public void list_noAccess_notFound() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    expectNotFound(get(machineTypeCollectionUri(PROJECT1, ZONE1)));
  }

  @Test
  public void list_noCredentials_unauthorized() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    expectUnauthorized(get(machineTypeCollectionUri(PROJECT2, ZONE1)));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  public void list_found_ok() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    authorizeComputeService(PROJECT2, EMAIL_OWNER_PROJECT_2);
    Zone zone = buildZone(API_PROJECT2, ZONE1);
    MachineType machineType1 = buildMachineType(API_PROJECT2, ZONE1, MACHINE_TYPE1, 5);
    MachineType machineType2 = buildMachineType(API_PROJECT2, ZONE1, MACHINE_TYPE2, 7);
    addMachineTypes(PROJECT2, machineType1, machineType2);
    expectJson(get(machineTypeCollectionUri(PROJECT2, ZONE1)),
        jsonPath("$").isArray(),
        jsonPath("$", hasSize(2)),
        jsonPath("$", containsInAnyOrder(
            (Collection) machineTypes(project2.getId(), zone, machineType1, machineType2))));
  }

  @Test
  public void postJson_loggedIn_methodNotAllowed() {
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectMethodNotAllowed(postObjectJson(machineTypeCollectionUri(1234, ZONE1), emptyRequest()));
  }

  @Test
  public void put_loggedIn_methodNotAllowed() {
    login(EMAIL_OWNER_PROJECT_2);
    expectMethodNotAllowed(putObjectJson(machineTypeIdUri(1234, ZONE1, MACHINE_TYPE1), emptyRequest()));
  }

  @Test
  public void delete_loggedIn_methodNotAllowed() {
    login(EMAIL_OWNER_PROJECT_2);
    expectMethodNotAllowed(delete(machineTypeIdUri(1234, ZONE1, MACHINE_TYPE1)));
  }

  private String machineTypeIdUri(String projectName, String zoneName, String machineTypeName) {
    return machineTypeIdUri(getProject(projectName).getId(), zoneName, machineTypeName);
  }

  private String machineTypeIdUri(long projectId, String zoneName, String machineTypeName) {
    return ResourceType.MACHINE_TYPE
        .getResourceId(Long.toString(projectId), zoneName, machineTypeName)
        .getResourceUri();
  }

  private String machineTypeCollectionUri(String projectName, String zoneName) {
    return machineTypeCollectionUri(getProject(projectName).getId(), zoneName);
  }

  private String machineTypeCollectionUri(long projectId, String zoneName) {
    return ResourceType.MACHINE_TYPE
        .getResourceCollectionId(Long.toString(projectId), zoneName)
        .getResourceUri();
  }

  private Zone buildZone(String apiProjectId, String name) {
    Zone zone = new Zone();
    zone.setName(name);
    zone.setDescription(name);
    zone.setSelfLink(ComputeResourceType.ZONE.buildName(apiProjectId, name).getResourceUrl());
    return zone;
  }

  private MachineType buildMachineType(
      String apiProjectId,
      String zoneName,
      String machineTypeName,
      int cpus) {
    MachineType machineType = new MachineType();
    machineType.setName(machineTypeName);
    machineType.setZone(zoneName);
    machineType.setDescription("Machine with " + cpus + " CPUs");
    ResourceName resourceName = ComputeResourceType.MACHINE_TYPE.buildName(
        apiProjectId, zoneName, machineTypeName);
    machineType.setSelfLink(resourceName.getResourceUrl());
    machineType.setGuestCpus(cpus);
    machineType.setMemoryMb(cpus * 1024);
    machineType.setImageSpaceGb(cpus * 10);
    machineType.setScratchDisks(asList(new MachineType.ScratchDisks[1000]));
    return machineType;
  }

  private MachineType addMachineType(
      String projectName,
      String zoneName,
      String machineTypeName,
      int cpus) {
    Project project = getProject(projectName);
    MachineType machineType = buildMachineType(
        project.getApiProjectId(), zoneName, machineTypeName, cpus);
    getComputeClient(project).addMachineType(machineType);
    return machineType;
  }

  private void addMachineTypes(String projectName, MachineType... machineTypes) {
    getComputeClient(projectName).addAllMachineTypes(machineTypes);
  }
}
