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

package com.google.openbidder.remarketing.services;

import com.google.common.base.Strings;
import com.google.inject.AbstractModule;
import com.google.openbidder.remarketing.services.impl.RemarketingServiceImpl;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Singleton;

/**
 * Bind a singleton remarketing service to its implementation.
 */
@Parameters(separators = "=")
public class RemarketingModule extends AbstractModule {

  @Parameter(names = "--remarketing_bucket",
      description = "The name of the Google Cloud Storage Bucket")
  private String bucketName;

  @Override
  public void configure() {
    if (!Strings.isNullOrEmpty(bucketName)) {
      bind(String.class).annotatedWith(RemarketingBucket.class).toInstance(bucketName);
      bind(ExecutorService.class).annotatedWith(RemarketingBucket.class)
          .toInstance(Executors.newFixedThreadPool(2));
      bind(RemarketingService.class).to(RemarketingServiceImpl.class).in(Singleton.class);
    }
  }
}
