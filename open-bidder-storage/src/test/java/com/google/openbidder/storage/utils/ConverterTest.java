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

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.openbidder.storage.model.StorageTestModel;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Unit tests for {@link Converter}
 */
public class ConverterTest {
  private ProtobufConverter converter;

  @Before
  public void setUp() {
    converter = new ProtobufConverter();
  }

  @Test
  public void serializeDeserialize_protobuf_sameValue() {
    StorageTestModel.FirstMessage entry = StorageTestModel.FirstMessage.newBuilder()
        .setFirstField("first field")
        .addSecondField("some data")
        .setThirdField(true)
        .build();

    assertEquals(ByteBuffer.wrap(entry.toByteArray()), converter.serialize(entry));

    ByteBuffer serializedAction = converter.serialize(entry);
    InputStream inputStream = new ByteArrayInputStream(serializedAction.array());
    StorageTestModel.FirstMessage deserializedEntry =
        converter.deserialize(StorageTestModel.FirstMessage.class, inputStream);

    assertEquals(entry, deserializedEntry);
  }

  @Test
  public void serializeDeserializeList_protobuf_sameValue() {
    StorageTestModel.FirstMessage entryA = StorageTestModel.FirstMessage.newBuilder()
        .setFirstField("first field")
        .addSecondField("some data")
        .setThirdField(true)
        .build();

    StorageTestModel.FirstMessage entryB = StorageTestModel.FirstMessage.newBuilder()
        .setFirstField("first field")
        .addSecondField("some other data")
        .setThirdField(false)
        .build();

    List<StorageTestModel.FirstMessage> entries = ImmutableList.of(entryA, entryB);

    ByteBuffer serializedList = converter.serializeList(entries);
    InputStream inputStream = new ByteArrayInputStream(serializedList.array());
    List<StorageTestModel.FirstMessage> deserializedList =
        converter.deserializeList(StorageTestModel.FirstMessage.class, inputStream);

    assertEquals(entries, deserializedList);
  }
}
