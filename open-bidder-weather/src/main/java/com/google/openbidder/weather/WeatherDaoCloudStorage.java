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

package com.google.openbidder.weather;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.HttpResponseException;
import com.google.openbidder.cloudstorage.GoogleCloudStorage;
import com.google.openbidder.cloudstorage.StorageObject;
import com.google.openbidder.cloudstorage.model.ListBucketResult;
import com.google.openbidder.weather.model.Weather.WeatherRules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores Weather rules in Google Cloud Storage.  The storage scheme is simple: a single
 * bucket is used; it has one object per {@link WeatherRules}, where the object name is based
 * on the rules' ownerId and the value is its protobuf serialized form.
 */
public final class WeatherDaoCloudStorage implements WeatherDao {
  private static final Logger logger = LoggerFactory.getLogger(WeatherDaoCloudStorage.class);
  private final GoogleCloudStorage cloudStorage;
  private final String storageBucket;

  public WeatherDaoCloudStorage(GoogleCloudStorage cloudStorage, String storageBucket) {
    this.cloudStorage = cloudStorage;
    this.storageBucket = storageBucket;
  }

  @Override
  public void insert(WeatherRules rules) {
    try {
      cloudStorage.putObject(storageBucket, rules.getOwnerId(),
          new ByteArrayContent("binary/octet-stream", rules.toByteArray()), null);
    } catch (HttpResponseException e) {
      logger.error("Failed to insert weather rules to {}: {}", storageBucket, e.getMessage());
    }
  }

  @Override
  public void deleteRules(String ownerId) {
    try {
      cloudStorage.removeObject(storageBucket, ownerId);
    } catch (HttpResponseException e) {
      logger.error("Failed to delete weather rules from {}: {}", storageBucket, e.getMessage());
    }
  }

  @Override
  public List<WeatherRules> listRules() {
    List<WeatherRules> rules = new ArrayList<>();
    try {
      ListBucketResult items = cloudStorage.listObjectsInBucket(storageBucket, null);
      for (ListBucketResult.Content item : items.getContents()) {
        StorageObject rule = cloudStorage.getObject(storageBucket, item.getKey(), null);
        rules.add(WeatherRules.parseFrom(rule.getInputStream()));
      }
    } catch (IOException e) {
      logger.error("Failed to read weather rules", e);
    }
    return rules;
  }
}
