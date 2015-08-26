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

package com.google.openbidder.exchange.doubleclick.interceptor;

import com.google.inject.AbstractModule;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Standard DoubleClick interceptors.
 */
@Parameters(separators = "=")
public class DoubleClickInterceptorsModule extends AbstractModule {
  private final Logger logger = LoggerFactory.getLogger(DoubleClickInterceptorsModule.class);

  @Parameter(names = "--doubleclick_dumpfile",
      description = "Filename to dump native BidRequest messages")
  private String dumpFilename;

  @Override
  protected void configure() {
    if (dumpFilename != null) {
      File file = new File(dumpFilename);
      try {
        logger.info("Dumping requests to file: {}", file.getCanonicalPath());
      } catch (IOException e) {
      }
      bind(File.class).annotatedWith(BidRequestDumpInterceptor.DumpFile.class).toInstance(file);
    }
  }
}
