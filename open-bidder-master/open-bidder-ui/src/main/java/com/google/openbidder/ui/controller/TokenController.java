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

import com.google.common.base.Preconditions;
import com.google.openbidder.ui.controller.support.AbstractErrorHandlingController;
import com.google.openbidder.ui.notify.NotificationService;
import com.google.openbidder.ui.resource.support.ResourceId;
import com.google.openbidder.ui.resource.support.ResourceType;
import com.google.openbidder.ui.util.json.ResourceIdDeserializer;
import com.google.openbidder.ui.util.validation.ResourcePathType;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Service for distributing async notification service tokens.
 */
@Controller
public class TokenController extends AbstractErrorHandlingController<Void> {

  private final NotificationService notificationService;

  @Inject
  public TokenController(NotificationService notificationService) {
    this.notificationService = Preconditions.checkNotNull(notificationService);
  }

  @RequestMapping(
      value = "/token/new",
      method = RequestMethod.GET,
      produces = "application/json")
  @ResponseBody
  public String newtoken(@RequestParam(required = false) Long projectId) {
    return notificationService.createToken(projectId);
  }

  @RequestMapping(
      value = "/token/project",
      method = RequestMethod.POST,
      consumes = "application/json")
  @ResponseBody
  public @Nullable Void setProject(@RequestBody @Valid ProjectToken projectToken) {
    long projectId = Long.parseLong(projectToken.getProject().getResourceName());
    notificationService.setProject(projectToken.getToken(), projectId);
    return null;
  }

  public static class ProjectToken {

    @NotNull
    @ResourcePathType(type = ResourceType.PROJECT)
    private ResourceId project;

    @NotNull
    private String token;

    public ResourceId getProject() {
      return project;
    }

    @JsonDeserialize(using = ResourceIdDeserializer.class)
    public void setProject(ResourceId project) {
      this.project = project;
    }

    public String getToken() {
      return token;
    }

    public void setToken(String token) {
      this.token = token;
    }
  }
}
