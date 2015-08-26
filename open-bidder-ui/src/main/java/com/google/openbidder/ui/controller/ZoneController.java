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

import com.google.openbidder.ui.controller.support.ResourceController;
import com.google.openbidder.ui.resource.ZoneResourceService;
import com.google.openbidder.ui.resource.model.ZoneResource;

import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

/**
 * RESTful API controller for {@link ZoneResource}s.
 */
@Controller
@RequestMapping("/projects/{parentResourceName}/zones")
public class ZoneController extends ResourceController<ZoneResource> {

  private final ZoneResourceService zoneResourceService;

  @Inject
  public ZoneController(ZoneResourceService zoneResourceService) {
    super(zoneResourceService);
    this.zoneResourceService = zoneResourceService;
  }

  @RequestMapping(
      value = "/{zoneId}/register",
      method = RequestMethod.POST,
      consumes = "application/json",
      produces = "application/json")
  @ResponseBody
  public ZoneResource registerZone(
      @PathVariable("parentResourceName") String projectId,
      @RequestBody @Validated ZoneResource zoneResource) {
    return zoneResourceService.register(projectId, zoneResource);
  }

  @RequestMapping(
      value = "/{zoneId}/register",
      method = RequestMethod.DELETE,
      produces = "application/json")
  @ResponseBody
  public ZoneResource unregisterZone(
      @PathVariable("parentResourceName") String projectId,
      @PathVariable("zoneId") String zoneId) {
    return zoneResourceService.unregister(projectId, zoneId);
  }
}
