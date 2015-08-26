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

package com.google.openbidder.config.http;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableList;

/**
 * A webserver feature. Implemented as an enum-like class to allow extensibility.
 */
public class Feature {
  public static ImmutableList<Feature> values = ImmutableList.of();

  public static final Feature BID = new Feature("BID", false);
  public static final Feature IMPRESSION = new Feature("IMPRESSION", false);
  public static final Feature CLICK = new Feature("CLICK", false);
  public static final Feature MATCH = new Feature("MATCH", false);
  public static final Feature ADMIN = new Feature("ADMIN", true);
  public static final Feature OTHER = new Feature("OTHER", false);

  private final int ordinal;
  private final String name;
  private final boolean admin;

  protected Feature(String name, boolean admin) {
    synchronized(Feature.class) {
      this.ordinal = values.size();
      this.name = checkNotNull(name);
      this.admin = admin;

      checkState(valueOf(name) == null);
      values = ImmutableList.<Feature>builder().addAll(values).add(this).build();
    }
  }

  public final int ordinal() {
    return ordinal;
  }

  public final String name() {
    return name;
  }

  public final boolean admin() {
    return admin;
  }

  public final Feature[] values() {
    ImmutableList<Feature> values = Feature.values;
    return values.toArray(new Feature[values.size()]);
  }

  public static Feature valueOf(String name) {
    checkNotNull(name);

    for (Feature feature : values) {
      if (feature.name.equals(name)) {
        return feature;
      }
    }

    return null;
  }

  @Override
  public int hashCode() {
    return ordinal;
  }

  @Override
  public boolean equals(Object obj) {
    return obj == this;
  }

  @Override
  public String toString() {
    return name;
  }
}
