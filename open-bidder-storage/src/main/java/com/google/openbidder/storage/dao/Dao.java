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

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * A generalized DAO interface for Open Bidder.
 */
public interface Dao<T> {

  /**
   * Create and stores an object to some path.
   */
  void createObject(T object, String... path);

  /**
   * Create and stores an object to some path.
   */
  void createObject(T object, Iterable<String> path);

  /**
   * Create and stores the given objects in a path.
   *
   * @param objects map from object name to object
   */
  void createObjects(Map<String, ? extends T> objects, String... path);

  /**
   * Create and stores the given objects in a path.
   *
   * @param objects map from object name to object
   */
  void createObjects(Map<String, ? extends T> objects, Iterable<String> path);

  /**
   * Create and stores the given objects together in the same path.
   *
   * @param objects list of objects that will be places in the same path
   */
  void createObjectList(List<? extends T> objects, String... path);

  /**
   * Create and stores the given objects together in the same path.
   *
   * @param objects list of objects that will be places in the same path
   */
  void createObjectList(List<? extends T> objects, Iterable<String> path);

  /**
   * Lookup at an arbitrary object by a unique path.
   */
  @Nullable
  <U extends T> U getObject(Class<U> klass, String... path);

  /**
   * Lookup at an arbitrary object by a unique path.
   */
  <U extends T> U getObject(Class<U> klass, Iterable<String> path);

  /**
   * Lookup a list of arbitrary objects in a path.
   */
  <U extends T> List<U> getObjectList(Class<U> klass, String... path);

  /**
   * Lookup a list of arbitrary objects in a path.
   */
  <U extends T> List<U> getObjectList(Class<U> klass, Iterable<String> path);

  /**
   * Update an object in some path.
   */
  void updateObject(T updatedObject, String... path);

  /**
   * Update an object in some path.
   */
  void updateObject(T updatedObject, Iterable<String> path);

  /**
   * Update an arbitrary amount of objects in some path.
   */
  void updateObjects(Map<String, ? extends T> updatedObjects, String... path);

  /**
   * Update an arbitrary amount of objects in some path.
   */
  void updateObjects(Map<String, ? extends T> updatedObjects, Iterable<String> path);

  /**
   * Delete an object from some path.
   */
  void deleteObject(String... path);

  /**
   * Delete an object from some path.
   */
  void deleteObject(Iterable<String> path);

  /**
   * Delete a list of objects from the given path.
   */
  void deleteObjects(Iterable<String> objectNames, String... path);

  /**
   * Delete a list of objects from the given path.
   */
  void deleteObjects(Iterable<String> objectNames, Iterable<String> path);

  /**
   * Delete all the object in some path.
   */
  void deleteAllObjects(String... path);

  /**
   * Delete all the object in some path.
   */
  void deleteAllObjects(Iterable<String> path);

  /**
   * Find all the objects in a path that match a condition.
   */
  <U extends T> Iterable<U> findAll(Class<U> klass, String condition, String... path);

  /**
   * Find all the objects in a path that match a condition.
   */
  <U extends T> Iterable<U> findAll(Class<U> klass, String condition, Iterable<String> path);
}
