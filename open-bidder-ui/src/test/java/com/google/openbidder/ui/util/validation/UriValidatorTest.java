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

package com.google.openbidder.ui.util.validation;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.openbidder.ui.entity.Project;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 * Tests for {@link UriValidator}.
 */
public class UriValidatorTest {
  private static Validator validator;

  private Project project;

  // Retrieves the validator instance.
  @BeforeClass
  public static void setUpClass() {

    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Before
  public void setUp() {
    project = createValidProject();
  }

  @Test
  public void testInvalidSchemeUri() {
    Set<ConstraintViolation<Project>> constraintViolations = validator.validate(project);
    assertEquals(0, constraintViolations.size());

    project.setUserDistUri("test");
    constraintViolations = validator.validate(project);
    assertEquals(1, constraintViolations.size());
  }

  private Project createValidProject() {
    Project project = new Project();
    project.setApiProjectNumber(123L);
    project.setUserDistUri("gs://open-bidder-user");
    project.setApiProjectId("testApiProjectId");
    project.setOauth2ClientId("testOauth2clientId");
    project.setOauth2ClientSecret("testOauth2clientSecret");
    project.setBidInterceptors(ImmutableList.of("Bidding1", "Bidding2"));
    project.setImpressionInterceptors(ImmutableList.of("Impression1", "Impression2"));
    project.setClickInterceptors(ImmutableList.of("Click1", "Click2"));
    project.setProjectName("testProject");
    project.setVmParameters("testVmParameters");
    project.setMainParameters("testMainParameters");
    project.setLoadBalancerRequestPort("18080");
    project.setBidderRequestPort("18081");
    project.setBidderAdminPort("18082");
    project.setLoadBalancerStatPort("18083");
    return project;
  }
}
