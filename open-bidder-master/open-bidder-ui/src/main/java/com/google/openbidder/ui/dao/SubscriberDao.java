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

package com.google.openbidder.ui.dao;

import com.google.openbidder.ui.entity.Subscriber;
import com.google.openbidder.ui.notify.Topic;
import com.google.openbidder.ui.util.db.Transactable;

import org.joda.time.Duration;

/**
 * Persistence functions related to {@link Subscriber} entities.
 */
public interface SubscriberDao {

  /**
   * Persist a {@link Subscriber}.
   */
  void save(Subscriber subscriber);

  /**
   * @return All {@link Subscriber}s.
   */
  Iterable<Subscriber> getAllSubscribers();

  /**
   * Find all subscribers matching the given project and topic.
   *
   * @return Collection of client IDs.
   */
  Iterable<String> getAllSubscribersForProjectAndTopic(long projectId, Topic topic);

  /**
   * Find all subscribers for a given project.
   *
   * @return Collection of client IDs.
   */
  Iterable<String> getAllSubscribersForProject(long projectId);

  /**
   * Update a {@link Subscriber} or create one if one doesn't exist already.
   */
  <T> T updateSubscriber(String clientId, Transactable<Subscriber, T> worker);

  /**
   * Update a subscriber's token.
   */
  <T> T updateSubscriber(String userId, String oldToken, Transactable<Subscriber, T> worker);

  /**
   * Delete subscriptions older than
   */
  void deleteSubscriptionsOlderThan(Duration duration);
}
