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

package com.google.openbidder.ui.controller;

import com.google.openbidder.ui.preferreddeals.PreferredDealsService;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

/**
 * Controller to start Ad Exchange preferred deals upload and synchronization with the
 * Google Cloud Storage.
 */
@Controller
@RequestMapping("/projects/{projectId}/preferredDeals")
public class PreferredDealsController {

  private final PreferredDealsService preferredDealsService;

  @Inject
  public PreferredDealsController(PreferredDealsService preferredDealsService) {
    this.preferredDealsService = preferredDealsService;
  }

  @RequestMapping(value = "/upload", method = RequestMethod.POST)
  @ResponseBody
  void upload(@PathVariable("projectId") String projectId) {
    preferredDealsService.uploadPreferredDeals(Long.parseLong(projectId));
  }
}
