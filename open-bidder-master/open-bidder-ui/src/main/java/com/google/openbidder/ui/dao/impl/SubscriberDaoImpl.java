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

package com.google.openbidder.ui.dao.impl;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.openbidder.ui.dao.SubscriberDao;
import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.entity.Subscriber;
import com.google.openbidder.ui.notify.SubscriberNotFoundException;
import com.google.openbidder.ui.notify.Topic;
import com.google.openbidder.ui.util.db.Db;
import com.google.openbidder.ui.util.db.Transactable;
import com.google.openbidder.util.Clock;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;

import org.joda.time.Duration;

import javax.inject.Inject;

/**
 * Default implementation of {@link SubscriberDao}.
 */
public class SubscriberDaoImpl implements SubscriberDao {

  private static Function<Key<?>, String> GET_KEY_NAME = new Function<Key<?>, String>() {
    @Override public String apply(Key<?> key) {
      return key.getName();
    }};

  private final ObjectifyFactory objectifyFactory;
  private final Clock clock;

  @Inject
  public SubscriberDaoImpl(
      ObjectifyFactory objectifyFactory,
      Clock clock) {

    this.objectifyFactory = Preconditions.checkNotNull(objectifyFactory);
    this.clock = Preconditions.checkNotNull(clock);
  }

  @Override
  public void save(Subscriber subscriber) {
    Objectify ofy = objectifyFactory.begin();
    ofy.save().entity(subscriber);
  }

  @Override
  public Iterable<Subscriber> getAllSubscribers() {
    Objectify ofy = objectifyFactory.begin();
    return ofy.load().type(Subscriber.class).iterable();
  }

  @Override
  public Iterable<String> getAllSubscribersForProjectAndTopic(long projectId, Topic topic) {
    Preconditions.checkNotNull(topic);
    Objectify ofy = objectifyFactory.begin();
    Iterable<Key<Subscriber>> query = ofy.load().type(Subscriber.class)
        .filter("project", Project.key(projectId))
        .filter("topics", topic)
        .filter("activeSince <=", clock.now())
        .keys();
    return Iterables.transform(query, GET_KEY_NAME);
  }

  @Override
  public Iterable<String> getAllSubscribersForProject(long projectId) {
    Objectify ofy = objectifyFactory.begin();
    Iterable<Key<Subscriber>> query = ofy.load().type(Subscriber.class)
        .filter("project", Project.key(projectId))
        .filter("activeSince <=", clock.now())
        .keys();
    return Iterables.transform(query, GET_KEY_NAME);
  }

  @Override
  public <T> T updateSubscriber(String clientId, Transactable<Subscriber, T> worker) {
    return Db.updateInTransaction(objectifyFactory, Subscriber.key(clientId), worker);
  }

  @Override
  public <T> T updateSubscriber(
      String userId,
      String oldToken,
      Transactable<Subscriber, T> worker) {

    Preconditions.checkNotNull(userId);
    Preconditions.checkNotNull(oldToken);
    Preconditions.checkNotNull(worker);
    Objectify ofy = objectifyFactory.begin();
    Iterable<Key<Subscriber>> query = ofy.load().type(Subscriber.class)
        .filter("userId", userId)
        .filter("token", oldToken)
        .keys();
    Key<Subscriber> subscriberKey = Iterables.getFirst(query, null);
    if (subscriberKey == null) {
      throw new SubscriberNotFoundException(userId, oldToken);
    }
    return Db.updateInTransaction(objectifyFactory, subscriberKey, worker);
  }

  @Override
  public void deleteSubscriptionsOlderThan(Duration duration) {
    Objectify ofy = objectifyFactory.begin();
    ofy.delete().keys(ofy.load().type(Subscriber.class)
        .filter("createdAt <=", clock.now().minus(duration))
        .keys());
  }
}
