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

package com.google.openbidder.remarketing.services;

import com.google.openbidder.remarketing.model.Remarketing.Action;
import com.google.openbidder.remarketing.model.Remarketing.TargetedUser;

import java.util.Collection;

/**
 * The remarketing service interface.
 */
public interface RemarketingService {

  /**
   * Add a targeted user to be stored locally.
   *
   * @param user A targeted user object to be added.
   */
  void addTargetedUser(TargetedUser user);

  /**
   * Delete an object locally by id.
   */
  void deleteTargetedUser(String pubUserId);

  /**
   * Delete all the objects both locally and from the data storage.
   */
  void deleteAllTargetedUsers();

  /**
   * Get a targeted user by using its id.
   *
   * @return A TargetedUser object. If there is no match, null is returned.
   */
  TargetedUser getTargetedUser(String pubUserId);

  /**
   * Store the local data in the data storage. Should be called periodically.
   */
  void storeTargetedUsers();

  /**
   * Delete an action from a users list.
   *
   * This should get called when an action for a targeted user is no longer relevant. For example
   * once he/she completes a purchase.
   */
  void deleteActionForUser(String pubUserId, String actionId);

  /**
   * Get a list of enabled actions for a user given their google gid.
   *
   * This gets called from the bid interceptor to get a list of enabled actions, so that it will
   * know if it should bid for a particular targeted user and what snippet to serve based on
   * action.
   */
  Collection<Action> getActionsForUser(String googleGid);

  /**
   * Reload the actions from external storage. This should be called periodically.
   */
  void reloadActions();

  /**
   * Merge the targeted users that are in memory and in external storage and then update the
   * external storage. This should be called periodically.
   */
  void updateTargetedUsers();
}
