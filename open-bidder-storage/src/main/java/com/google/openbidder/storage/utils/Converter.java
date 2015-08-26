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

package com.google.openbidder.storage.utils;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * A converter utility that handles object serialization and deserailization.
 */
public interface Converter<T> {
  /**
   * Deserialize an input stream and return an object.
   */
  <U extends T> U deserialize(Class<U> klass, InputStream in);

  /**
   * Deserialize a Byte Buffer and return an object.
   */
  <U extends T> U deserialize(Class<U> klass, ByteBuffer in);

  /**
   * Deserialize a list of delimited objects.
   */
  <U extends T> List<U> deserializeList(Class<U> klass, InputStream in);

  /**
   * Serialize an object.
   */
  ByteBuffer serialize(T obj);

  /**
   * Serialize a list of delimited objects.
   */
  ByteBuffer serializeList(List<? extends T> obj);
}
