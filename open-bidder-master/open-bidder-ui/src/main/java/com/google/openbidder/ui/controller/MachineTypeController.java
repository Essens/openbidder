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

import com.google.openbidder.ui.controller.support.GrandChildResourceController;
import com.google.openbidder.ui.resource.MachineTypeResourceService;
import com.google.openbidder.ui.resource.model.MachineTypeResource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.inject.Inject;

/**
 * RESTful API controller for {@link MachineTypeResource}s.
 */
@Controller
@RequestMapping("/projects/{parentResourceName}/zones/{childResourceName}/machinetypes")
public class MachineTypeController
    extends GrandChildResourceController<MachineTypeResource> {

  @Inject
  public MachineTypeController(MachineTypeResourceService machineTypeResourceService) {
    super(machineTypeResourceService);
  }
}
