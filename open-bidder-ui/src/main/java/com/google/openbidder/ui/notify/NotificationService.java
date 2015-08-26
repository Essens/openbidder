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

package com.google.openbidder.ui.notify;

/**
 * Server-to-client publish-subscribe service.
 * TODO(wshields): rename to MessageService when the old UI is turned down
 */
public interface NotificationService {

  /**
   * @return Connection token for async notifications.
   */
  String createToken(Long projectId);

  /**
   * Send a message to all listeners on a given project with a specified topic.
   */
  void notify(long projectId, Topic topic, Object message);

  /**
   * Modify the project assigned to a given token.
   */
  void setProject(String token, long projectId);

  /**
   * Remove stale subscriptions.
   */
  void removeStaleSubscriptions();

  /**
   * Mark a connection as active.
   */
  void connect(String clientId);

  /**
   * Mark a connection as inactive.
   */
  void disconnect(String clientId);
}
