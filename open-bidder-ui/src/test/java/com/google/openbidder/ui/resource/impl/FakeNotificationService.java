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

package com.google.openbidder.ui.resource.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.openbidder.ui.notify.NotificationService;
import com.google.openbidder.ui.notify.Topic;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Fake implementation of {@link NotificationService}.
 */
public class FakeNotificationService implements NotificationService {
  private final Map<String, Long> tokens = new HashMap<>();
  private final Multimap<String, Message> messages = HashMultimap.create();

  @Override
  public String createToken(Long projectId) {
    String token = UUID.randomUUID().toString();
    checkState(tokens.put(token, null) == null);
    return token;
  }

  @Override
  public void notify(long projectId, Topic topic, Object messageBody) {
    Message message = new Message(topic, messageBody);
    for (Map.Entry<String, Long> entry : tokens.entrySet()) {
      if (entry.getValue() != null && entry.getValue() == projectId) {
        messages.put(entry.getKey(), message);
      }
    }
  }

  @Override
  public void setProject(String token, long projectId) {
    tokens.put(token, projectId);
  }

  @Override
  public void removeStaleSubscriptions() {
    // do nothing
  }

  @Override
  public void connect(String clientId) {
    // do nothing
  }

  @Override
  public void disconnect(String clientId) {
    // do nothing
  }

  public Multimap<String, Message> getMessages() {
    return messages;
  }

  public void verifyMessage(String token, final Topic topic, final Object message) {
    checkNotNull(token);
    boolean messageFound = Iterables.any(messages.get(token), new Predicate<Message>() {
      @Override public boolean apply(Message msg) {
        return Objects.equal(msg.topic, topic)
            && Objects.equal(msg.message, message);
      }});
    assertTrue("Token " + token + " has no message on topic " + topic + " of " + message,
        messageFound);
  }

  public void clear() {
    tokens.clear();
    messages.clear();
  }

  public static class Message {
    private final Topic topic;
    private final Object message;

    public Message(Topic topic, Object message) {
      this.topic = checkNotNull(topic);
      this.message = message;
    }

    public Topic getTopic() {
      return topic;
    }

    public Object getMessage() {
      return message;
    }
  }
}
