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

import com.google.appengine.api.channel.ChannelPresence;
import com.google.appengine.api.channel.ChannelService;
import com.google.common.base.Preconditions;
import com.google.openbidder.ui.notify.NotificationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

/**
 * Controller for handling presence notifications for Channel API listeners. This listens for
 * AppEngine supplied connection and disconnection messages and acts appropriately.
 */
@Controller
public class PresenceController {

  private static final Logger logger = LoggerFactory.getLogger(PresenceController.class);

  private static final String STATUS_CONNECTED = "connected";
  private static final String STATUS_DISCONNECTED = "disconnected";

  private final NotificationService notificationService;
  private final ChannelService channelService;

  @Inject
  public PresenceController(
      NotificationService notificationService,
      ChannelService channelService) {

    this.notificationService = Preconditions.checkNotNull(notificationService);
    this.channelService = Preconditions.checkNotNull(channelService);
  }

  @RequestMapping(value = "/_ah/channel/{status}", method = RequestMethod.POST)
  public void channelStatus(
      @PathVariable String status,
      HttpServletRequest request) throws IOException {

    ChannelPresence presence = channelService.parsePresence(request);
    if (STATUS_CONNECTED.equals(status)) {
      notificationService.connect(presence.clientId());
    } else if (STATUS_DISCONNECTED.equals(status)) {
      notificationService.disconnect(presence.clientId());
    } else {
      logger.info("Unknown presence status: {}", status);
    }
  }
}
