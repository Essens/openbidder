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

import static com.google.openbidder.ui.resource.ResourceMatchers.instance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.google.api.services.compute.model.AccessConfig;
import com.google.api.services.compute.model.Instance;
import com.google.api.services.compute.model.Metadata;
import com.google.api.services.compute.model.NetworkInterface;
import com.google.api.services.compute.model.Tags;
import com.google.api.services.compute.model.Zone;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.openbidder.ui.compute.BidderInstanceBuilder;
import com.google.openbidder.ui.compute.ComputeResourceType;
import com.google.openbidder.ui.compute.InstanceBuilder;
import com.google.openbidder.ui.compute.LoadBalancerInstanceBuilder;
import com.google.openbidder.ui.compute.ResourceName;
import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.notify.Topic;
import com.google.openbidder.ui.resource.model.BalancerResource;
import com.google.openbidder.ui.resource.model.BidderResource;
import com.google.openbidder.ui.resource.model.InstanceResource;
import com.google.openbidder.ui.resource.support.InstanceStatus;
import com.google.openbidder.ui.resource.support.InstanceType;
import com.google.openbidder.ui.resource.support.ResourceCollectionId;
import com.google.openbidder.ui.resource.support.ResourceType;
import com.google.openbidder.ui.util.WebContextLoader;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Instance resource tests.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
    locations = {
        "file:src/main/webapp/WEB-INF/applicationContext.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-security.xml",
        "file:src/main/webapp/WEB-INF/ui-servlet.xml",
        "classpath:/bean-overrides.xml"
    },
    loader = WebContextLoader.class)
public class InstanceFunctionalTest extends OpenBidderFunctionalTestCase {

  private static final Map<String, String> PARAMS_1 = ImmutableMap.<String, String>builder()
      .put("status", "RUNNING")
      .put("zone", "rtb-us-east1")
      .put("machineType", "standard-1-cpu")
      .put("image", "debian-7-wheezy-v20130522")
      .put("internalIp", "10.0.0.1")
      .put("externalIp", "206.43.112.1")
      .build();

  private static final Map<String, String> PARAMS_2 = ImmutableMap.<String, String>builder()
      .put("status", "PROVISIONING")
      .put("zone", "rtb-us-east2")
      .put("machineType", "standard-2-cpu")
      .put("image", "centos-7-v20140926")
      .put("internalIp", "10.0.0.2")
      .put("externalIp", "206.43.112.2")
      .build();

  private static final Function<Map.Entry<String, String>, Metadata.Items> METADATA_ITEM =
      new Function<Map.Entry<String, String>, Metadata.Items>() {
        @Override
        public Metadata.Items apply(Map.Entry<String, String> entry) {
          Metadata.Items item = new Metadata.Items();
          item.setKey(entry.getKey());
          item.setValue(entry.getValue());
          return item;
        }
      };

  @Test
  public void get_unknownProject_notFound() {
    login(EMAIL_NO_PROJECTS);
    expectNotFound(get(instanceIdUri(1234, ZONE1, "bidder-1234")));
  }

