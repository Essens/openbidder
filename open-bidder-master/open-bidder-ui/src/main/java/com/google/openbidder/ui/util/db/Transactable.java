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

package com.google.openbidder.ui.util.db;

import com.googlecode.objectify.Objectify;

/**
 * Represents a single unit of work to occur within the context of a transaction.
 */
public interface Transactable<F, T> {

  /**
   * Perform a unit of work under the assumption that it is within a single transaction.
   * Implementations should not begin sessions or start or end transactions as the
   * calling code will take responsibility for that.
   */
  T work(F item, Objectify ofy);
}
