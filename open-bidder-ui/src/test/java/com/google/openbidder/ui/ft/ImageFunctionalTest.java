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

package com.google.openbidder.ui.ft;

import static com.google.openbidder.ui.resource.ResourceMatchers.image;
import static com.google.openbidder.ui.resource.ResourceMatchers.images;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.google.api.services.compute.model.Image;
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
 * Image resource test.
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
public class ImageFunctionalTest extends OpenBidderFunctionalTestCase {

  @Test
  public void get_noAccess_notFound() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    expectNotFound(get(imageIdUri(PROJECT1, "image-test")));
  }

  @Test
  public void get_noCredentials_unauthorized() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    expectUnauthorized(get(imageIdUri(PROJECT2, DEFAULT_IMAGE1)));
  }

  @Test
  public void get_notFound_notFound() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    authorizeComputeService(PROJECT2, EMAIL_OWNER_PROJECT_2);
    expectNotFound(get(imageIdUri(PROJECT2, DEFAULT_IMAGE1)));
  }

  @Test
  public void get_found_ok() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    authorizeComputeService(PROJECT2, EMAIL_OWNER_PROJECT_2);
    Image image = addImage(PROJECT2, DEFAULT_IMAGE1);
    expectJson(get(imageIdUri(PROJECT2, DEFAULT_IMAGE1)),
        jsonPath("$", image(project2.getId(), image)));
  }

  @Test
  public void list_noAccess_notFound() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    expectNotFound(get(imageCollectionUri(PROJECT1)));
  }

  @Test
  public void list_noCredentials_unauthorized() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    expectUnauthorized(get(imageCollectionUri(PROJECT2)));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  public void list_found_ok() {
    standardFixtures();
    login(EMAIL_OWNER_PROJECT_2);
    authorizeComputeService(PROJECT2, EMAIL_OWNER_PROJECT_2);
    Image image1 = buildImage(API_PROJECT2, DEFAULT_IMAGE1);
    Image image2 = buildImage(API_PROJECT2, DEFAULT_IMAGE2);
    addImages(PROJECT2, image1, image2);
    expectJson(get(imageCollectionUri(PROJECT2)),
        jsonPath("$").isArray(),
        jsonPath("$", hasSize(2)),
        jsonPath("$", containsInAnyOrder(
            (Collection) images(project2.getId(), image1, image2))));
  }

  @Test
  public void postJson_loggedIn_methodNotAllowed() {
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectMethodNotAllowed(postObjectJson(imageCollectionUri(1234), emptyRequest()));
  }

  @Test
  public void postForm_loggedIn_methodNotAllowed() {
    login(EMAIL_OWNER_PROJECT_1_READ_PROJECT_2);
    expectMethodNotAllowed(post(imageCollectionUri(1234)));
  }

  @Test
  public void put_loggedIn_methodNotAllowed() {
    login(EMAIL_OWNER_PROJECT_2);
    expectMethodNotAllowed(putObjectJson(imageIdUri(1234, DEFAULT_IMAGE1), emptyRequest()));
  }

  @Test
  public void delete_loggedIn_methodNotAllowed() {
    login(EMAIL_OWNER_PROJECT_2);
    expectMethodNotAllowed(delete(imageIdUri(1234, DEFAULT_IMAGE2)));
  }

  private String imageIdUri(String projectName, String imageName) {
    return imageIdUri(getProject(projectName).getId(), imageName);
  }

  private String imageIdUri(long projectId, String imageName) {
    return ResourceType.DEFAULT_IMAGE
        .getResourceId(Long.toString(projectId), imageName)
        .getResourceUri();
  }

  private String imageCollectionUri(String projectName) {
    return imageCollectionUri(getProject(projectName).getId());
  }

  private String imageCollectionUri(long projectId) {
    return ResourceType.DEFAULT_IMAGE
        .getResourceCollectionId(Long.toString(projectId))
        .getResourceUri();
  }

  private Image buildImage(String apiProjectId, String imageName) {
    Image image = new Image();
    image.setName(imageName);
    image.setDescription("Test image");
    image.setCreationTimestamp(clock.now().toString());
    ResourceName resourceName = ComputeResourceType.DEFAULT_IMAGE.buildName(
        apiProjectId, imageName);
    image.setSelfLink(resourceName.getResourceUrl());
    return image;
  }

  private Image addImage(String projectName, String imageName) {
    Project project = getProject(projectName);
    Image image = buildImage(project.getApiProjectId(), imageName);
    getComputeClient(project).addDefaultImage(image);
    return image;
  }

  private void addImages(String projectName, Image... images) {
    getComputeClient(projectName).addAllDefaultImages(images);
  }
}
