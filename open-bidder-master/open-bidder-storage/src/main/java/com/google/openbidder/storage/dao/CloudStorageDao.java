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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpStatusCodes;
import com.google.openbidder.cloudstorage.GoogleCloudStorage;
import com.google.openbidder.cloudstorage.GoogleCloudStorageException;
import com.google.openbidder.cloudstorage.StorageObject;
import com.google.openbidder.cloudstorage.model.ListBucketResult;
import com.google.openbidder.storage.utils.Converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * A generalized DAO that handles communication with Cloud Storage.
 */
public class CloudStorageDao<T> extends AbstractDao<T> {
  private static final Logger logger = LoggerFactory.getLogger(CloudStorageDao.class);

  private static final String PATH_ONE_VALUE = "path should be: bucketName";
  private static final String PATH_TWO_VALUES = "path should be: bucketName, objectName";

  private GoogleCloudStorage cloudStorage;
  private Converter<T> converter;

  @Inject
  public CloudStorageDao(GoogleCloudStorage cloudStorage, Converter<T> converter) {
    this.cloudStorage = checkNotNull(cloudStorage);
    this.converter = checkNotNull(converter);
  }

  @Override
  public void createObject(T object, String... path) {
    if (path.length == 2) {
      String bucketName = path[0];
      String objectName = path[1];
      byte[] objectAsBytes = converter.serialize(object).array();

      storeObject(bucketName, objectName, ByteBuffer.wrap(objectAsBytes));
    } else {
      throw new InvalidPathException(PATH_TWO_VALUES);
    }
  }

  @Override
  public void createObjectList(List<? extends T> objects, String... path) {
    if (path.length == 2) {
      String bucketName = path[0];
      String objectName = path[1];
      byte[] objectAsBytes = converter.serializeList(objects).array();

      storeObject(bucketName, objectName, ByteBuffer.wrap(objectAsBytes));
    } else {
      throw new InvalidPathException(PATH_TWO_VALUES);
    }
  }

  @Override
  public void createObjects(Map<String, ? extends T> objects, String... path) {
    if (path.length == 1) {
      String bucketName = path[0];

      for (Map.Entry<String, ? extends T> entry : objects.entrySet()) {
        createObject(entry.getValue(), bucketName, entry.getKey());
      }
    } else {
      throw new InvalidPathException(PATH_ONE_VALUE);
    }
  }

  @Override
  public @Nullable <U extends T> U getObject(Class<U> klass, String... path) {
    if (path.length == 2) {
      String bucketName = path[0];
      String objectName = path[1];
      StorageObject storageObject = getStorageObject(bucketName, objectName);

      if (storageObject != null) {
        return converter.deserialize(klass, storageObject.getInputStream());
      }
    } else {
      throw new InvalidPathException(PATH_TWO_VALUES);
    }

    return null;
  }

  @Override
  public <U extends T> List<U> getObjectList(Class<U> klass, String... path) {
    if (path.length == 2) {
      String bucketName = path[0];
      String objectName = path[1];
      StorageObject storageObject = getStorageObject(bucketName, objectName);

      if (storageObject != null) {
        return converter.deserializeList(klass, storageObject.getInputStream());
      }
    } else {
      throw new InvalidPathException(PATH_TWO_VALUES);
    }

    return new ArrayList<>();
  }

  @Override
  public void updateObject(T updatedObject, String... path) {
    if (path.length == 2) {
      String bucketName = path[0];
      String objectName = path[1];

      deleteObject(bucketName, objectName);
      createObject(updatedObject, bucketName, objectName);
    } else {
      throw new InvalidPathException(PATH_TWO_VALUES);
    }
  }

  @Override
  public void updateObjects(Map<String, ? extends T> updatedObjects, String... path) {
    if (path.length == 1) {
      String bucketName = path[0];

      for (Map.Entry<String, ? extends T> entry : updatedObjects.entrySet()) {
        updateObject(entry.getValue(), bucketName, entry.getKey());
      }
    } else {
      throw new InvalidPathException(PATH_ONE_VALUE);
    }
  }

  @Override
  public void deleteObject(String... path) {
    if (path.length == 2) {
      String bucketName = path[0];
      String objectName = path[1];

      try {
        cloudStorage.removeObject(bucketName, objectName);
      } catch (HttpResponseException e) {
        logger.info("Failed to delete object from {}: {}", bucketName, e.getMessage());
      }
    } else {
      throw new InvalidPathException(PATH_TWO_VALUES);
    }
  }

  @Override
  public void deleteObjects(Iterable<String> objectNames, String... path) {
    if (path.length == 1) {
      String bucketName = path[0];

      for (String objectName : objectNames) {
        deleteObject(bucketName, objectName);
      }
    } else {
      throw new InvalidPathException(PATH_ONE_VALUE);
    }
  }

  @Override
  public void deleteAllObjects(String... path) {
    if (path.length == 1) {
      String bucketName = path[0];

      try {
        ListBucketResult bucketList = cloudStorage.listObjectsInBucket(bucketName, null);
        for (ListBucketResult.Content content : bucketList.getContents()) {
          deleteObject(bucketName, content.getKey());
        }
      } catch (HttpResponseException e) {
        throw new GoogleCloudStorageException(e);
      }
    } else {
      throw new InvalidPathException(PATH_ONE_VALUE);
    }
  }

  @Override
  public <U extends T> Iterable<U> findAll(Class<U> klass, String condition, String... path) {
    List<U> objects = new ArrayList<>();

    if (path.length == 1) {
      String bucketName = path[0];

      try {
        ListBucketResult bucketList = cloudStorage.listObjectsInBucket(bucketName, condition);
        for (ListBucketResult.Content content : bucketList.getContents()) {
          objects.add(getObject(klass, bucketName, content.getKey()));
        }
      } catch (HttpResponseException e) {
        throw new GoogleCloudStorageException(e);
      }
    } else {
      throw new InvalidPathException(PATH_ONE_VALUE);
    }
    return objects;
  }

  private @Nullable StorageObject getStorageObject(String bucketName, String objectName) {
    StorageObject storageObject = null;
    try {
      storageObject = cloudStorage.getObject(bucketName, objectName, null);
    } catch (HttpResponseException e) {
      if (e.getStatusCode() != HttpStatusCodes.STATUS_CODE_NOT_FOUND) {
        throw new GoogleCloudStorageException(e);
      }
    }
    return storageObject;
  }

  private void storeObject(String bucketName, String objectName, ByteBuffer object) {
    try {
      cloudStorage.putObject(
          bucketName,
          objectName,
          new ByteArrayContent("binary/octet-stream", object.array()),
           /* custom metadata */ null);
    } catch (HttpResponseException e) {
      throw new GoogleCloudStorageException(e);
    }
  }
}