  @Test
  public void get_noAccess_notFound() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    expectNotFound(get(instanceIdUri(PROJECT1, ZONE1, "balancer-2345")));
  }

  @Test
  public void get_noCredentials_unauthorized() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    expectUnauthorized(get(instanceIdUri(PROJECT2, ZONE2, "instance-123")));
  }

  @Test
  public void get_notFound_notFound() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    authorizeComputeService(PROJECT2, EMAIL_OWNER_PROJECT_2);
    expectNotFound(get(instanceIdUri(PROJECT2, ZONE2, BIDDER1)));
  }

  @Test
  public void get_bidder_ok() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    authorizeComputeService(PROJECT1, EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    Instance instance = buildBidder(
        PROJECT1,
        EMAIL_OWNER_PROJECT_1_READ_PROJECT_2,
        BIDDER1,
        PARAMS_1);
    Zone zone = buildZone(API_PROJECT1, ZONE1);

    addInstance(PROJECT1, instance);
    expectJson(get(instanceIdUri(PROJECT1, ZONE1, BIDDER1)),
        jsonPath("$", instance(project1, zone, instance)));
  }

  @Test
  public void get_balancer_ok() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    authorizeComputeService(PROJECT1, EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    Instance instance = buildBalancer(
        PROJECT1,
        EMAIL_OWNER_PROJECT_1_READ_PROJECT_2,
        BALANCER1,
        PARAMS_1);
    Zone zone = buildZone(API_PROJECT1, ZONE1);
    addInstance(PROJECT1, instance);
    expectJson(get(instanceIdUri(PROJECT1, ZONE1, BALANCER1)),
        jsonPath("$", instance(project1, zone,  instance)));
  }

  @Test
  public void postJson_missingFields_badRequest() {
    login(EMAIL_NO_PROJECTS);
    expectBadRequest(postObjectJson(instanceCollectionUri(1234, ZONE1), emptyRequest()));
  }

  @Test
  public void postJson_unknownProject_notFound() {
    login(EMAIL_NO_PROJECTS);
    standardFixtures();
    expectNotFound(postObjectJson(
        instanceCollectionUri(1234, ZONE1), minimalRequest(PROJECT1, "rtb1")));
  }

  @Test
  public void postJson_noCredentials_unauthorized() {
    standardFixtures();
    login(EMAIL_READ_WRITE_PROJECT_1);
    expectUnauthorized(
        postObjectJson(instanceCollectionUri(PROJECT1, ZONE1), minimalRequest(PROJECT1, "rtb1")));
  }

  @Test
  public void postJson_readAccess_forbidden() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    authorizeComputeService(PROJECT2, EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectForbidden(
        postObjectJson(instanceCollectionUri(PROJECT2, ZONE1), minimalRequest(PROJECT1, "rtb1")));
  }

  @Test
  public void postJson_noInstanceType_badRequest() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    authorizeComputeService(PROJECT2, EMAIL_OWNER_PROJECT_2);
    expectBadRequest(postObjectJson(instanceCollectionUri(PROJECT2, ZONE2), emptyRequest()));
  }

  @Test
  public void postJson_noNetwork_badRequest() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    authorizeComputeService(PROJECT2, EMAIL_OWNER_PROJECT_2);
    InstanceResource request = new InstanceResource();
    request.setInstanceType(InstanceType.BIDDER);
    expectBadRequest(postObjectJson(instanceCollectionUri(PROJECT2, ZONE2), request));
  }

  @Test
  public void postJson_noZone_badRequest() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    authorizeComputeService(PROJECT1, EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    InstanceResource request = new InstanceResource();
    request.setInstanceType(InstanceType.BALANCER);
    expectBadRequest(postObjectJson(instanceCollectionUri(PROJECT1, ZONE1), request));
  }

  @Test
  public void postJson_badZone_badRequest() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    authorizeComputeService(PROJECT1, EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    InstanceResource request = new InstanceResource();
    request.setInstanceType(InstanceType.BALANCER);
    request.setZone(ResourceType.ZONE.getResourceId(Long.toString(project2.getId()), "foo"));
    request.setImage(
        ResourceType.DEFAULT_IMAGE.getResourceId(Long.toString(project1.getId()), DEFAULT_IMAGE1));
    expectBadRequest(postObjectJson(instanceCollectionUri(PROJECT1, ZONE1), request));
  }

  @Test
  public void postJson_noMachineTypeOnRequestOrProject_badRequest() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    authorizeComputeService(PROJECT1, EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    InstanceResource request = new InstanceResource();
    request.setInstanceType(InstanceType.BIDDER);
    request.setImage(
        ResourceType.DEFAULT_IMAGE.getResourceId(Long.toString(project1.getId()), DEFAULT_IMAGE1));
    request.setZone(ResourceType.ZONE.getResourceId(Long.toString(project1.getId()), "foo"));
    expectBadRequest(postObjectJson(instanceCollectionUri(PROJECT1, ZONE1), request));
  }

  @Test
  public void postJson_machineTypeOnRequest_okSetsBidderMachineType() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    setProject(PROJECT1);
    authorizeComputeService(PROJECT1, EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);

    InstanceResource request = new InstanceResource();
    request.setInstanceType(InstanceType.BIDDER);
    request.setZone(ResourceType.ZONE.getResourceId(Long.toString(project1.getId()), ZONE1));
    request.setMachineType(ResourceType.MACHINE_TYPE.getResourceId(
        Long.toString(project1.getId()), ZONE1, MACHINE_TYPE1));
    request.setImage(
        ResourceType.DEFAULT_IMAGE.getResourceId(Long.toString(project1.getId()), DEFAULT_IMAGE1));
    BidderResource actualBiddder = expectJson(postObjectJson(
        instanceCollectionUri(PROJECT1, ZONE1), request),
        BidderResource.class);

    Instance runningInstance = getComputeClient(PROJECT1).getInstanceDirect(
        actualBiddder.getId().getResourceName());
    Instance startingInstance = cloneInstance(runningInstance);
    startingInstance.setStatus(InstanceStatus.STARTING.toString());
    startingInstance.setCreationTimestamp(null);
    runningInstance.setStatus(InstanceStatus.RUNNING.toString());
    verifyNotification(PROJECT1, ZONE1, startingInstance);
    verifyNotification(PROJECT1, ZONE1, runningInstance);
    Project savedProject = getEntity(project1.getKey());
    assertEquals(MACHINE_TYPE1, savedProject.getBidderMachineType(ZONE1));
  }

  @Test
  public void postJson_machineTypeOnProject_ok() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    setProject(PROJECT1);
    authorizeComputeService(PROJECT1, EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);

    project1.setLoadBalancerMachineType(ZONE1, "standard-4-cpu");
    project1.setLoadBalancerImage(DEFAULT_IMAGE1);
    project1 = putAndGet(project1);

    InstanceResource request = new InstanceResource();
    request.setInstanceType(InstanceType.BALANCER);
    request.setImage(
        ResourceType.DEFAULT_IMAGE.getResourceId(Long.toString(project1.getId()), DEFAULT_IMAGE1));
    request.setZone(ResourceType.ZONE.getResourceId(Long.toString(project1.getId()), ZONE1));
    BalancerResource actualBalancerResource = expectJson(postObjectJson(
        instanceCollectionUri(PROJECT1, ZONE1), request), BalancerResource.class);

    Instance instance = getComputeClient(PROJECT1).getInstanceDirect(
        actualBalancerResource.getId().getResourceName());
    Instance startingInstance = cloneInstance(instance);
    startingInstance.setCreationTimestamp(null);
    startingInstance.setStatus(InstanceStatus.STARTING.toString());
    Instance runningInstance = cloneInstance(instance);
    runningInstance.setStatus(InstanceStatus.RUNNING.toString());

    verifyNotification(PROJECT1, ZONE1, startingInstance);
    verifyNotification(PROJECT1, ZONE1, runningInstance);
    ResourceName machineTypeName = ResourceName.parseResource(runningInstance.getMachineType());
    assertEquals("standard-4-cpu", machineTypeName.getResourceName());
  }

  @Test
  public void put_loggedIn_methodNotAllowed() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectMethodNotAllowed(
        putObjectJson(instanceIdUri(PROJECT1, ZONE1, "bidder-1234"), emptyRequest()));
  }

  @Test
  public void delete_unknownProject_notFound() {
    login(EMAIL_NO_PROJECTS);
    expectNotFound(delete(instanceIdUri(1234, ZONE1, "instance-1234")));
  }

  @Test
  public void delete_readAccess_forbidden() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    authorizeComputeService(PROJECT2, EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectForbidden(delete(instanceIdUri(PROJECT2, ZONE2, "balancer-23456")));
  }

  @Test
  public void delete_noNetworkOnProject_notFound() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    authorizeComputeService(PROJECT2, EMAIL_OWNER_PROJECT_2);
    expectNotFound(delete(instanceIdUri(PROJECT2, ZONE2, "balancer-23456")));
  }

  @Test
  public void delete_noCredentials_unauthorized() {
    standardFixtures();
    login(EMAIL_READ_WRITE_PROJECT_1);
    expectUnauthorized(delete(instanceIdUri(PROJECT1, ZONE1, "balancer-23456")));
  }

  @Test
  public void delete_instanceNotFound_ok() {
    standardFixtures();
    login(EMAIL_READ_WRITE_PROJECT_1);
    setProject(PROJECT1);
    authorizeComputeService(PROJECT1, EMAIL_READ_WRITE_PROJECT_1);
    expectOk(delete(instanceIdUri(PROJECT1, ZONE1, BIDDER2)));
    verifyDeleteNotification(PROJECT1, ZONE1, BIDDER2);
  }

  @Test
  public void delete_deleteNotFound_ok() {
    standardFixtures();
    login(EMAIL_READ_WRITE_PROJECT_1);
    setProject(PROJECT1);
    authorizeComputeService(PROJECT1, EMAIL_READ_WRITE_PROJECT_1);

    Instance runningBalancer = buildBalancer(
        PROJECT1,
        EMAIL_OWNER_PROJECT_1_READ_PROJECT_2,
        BALANCER1,
        PARAMS_2);
    addInstance(PROJECT1, runningBalancer);
    Instance stoppingBalancer = cloneInstance(runningBalancer);
    stoppingBalancer.setStatus(InstanceStatus.STOPPING.toString());
    Instance deletedBalancer = cloneInstance(runningBalancer);
    deletedBalancer.setStatus(InstanceStatus.TERMINATED.toString());

    expectOk(delete(instanceIdUri(PROJECT1, ZONE1, BALANCER1)));
    verifyNotification(PROJECT1, ZONE1, stoppingBalancer);
    verifyNotification(PROJECT1, ZONE1, deletedBalancer);
  }

  @Test
  public void delete_instanceExists_ok() {
    standardFixtures();
    login(EMAIL_READ_WRITE_PROJECT_1);
    setProject(PROJECT1);
    authorizeComputeService(PROJECT1, EMAIL_READ_WRITE_PROJECT_1);

    Instance runningBidder = buildBidder(
        PROJECT1,
        EMAIL_OWNER_PROJECT_1_READ_PROJECT_2,
        BIDDER1,
        PARAMS_1);
    addInstance(PROJECT1, runningBidder);
    Instance stoppingBidder = cloneInstance(runningBidder);
    stoppingBidder.setStatus(InstanceStatus.STOPPING.toString());
    Instance deletedBidder = cloneInstance(runningBidder);
    deletedBidder.setStatus(InstanceStatus.TERMINATED.toString());

    expectOk(delete(instanceIdUri(PROJECT1, ZONE1, BIDDER1)));
    verifyDeleteInstance(PROJECT1, BIDDER1);
    verifyNotification(PROJECT1, ZONE1, stoppingBidder);
    verifyNotification(PROJECT1, ZONE1, deletedBidder);
  }

  private String instanceIdUri(String projectName, String zoneName, String instanceName) {
    return instanceIdUri(getProject(projectName).getId(), zoneName, instanceName);
  }

  private String instanceIdUri(long projectId, String zoneName, String instanceName) {
    return ResourceType.INSTANCE
        .getResourceId(Long.toString(projectId), zoneName, instanceName)
        .getResourceUri();
  }

  private String instanceCollectionUri(String projectName, String zoneName) {
    return instanceCollectionUri(getProject(projectName).getId(), zoneName);
  }

  private String instanceCollectionUri(long projectId, String zoneName) {
    return ResourceType.INSTANCE
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

  private Instance buildBidder(
      String projectName,
      String email,
      String instanceName,
      Map<String, String> params) {

    Project project = getProject(projectName);
    Instance instance = buildInstance(project, email, instanceName, params);
    configureBidder(project, instance);
    return instance;
  }

  private Instance buildBalancer(
      String projectName,
      String email,
      String instanceName,
      Map<String, String> params) {

    Project project = getProject(projectName);
    Instance instance = buildInstance(project, email, instanceName, params);
    configureBalancer(instance);
    return instance;
  }

  private Instance buildInstance(
      Project project,
      String email,
      String instanceName,
      Map<String, String> params) {

    return buildInstance(
        project,
        email,
        instanceName,
        params.get("status"),
        params.get("zone"),
        params.get("machineType"),
        buildNetworkInterface(
            project.getApiProjectId(),
            project.getNetworkName(),
            params.get("internalIp"),
            params.get("externalIp")
        )
    );
  }

  private Instance buildInstance(
      Project project,
      String email,
      String name,
      String status,
      String zoneName,
      String machineTypeName,
      NetworkInterface networkInterface) {

    Instance instance = new Instance();
    String apiProjectId = project.getApiProjectId();
    ResourceName instanceName = ComputeResourceType.INSTANCE.buildName(
        apiProjectId, name, zoneName);
    ResourceName zone = ComputeResourceType.ZONE.buildName(apiProjectId, zoneName);
    ResourceName machineType = ComputeResourceType.MACHINE_TYPE.buildName(
        apiProjectId, zoneName, machineTypeName);

    instance.setSelfLink(instanceName.getResourceUrl());
    instance.setName(name);
    instance.setStatus(status);
    instance.setZone(zone.getResourceUrl());
    instance.setMachineType(machineType.getResourceUrl());
    instance.setDescription("Created by " + email);
    instance.setCreationTimestamp(clock.now().toString());
    instance.setNetworkInterfaces(Arrays.asList(networkInterface));
    return instance;
  }

  private void configureBidder(
      Project project,
      Instance instance) {

    Map<String, String> items = ImmutableMap.<String, String>builder()
        .put(InstanceBuilder.METADATA_STARTUP_SCRIPT, "/etc/ob/bootstrap-bidder.sh")
        .put(InstanceBuilder.METADATA_IMAGE, DEFAULT_IMAGE1)
        .put(InstanceBuilder.METADATA_IMAGE_TYPE, InstanceBuilder.IMAGE_DEFAULT)
        .put(BidderInstanceBuilder.METADATA_LISTEN_PORT, "8888")
        .put(BidderInstanceBuilder.METADATA_BID_INTERCEPTORS,
            "com.google.openbidder.interceptor1,com.google.openbidder.interceptor2")
        .put(BidderInstanceBuilder.METADATA_JVM_PARAMETERS, Joiner.on(' ').join(
            "-Xmx768m",
            "-XX:+CMSClassUnloadingEnabled"))
        .put(BidderInstanceBuilder.METADATA_PROJECT_ID, project.getApiProjectId())
        .put(BidderInstanceBuilder.METADATA_PROJECT_NUMBER, "9782389472342")
        .build();
    instance.setMetadata(buildMetadata(items));
    instance.setTags(new Tags().setItems(Arrays.asList(BidderInstanceBuilder.TAG)));
  }

  private void configureBalancer(Instance instance) {
    Map<String, String> items = ImmutableMap.<String, String>builder()
        .put(InstanceBuilder.METADATA_STARTUP_SCRIPT, "bootstrap-balancer.sh")
        .put(InstanceBuilder.METADATA_IMAGE, DEFAULT_IMAGE1)
        .put(InstanceBuilder.METADATA_IMAGE_TYPE, InstanceBuilder.IMAGE_DEFAULT)
        .put(LoadBalancerInstanceBuilder.METADATA_BIDDER_REQUEST_PORT, "8888")
        .put(LoadBalancerInstanceBuilder.METADATA_REQUEST_PORT, "9092")
        .put(LoadBalancerInstanceBuilder.METADATA_HAPROXY_STATS_PORT, "8777")
        .build();
    instance.setMetadata(buildMetadata(items));
    instance.setTags(new Tags().setItems(Arrays.asList(BidderInstanceBuilder.TAG)));
  }

  private NetworkInterface buildNetworkInterface(
      String apiProjectId,
      String networkName,
      String internalIp,
      String externalIp) {

    ResourceName network = ComputeResourceType.NETWORK.buildName(apiProjectId, networkName);
    NetworkInterface networkInterface = new NetworkInterface();
    networkInterface.setNetwork(network.getResourceUrl());
    networkInterface.setNetworkIP(internalIp);
    if (externalIp != null) {
      AccessConfig accessConfig = new AccessConfig();
      accessConfig.setNatIP(externalIp);
      networkInterface.setAccessConfigs(Arrays.asList(accessConfig));
    }
    return networkInterface;
  }

  private Metadata buildMetadata(Map<String, String> itemMap) {
    List<Metadata.Items> itemList = ImmutableList.copyOf(
        Collections2.transform(itemMap.entrySet(), METADATA_ITEM));
    Metadata metadata = new Metadata();
    metadata.setItems(itemList);
    return metadata;
  }

  private void addInstance(String projectName, Instance instance) {
    getComputeClient(projectName).addInstance(instance);
  }

  private void verifyDeleteInstance(String projectName, String instanceName) {
    assertNull(getComputeClient(projectName).getInstanceDirect(instanceName));
  }

  private void verifyDeleteNotification(String projectName, String zoneName, String instanceName) {
    Project project = getProject(projectName);
    InstanceResource deleteMessage = deleteInstanceMessage(project.getId(), zoneName, instanceName);
    verifyNotification(Topic.INSTANCE, deleteMessage);
  }

  private void verifyNotification(String projectName, String zoneName, Instance instance) {
    Project project = getProject(projectName);
    InstanceResource message = build(project, zoneName, instance);
    verifyNotification(Topic.INSTANCE, message);
  }

  private InstanceResource build(Project project, String zoneName, Instance instance) {
    ResourceCollectionId instances = ResourceType.INSTANCE.getResourceCollectionId(
        Long.toString(project.getId()), zoneName);
    return InstanceResource.build(project, instances, instance);
  }

  private InstanceResource deleteInstanceMessage(
      long projectId,
      String zoneName,
      String instanceName) {
    InstanceResource instanceResource = new InstanceResource();
    instanceResource.setId(ResourceType.INSTANCE.getResourceId(
        Long.toString(projectId), zoneName, instanceName));
    instanceResource.setStatus(InstanceStatus.TERMINATED.toString());
    return instanceResource;
  }

  private Object minimalRequest(String projectName, String zoneName) {
    return ImmutableMap.of("zone",
        ResourceType.ZONE.getResourceId(Long.toString(getProject(projectName).getId()), zoneName));
  }

  private static Instance cloneInstance(Instance instance) {
    Instance newInstance = new Instance();
    for (Map.Entry<String, Object> entry : instance.entrySet()) {
      newInstance.set(entry.getKey(), entry.getValue());
    }
    newInstance.setUnknownKeys(instance.getUnknownKeys());
    return newInstance;
  }
}
