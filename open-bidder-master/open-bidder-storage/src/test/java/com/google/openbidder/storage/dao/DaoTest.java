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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.api.client.http.HttpResponseException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.openbidder.cloudstorage.GoogleCloudStorage;
import com.google.openbidder.cloudstorage.testing.FakeGoogleCloudStorage;
import com.google.openbidder.storage.model.StorageTestModel;
import com.google.openbidder.storage.utils.ProtobufConverter;
import com.google.openbidder.util.testing.FakeClock;
import com.google.protobuf.MessageLite;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Unit tests for {@link Dao}
 */
public class DaoTest {
  private Dao<MessageLite> dao;
  private ProtobufConverter converter;

  @Before
  public void setUp() throws HttpResponseException {
    GoogleCloudStorage cloudStorage = new FakeGoogleCloudStorage(new FakeClock());
    cloudStorage.putBucket("fake-bucket");
    converter = new ProtobufConverter();
    dao = new CloudStorageDao<>(cloudStorage, converter);
  }

  @Test
  public void createObject_object_objectStored() {
    StorageTestModel.FirstMessage entry = StorageTestModel.FirstMessage.newBuilder()
        .setFirstField("first field")
        .addSecondField("some data")
        .setThirdField(true)
        .build();

    dao.createObject(entry, "fake-bucket", "ob1");

    assertEquals(entry, dao.getObject(StorageTestModel.FirstMessage.class, "fake-bucket", "ob1"));
    assertNotEquals(
        entry,
        dao.getObject(StorageTestModel.SecondMessage.class, "fake-bucket", "ob1"));
    assertNotEquals(
        entry,
        dao.getObject(StorageTestModel.FirstMessage.class, "fake-bucket", "ob2"));
  }

  @Test
  public void createObjectList_objectList_objectListStored() {
    StorageTestModel.FirstMessage entryA = StorageTestModel.FirstMessage.newBuilder()
        .setFirstField("first field")
        .addSecondField("some data")
        .setThirdField(true)
        .build();

    StorageTestModel.FirstMessage entryB = StorageTestModel.FirstMessage.newBuilder()
        .setFirstField("some other field")
        .addSecondField("some other data")
        .setThirdField(false)
        .build();

    List<StorageTestModel.FirstMessage> entries = ImmutableList.of(entryA, entryB);

    dao.createObjectList(entries, "fake-bucket", "ob");

    assertEquals(entries, dao.getObjectList(StorageTestModel.FirstMessage.class, "fake-bucket", "ob"));
    assertNotEquals(
        entries, dao.getObjectList(StorageTestModel.FirstMessage.class, "fake-bucket", "ob1"));
    assertNotEquals(
        entries, dao.getObjectList(StorageTestModel.FirstMessage.class, "fake-bucket", "ob2"));
  }

  @Test
  public void createObjects_objects_objectsStored() {
    StorageTestModel.FirstMessage entryA = StorageTestModel.FirstMessage.newBuilder()
        .setFirstField("first field")
        .addSecondField("some data")
        .setThirdField(true)
        .build();

    StorageTestModel.FirstMessage entryB = StorageTestModel.FirstMessage.newBuilder()
        .setFirstField("some other field")
        .addSecondField("some other data")
        .setThirdField(false)
        .build();

    dao.createObjects(ImmutableMap.of(
        entryA.getFirstField(), entryA,
        entryB.getFirstField(), entryB),
        "fake-bucket");

    assertEquals(
        entryA,
        dao.getObject(StorageTestModel.FirstMessage.class, "fake-bucket", entryA.getFirstField()));
    assertEquals(
        entryB,
        dao.getObject(StorageTestModel.FirstMessage.class, "fake-bucket", entryB.getFirstField()));

    assertNotEquals(
        entryA,
        dao.getObject(StorageTestModel.SecondMessage.class, "fake-bucket", entryA.getFirstField()));
    assertNotEquals(
        entryA,
        dao.getObject(StorageTestModel.FirstMessage.class, "fake-bucket", entryB.getFirstField()));
  }

