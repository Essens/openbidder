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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.googlecode.objectify.ObjectifyService.ofy;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.Work;

import java.lang.reflect.Constructor;

/**
 * Persistence related utilities.
 */
public class Db {

  private Db() {
  }

  public static <T> T safe(Key<?> key, T entity) {
    if (entity == null) {
      throw new NotFoundException(key);
    }
    return entity;
  }

  public static <T> T safe(T entity) {
    if (entity == null) {
      throw new NotFoundException();
    }
    return entity;
  }

  /**
   * Update a single entity within a transaction.
   */
  public static <F, T> T updateInTransaction(
      final Key<F> key,
      final Transactable<F, T> worker) {
    checkNotNull(key);
    checkNotNull(worker);
    return ofy().transact(new Work<T>() {
      @Override public T run() {
        F item = ofy().load().now(key);
        return item == null ? null : worker.work(item, ofy());
      }
    });
  }

  /**
   * Like
   * {@link #updateInTransaction(com.googlecode.objectify.Key, Transactable)}
   * but it will create the entity if necessary. This can be done when the key isn't
   * auto-generated.
   * <p>
   * Note: the {@code entityClass} must have a public constructor with a single {@link String}
   * argument being the key.
   */
  public static <F, T> T createOrUpdateInTransaction(
      final Class<F> entityClass,
      final String keyName,
      final Transactable<F, T> worker) {
    checkNotNull(entityClass);
    checkNotNull(keyName);
    checkNotNull(worker);
    final Key<F> key = Key.create(entityClass, keyName);
    return ofy().transact(new Work<T>() {
      @Override public T run() {
        F item = ofy().load().now(key);
        if (item == null) {
          try {
            Constructor<F> constructor = entityClass.getConstructor(String.class);
            item = constructor.newInstance(keyName);
          } catch (ReflectiveOperationException ee) {
            return null;
          }
        }
        return worker.work(item, ofy());
      }
    });
  }
}
