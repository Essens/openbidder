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

package com.google.openbidder.ui.controller;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.openbidder.ui.controller.support.RootResourceController;
import com.google.openbidder.ui.resource.ProjectResourceService;
import com.google.openbidder.ui.resource.model.ProjectResource;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

@Controller
@RequestMapping("/projects")
public class ProjectController extends RootResourceController<ProjectResource> {

  private final ProjectResourceService projectResourceService;

  @Inject
  public ProjectController(ProjectResourceService projectResourceService) {
    super(projectResourceService);
    this.projectResourceService = checkNotNull(projectResourceService);
  }

  @RequestMapping(
      value = "/{projectId}/defaultProject",
      method = RequestMethod.POST,
      produces = "application/json")
  @ResponseBody
  public ProjectResource setAsDefault(
      @PathVariable("projectId") String projectId) {
    return projectResourceService.setAsDefault(projectId);
  }

  @RequestMapping(
      value = "/{projectId}/authorized",
      method = RequestMethod.POST,
      produces = "application/json")
  @ResponseBody
  public ResponseEntity<Void> verifyTokens(
      @PathVariable("projectId") String projectId) {
    boolean isAuthorized = projectResourceService.isAuthorized(projectId);
    return new ResponseEntity<>(isAuthorized ? HttpStatus.OK : HttpStatus.UNAUTHORIZED);
  }

  @RequestMapping(
      value = "/{projectId}/authorized",
      method = RequestMethod.DELETE,
      produces = "application/json")
  @ResponseBody
  public ProjectResource revokeTokens(
      @PathVariable("projectId") String projectId) {
    return projectResourceService.revokeAuthorization(projectId);
  }
}
