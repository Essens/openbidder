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

package com.google.openbidder.ui.entity;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.openbidder.ui.notify.Topic;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import org.joda.time.Instant;

import java.util.List;

/**
 * Represents a subscriber (in the publish-subscribe sense).
 */
@Entity
public class Subscriber {

  @Id
  @Index
  private String clientId;

  @Index
  private String userId;

  @Index
  private Key<Project> project;

  @Index
  private String token;

  @Index
  private Instant createdAt;

  @Index
  private Instant activeSince;

  private List<Topic> topics;

  @SuppressWarnings("unused")
  private Subscriber() {
  }

  public Subscriber(String clientId) {
    this.clientId = checkNotNull(clientId);
  }

  /**
   * @return Unique identifier for Channel API
   */
  public String getClientId() {
    return clientId;
  }

  /**
   * @return Email address
   */
  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * @return Associated {@link Project}.
   */
  public Key<Project> getProject() {
    return project;
  }

  public void setProject(Key<Project> project) {
    this.project = project;
  }

  /**
   * @return Token to open channel with
   */
  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  /**
   * @return Date created.
   */
  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  /**
   * @return Start time for accepting messages
   */
  public Instant getActiveSince() {
    return activeSince;
  }

  public void setActiveSince(Instant activeSince) {
    this.activeSince = activeSince;
  }

  /**
   * @return Topics listened to.
   */
  public List<Topic> getTopics() {
    return topics;
  }

  public void setTopics(List<Topic> topics) {
    this.topics = topics;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("clientId", clientId)
        .add("userId", userId)
        .add("createdAt", createdAt)
        .add("activeSince", activeSince)
        .add("topics", topics)
        .toString();
  }

  public static Key<Subscriber> key(String clientId) {
    return Key.create(Subscriber.class, clientId);
  }
}
