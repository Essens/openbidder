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

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * A converter utility that handles protocol buffer serialization and deserialization.
 * <p>
 * This converter works with any protobuf {@link MessageLite} that implements the methods:
 * <code>parseFrom(InputStream)</code>, <code>parseFrom(ByteBuffer)</code>,
 * and <code>parseDelimitedFrom(InputStream)</code>. Any protoc-generated message
 * ({@link com.google.protobuf.MessageLite}) will implement these methods.
 */
public class ProtobufConverter implements Converter<MessageLite> {

  @Override
  public <M extends MessageLite> M deserialize(Class<M> klass, InputStream in) {
    try {
      Method method = klass.getMethod("parseFrom", InputStream.class);
      @SuppressWarnings("unchecked")
      M result = (M) method.invoke(null, in);
      return result;
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Error parsing proto", e);
    }
  }

  @Override
  public <M extends MessageLite> M deserialize(Class<M> klass, ByteBuffer in) {
    try {
      Method method = klass.getMethod("parseFrom", ByteString.class);
      @SuppressWarnings("unchecked")
      M result = (M) method.invoke(null, ByteString.copyFrom(in));
      return result;
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Error parsing proto", e);
    }
  }

  @Override
  public <M extends MessageLite> List<M> deserializeList(Class<M> klass, InputStream in) {
    try {
      List<M> result = new ArrayList<>();
      Method method = klass.getMethod("parseDelimitedFrom", InputStream.class);
      Object obj;
      while ((obj = method.invoke(null, in)) != null) {
        @SuppressWarnings("unchecked")
        M entry = (M) obj;
        result.add(entry);
      }
      return result;
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Error parsing delimited proto", e);
    }
  }

  @Override
  public ByteBuffer serialize(MessageLite obj) {
    return ByteBuffer.wrap(obj.toByteArray());
  }

  @Override
  public ByteBuffer serializeList(List<? extends MessageLite> objects) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    try {
      for (MessageLite msg : objects) {
        msg.writeDelimitedTo(out);
      }
    } catch (IOException e) {
      throw new IllegalStateException("Error writing delimited proto", e);
    }

    return ByteBuffer.wrap(out.toByteArray());
  }
}