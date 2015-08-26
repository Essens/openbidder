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

package com.google.openbidder.ui.notify.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.appengine.api.channel.ChannelFailureException;
import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.common.collect.ImmutableMap;
import com.google.openbidder.ui.dao.SubscriberDao;
import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.entity.Subscriber;
import com.google.openbidder.ui.notify.NotificationService;
import com.google.openbidder.ui.notify.Topic;
import com.google.openbidder.ui.notify.exception.MessageGenerationException;
import com.google.openbidder.ui.user.UserIdService;
import com.google.openbidder.ui.util.db.Transactable;
import com.google.openbidder.util.Clock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.Objectify;

import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Default implementation of {@link NotificationService} using the App Engine Channel Service.
 */
public class NotificationServiceImpl implements NotificationService {

  private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

  private final UserIdService userIdService;
  private final ChannelService channelService;
  private final SubscriberDao subscriberDao;
  private final ObjectMapper objectMapper;
  private final Clock clock;
  private final Duration maxSubscriptionAge;

  @Inject
  public NotificationServiceImpl(
      UserIdService userIdService,
      ChannelService channelService,
      SubscriberDao subscriberDao,
      ObjectMapper objectMapper,
      Clock clock,
      @Value("${Max.Subscription.AgeMs}") long maxSubscriptionAgeMilliseconds) {

    this.userIdService = userIdService;
    this.channelService = channelService;
    this.subscriberDao = subscriberDao;
    this.objectMapper = objectMapper;
    this.clock = clock;
    this.maxSubscriptionAge = new Duration(maxSubscriptionAgeMilliseconds);
  }

  @Override
  public String createToken(Long projectId) {
    String clientId = UUID.randomUUID().toString();
    String userId = userIdService.getUserId();
    String token = channelService.createChannel(clientId);
    logger.info("Created channel with token {} for user {}", token, userId);
    Subscriber subscriber = new Subscriber(clientId);
    subscriber.setUserId(userId);
    if (projectId != null) {
      subscriber.setProject(Project.key(projectId));
    }
    subscriber.setCreatedAt(clock.now());
    subscriber.setToken(token);
    subscriberDao.save(subscriber);
    return token;
  }

  @Override
  public void notify(long projectId, Topic topic, Object message) {
    checkNotNull(topic);
    checkNotNull(message);
    String json;
    try {
      // TODO(wshields): replace projectId with resource ID on old UI turndown
      json = objectMapper.writeValueAsString(ImmutableMap.of(
          "projectId", projectId,
          "topic", topic,
          "message", message));
    } catch (IOException e) {
      throw new MessageGenerationException(e.getMessage(), projectId, topic);
    }
    for (String clientId : subscriberDao.getAllSubscribersForProject(projectId)) {
      try {
        channelService.sendMessage(new ChannelMessage(clientId, json));
        logger.debug("Send successful to subscriber {}", clientId);
      } catch (ChannelFailureException e) {
        logger.warn("Send failed to subscriber {}", clientId);
      }
    }
  }

  @Override
  public void setProject(String token, final long projectId) {
    subscriberDao.updateSubscriber(userIdService.getUserId(), token,
        new Transactable<Subscriber, Void>() {
          @Override public @Nullable Void work(Subscriber subscriber, Objectify ofy) {
            subscriber.setProject(Project.key(projectId));
            ofy.save().entity(subscriber);
            return null;
          }
        });
  }

  @Override
  public void removeStaleSubscriptions() {
    subscriberDao.deleteSubscriptionsOlderThan(maxSubscriptionAge);
  }


  @Override
  public void connect(String clientId) {
    try {
      subscriberDao.updateSubscriber(clientId, new Transactable<Subscriber, Void>() {
        @Override public @Nullable Void work(Subscriber subscriber, Objectify ofy) {
          subscriber.setActiveSince(clock.now());
          ofy.save().entity(subscriber);
          return null;
        }
      });
    } catch (NotFoundException e) {
      // no action
    }
  }

  @Override
  public void disconnect(String clientId) {
    try {
      subscriberDao.updateSubscriber(clientId, new Transactable<Subscriber, Void>() {
        @Override public @Nullable Void work(Subscriber subscriber, Objectify ofy) {
          ofy.delete().entity(subscriber);
          return null;
        }
      });
    } catch (NotFoundException e) {
      // assume it's already cleaned up; do nothing
    }
  }
}
