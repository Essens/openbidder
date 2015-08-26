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

package com.google.openbidder.ui.controller.admin;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.openbidder.ui.notify.NotificationService;
import com.google.openbidder.ui.preferreddeals.PreferredDealsService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.inject.Inject;

/**
 * Scheduled tasks.
 */
@Controller
@RequestMapping("/cron")
public class CronController {

  private static final Logger logger = LoggerFactory.getLogger(CronController.class);

  private final NotificationService notificationService;
  private final PreferredDealsService preferredDealsService;

  @Inject
  public CronController(
      NotificationService notificationService,
      PreferredDealsService preferredDealsService) {
    this.notificationService = checkNotNull(notificationService);
    this.preferredDealsService = checkNotNull(preferredDealsService);
  }

  @RequestMapping("/subscription-cleanup")
  public void cleanupChannels() {
    logger.info("Removing stable subscriptions");
    notificationService.removeStaleSubscriptions();
  }

  @RequestMapping("/preferred-deals-update/{projectId}")
  public void updatePreferredDeals(@PathVariable String projectId) {
    logger.info("Updating preferred deals on Google Cloud Storage");
    preferredDealsService.uploadPreferredDeals(Long.parseLong(projectId));
  }
}
