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

package com.google.openbidder.ui.util;

import static java.util.Arrays.asList;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * A counter where each item counted is an {@link Enum}.
 */
public class EnumCounter<T extends Enum<T>> {

  private final Class<T> klass;
  private final int[] counts;
  private int total;

  public EnumCounter(Class<T> klass) {
    this.klass = Preconditions.checkNotNull(klass);
    counts = new int[klass.getEnumConstants().length];
  }

  public int increment(T value) {
    total++;
    return ++counts[value.ordinal()];
  }

  public int getCount(T value) {
    return counts[value.ordinal()];
  }

  public int getTotal() {
    return total;
  }

  public ImmutableMap<T, Integer> toMap() {
    return Maps.toMap(asList(klass.getEnumConstants()), new Function<T, Integer>() {
      @Override public Integer apply(T t) {
        return counts[t.ordinal()];
      }
    });
  }

  public static <T extends Enum<T>> EnumCounter<T> newInstance(Class<T> klass) {
    return new EnumCounter<>(klass);
  }
}