  @Test
  public void updateObject_object_objectUpdated() {
    StorageTestModel.FirstMessage entryA = StorageTestModel.FirstMessage.newBuilder()
        .setFirstField("first field")
        .addSecondField("some data")
        .setThirdField(true)
        .build();

    StorageTestModel.FirstMessage entryB = StorageTestModel.FirstMessage.newBuilder()
        .setFirstField("some other field")
        .addSecondField("some other data")
        .setThirdField(false)
        .build();

    dao.createObject(entryA, "fake-bucket", "ob");
    dao.updateObject(entryB, "fake-bucket", "ob");

    assertEquals(entryB, dao.getObject(StorageTestModel.FirstMessage.class, "fake-bucket", "ob"));
    assertNotEquals(
        entryA, dao.getObject(StorageTestModel.FirstMessage.class, "fake-bucket", "ob"));
  }

  @Test
  public void updateObjects_objects_objectsUpdated() {
    StorageTestModel.FirstMessage entryA = StorageTestModel.FirstMessage.newBuilder()
        .setFirstField("first field")
        .addSecondField("some data")
        .setThirdField(true)
        .build();

    StorageTestModel.FirstMessage entryB = StorageTestModel.FirstMessage.newBuilder()
        .setFirstField("some other field")
        .addSecondField("some other data")
        .setThirdField(false)
        .build();

    StorageTestModel.FirstMessage entryC = StorageTestModel.FirstMessage.newBuilder()
        .setFirstField("another field")
        .addSecondField("some data")
        .setThirdField(true)
        .build();

    dao.createObject(entryA, "fake-bucket", entryA.getFirstField());
    dao.createObject(entryB, "fake-bucket", entryB.getFirstField());
    dao.createObject(entryC, "fake-bucket", entryC.getFirstField());

    ImmutableMap<String, StorageTestModel.FirstMessage> updatedActions = ImmutableMap.of(
        entryA.getFirstField(), entryB,
        entryB.getFirstField(), entryC);

    dao.updateObjects(updatedActions, "fake-bucket");

    assertEquals(
        entryB,
        dao.getObject(StorageTestModel.FirstMessage.class, "fake-bucket", entryA.getFirstField()));
    assertEquals(
        entryC,
        dao.getObject(StorageTestModel.FirstMessage.class, "fake-bucket", entryB.getFirstField()));
    assertEquals(
        entryC,
        dao.getObject(StorageTestModel.FirstMessage.class, "fake-bucket",entryC.getFirstField()));
  }

  @Test
  public void deleteObject_object_objectDeleted() {
    StorageTestModel.FirstMessage entry = StorageTestModel.FirstMessage.newBuilder()
        .setFirstField("first field")
        .addSecondField("some data")
        .setThirdField(true)
        .build();

    dao.createObject(entry, "fake-bucket", "ob1");
    assertEquals(entry, dao.getObject(StorageTestModel.FirstMessage.class, "fake-bucket", "ob1"));

    dao.deleteObject("fake-bucket", "ob1");
    assertNull(dao.getObject(StorageTestModel.FirstMessage.class, "fake-bucket", "ob1"));
  }

  @Test
  public void deleteObjects_objects_objectsDeleted() {
    StorageTestModel.FirstMessage entryA = StorageTestModel.FirstMessage.newBuilder()
        .setFirstField("first field")
        .addSecondField("some data")
        .setThirdField(true)
        .build();

    StorageTestModel.FirstMessage entryB = StorageTestModel.FirstMessage.newBuilder()
        .setFirstField("some other field")
        .addSecondField("some other data")
        .setThirdField(false)
        .build();

    StorageTestModel.FirstMessage entryC = StorageTestModel.FirstMessage.newBuilder()
        .setFirstField("another field")
        .addSecondField("some data")
        .setThirdField(true)
        .build();

    dao.createObject(entryA, "fake-bucket", entryA.getFirstField());
    dao.createObject(entryB, "fake-bucket", entryB.getFirstField());
    dao.createObject(entryC, "fake-bucket", entryC.getFirstField());

    List<String> toDelete = ImmutableList.of(entryA.getFirstField(), entryC.getFirstField());

    dao.deleteObjects(toDelete, "fake-bucket");

    assertNull(
        dao.getObject(StorageTestModel.FirstMessage.class, "fake-bucket", entryA.getFirstField()));
    assertNull(
        dao.getObject(StorageTestModel.FirstMessage.class, "fake-bucket", entryC.getFirstField()));
    assertEquals(
        entryB,
        dao.getObject(StorageTestModel.FirstMessage.class, "fake-bucket", entryB.getFirstField()));
  }

