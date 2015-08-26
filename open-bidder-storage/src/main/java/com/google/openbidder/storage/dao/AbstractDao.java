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

package com.google.openbidder.storage.dao;

import com.google.common.collect.Iterables;

import java.util.List;
import java.util.Map;

/**
 * An abstract DAO that implements several functions from the DAO interface.
 */
public abstract class AbstractDao<T> implements Dao<T> {

  @Override
  public void createObject(T object, Iterable<String> path) {
    createObject(object, Iterables.toArray(path, String.class));
  }

  @Override
  public void createObjects(Map<String, ? extends T> objects, Iterable<String> path) {
    createObjects(objects, Iterables.toArray(path, String.class));
  }

  @Override
  public void createObjectList(List<? extends T> objects, Iterable<String> path) {
    createObjectList(objects, Iterables.toArray(path, String.class));
  }

  @Override
  public <U extends T> U getObject(Class<U> klass, Iterable<String> path) {
    return getObject(klass, Iterables.toArray(path, String.class));
  }

  @Override
  public <U extends T> List<U> getObjectList(Class<U> klass, Iterable<String> path) {
    return getObjectList(klass, Iterables.toArray(path, String.class));
  }

  @Override
  public void updateObject(T updatedObject, Iterable<String> path) {
    updateObject(updatedObject, Iterables.toArray(path, String.class));
  }

  @Override
  public void updateObjects(Map<String, ? extends T> updatedObjects, Iterable<String> path) {
    updateObjects(updatedObjects, Iterables.toArray(path, String.class));
  }

  @Override
  public void deleteObject(Iterable<String> path) {
    deleteObject(Iterables.toArray(path, String.class));
  }

  @Override
  public void deleteObjects(Iterable<String> objectNames, Iterable<String> path) {
    deleteObjects(objectNames, Iterables.toArray(path, String.class));
  }

  @Override
  public void deleteAllObjects(Iterable<String> path) {
    deleteAllObjects(Iterables.toArray(path, String.class));
  }

  @Override
  public <U extends T> Iterable<U> findAll(Class<U> klass, String condition, Iterable<String> path) {
    return findAll(klass, condition, Iterables.toArray(path, String.class));
  }
}
