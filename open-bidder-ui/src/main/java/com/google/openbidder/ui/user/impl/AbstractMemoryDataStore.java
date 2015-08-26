/*
 * Copyright 2015 Google Inc. All Rights Reserved.
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

package com.google.openbidder.ui.user.impl;

import static com.google.api.client.util.Preconditions.checkNotNull;

import com.google.api.client.util.store.AbstractDataStore;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.DataStoreUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory {@link DataStore}.
 */
abstract class AbstractMemoryDataStore<V extends Serializable> extends AbstractDataStore<V> {
  ConcurrentHashMap<String, V> map = new ConcurrentHashMap<>();

  protected AbstractMemoryDataStore(DataStoreFactory dataStoreFactory, String id) {
    super(dataStoreFactory, id);
  }

  @Override
  public final Set<String> keySet() throws IOException {
    return map.keySet();
  }

  @Override
  public final Collection<V> values() {
    return map.values();
  }

  @Override
  public final V get(String key) {
    return map.get(key);
  }

  @Override
  public DataStore<V> set(String key, V value) throws IOException {
    checkNotNull(key);
    checkNotNull(value);
    map.put(key, value);
    return this;
  }

  @Override
  public DataStore<V> delete(String key) throws IOException {
    if (key != null) {
      map.remove(key);
    }
    return this;
  }

  @Override
  public final DataStore<V> clear() throws IOException {
    for (String key : keySet()) {
      delete(key);
    }
    return this;
  }

  @Override
  public final boolean containsKey(String key) {
    return key == null ? false : map.containsKey(key);
  }

  @Override
  public final boolean containsValue(V value) {
    return value == null ? false : map.containsValue(value);
  }

  @Override
  public final boolean isEmpty() {
    return map.isEmpty();
  }

  @Override
  public final int size() {
    return map.size();
  }

  @Override
  public String toString() {
    return DataStoreUtils.toString(this);
  }
}
