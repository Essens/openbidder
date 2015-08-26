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

package com.google.openbidder.api.platform;

import javax.annotation.Nullable;

/**
 * Identifies the exchanged used by the bidder, and provides access to some exchange-specific
 * factories or utilities. A single instance of this class should exist for any particular exchange.
 */
public abstract class Exchange {
  private final String id;

  protected Exchange(String id) {
    this.id = id;
  }

  /**
   * @return the exchange ID
   */
  public final String getId() {
    return id;
  }

  /**
   * Creates a new response, in the type native to this exchange. The returned object
   * will be in the default state, and will be "open" for modification (e.g., if the exchange's
   * native type is immutable such as a protobuf, this method should return a mutable builder).
   */
  public abstract Object newNativeResponse();

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    return this == obj;
  }

  @Override
  public String toString() {
    return id;
  }
}
