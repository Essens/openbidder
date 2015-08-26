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

package com.google.openbidder.ui.notify.exception;

import com.google.common.base.Preconditions;
import com.google.openbidder.ui.notify.Topic;

/**
 * Error generating a JSON message.
 */
public class MessageGenerationException extends RuntimeException {

  private static final String MESSAGE = "MessageGenerationException on project %d, topic %s: %s";

  private final String jsonMessage;
  private final long projectId;
  private final Topic topic;

  public MessageGenerationException(String jsonMessage, long projectId, Topic topic) {
    super(String.format(MESSAGE, projectId, Preconditions.checkNotNull(topic),
        Preconditions.checkNotNull(jsonMessage)));
    this.jsonMessage = jsonMessage;
    this.projectId = projectId;
    this.topic = topic;
  }

  public String getJsonMessage() {
    return jsonMessage;
  }

  public long getProjectId() {
    return projectId;
  }

  public Topic getTopic() {
    return topic;
  }
}