  @Test
  public void deleteAllObjects_objects_objectsDeleted() {
    StorageTestModel.FirstMessage entryA = StorageTestModel.FirstMessage.newBuilder()
        .setFirstField("first field")
        .addSecondField("some data")
        .setThirdField(true)
        .build();

    StorageTestModel.FirstMessage entryB = StorageTestModel.FirstMessage.newBuilder()
        .setFirstField("some other field")
        .addSecondField("some other data")
        .setThirdField(false)
        .build();

    StorageTestModel.FirstMessage entryC = StorageTestModel.FirstMessage.newBuilder()
        .setFirstField("another field")
        .addSecondField("some data")
        .setThirdField(true)
        .build();

    dao.createObject(entryA, "fake-bucket", entryA.getFirstField());
    dao.createObject(entryB, "fake-bucket", entryB.getFirstField());
    dao.createObject(entryC, "fake-bucket", entryC.getFirstField());

    dao.deleteAllObjects("fake-bucket");

    assertNull(
        dao.getObject(StorageTestModel.FirstMessage.class, "fake-bucket", entryA.getFirstField()));
    assertNull(
        dao.getObject(StorageTestModel.FirstMessage.class, "fake-bucket", entryB.getFirstField()));
    assertNull(
        dao.getObject(StorageTestModel.FirstMessage.class, "fake-bucket", entryC.getFirstField()));
  }

  @Test
  public void findAllObjects_objects_objectsFound() {
    StorageTestModel.FirstMessage entryA = StorageTestModel.FirstMessage.newBuilder()
        .setFirstField("t-001")
        .addSecondField("some data")
        .setThirdField(true)
        .build();

    StorageTestModel.FirstMessage entryB = StorageTestModel.FirstMessage.newBuilder()
        .setFirstField("t-002")
        .addSecondField("some other data")
        .setThirdField(false)
        .build();

    StorageTestModel.FirstMessage entryC = StorageTestModel.FirstMessage.newBuilder()
        .setFirstField("t-003")
        .addSecondField("some data")
        .setThirdField(true)
        .build();

    dao.createObject(entryA, "fake-bucket", entryA.getFirstField());
    dao.createObject(entryB, "fake-bucket", entryB.getFirstField());
    dao.createObject(entryC, "fake-bucket", entryC.getFirstField());

    assertEquals(
        ImmutableList.of(entryA, entryB, entryC),
        ImmutableList.copyOf(dao.findAll(StorageTestModel.FirstMessage.class, "t-", "fake-bucket")));
    assertTrue(Iterables.isEmpty(
        dao.findAll(StorageTestModel.FirstMessage.class, "anot-", "fake-bucket")));
  }

  @Test(expected=InvalidPathException.class)
  public void invalidPath_noPath_exceptionThrown(){
    StorageTestModel.FirstMessage entry = StorageTestModel.FirstMessage.newBuilder()
        .setFirstField("t-001")
        .addSecondField("some data")
        .setThirdField(true)
        .build();

    dao.createObject(entry);
  }

  @Test(expected=InvalidPathException.class)
  public void invalidPath_Path_exceptionThrown(){
    StorageTestModel.FirstMessage entry = StorageTestModel.FirstMessage.newBuilder()
        .setFirstField("t-001")
        .addSecondField("some data")
        .setThirdField(true)
        .build();

    dao.createObject(entry, "fake-bucket", "some-object", "tooMany");
  }
}
