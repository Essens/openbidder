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

package com.google.openbidder.client;

import com.google.inject.AbstractModule;
import com.google.openbidder.config.client.ClientLogging;
import com.google.openbidder.config.client.ConnectionTimeout;
import com.google.openbidder.config.client.Uri;
import com.google.openbidder.config.server.MaxContentLength;
import com.google.openbidder.flags.util.UriConverter;
import com.google.openbidder.flags.util.UriValidator;
import com.google.openbidder.http.util.HttpUtil;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * Open Bidder client configuration.
 */
@Parameters(separators = "=")
public class ClientModule extends AbstractModule {

  private static final Logger logger = LoggerFactory.getLogger(ClientModule.class);

  @Parameter(names = "--url",
      description = "Connection URL",
      validateWith = UriValidator.class,
      converter = UriConverter.class)
  private URI url = HttpUtil.buildUri(Uri.DEFAULT);

  @Parameter(names = "--connection_timeout", description = "Connection timeout in milliseconds")
  private int connectionTimeout = ConnectionTimeout.DEFAULT;

  @Parameter(names = "--max_content_length", description = "Maximum content length in bytes")
  private int maxContentLength = MaxContentLength.DEFAULT;

  @Parameter(names = "--client_logging", arity = 1,
      description = "Enable detailed client logging")
  private boolean clientLogging;

  @Override
  protected void configure() {
    logger.info("Connection URL: {}, timeout: {} ms", url, connectionTimeout);
    logger.info("Maximum content length: {}", maxContentLength);
    logger.info("Client logging: {}", clientLogging);
    bind(URI.class).annotatedWith(Uri.class).toInstance(url);
    bind(int.class).annotatedWith(ConnectionTimeout.class).toInstance(connectionTimeout);
    bind(long.class).annotatedWith(ConnectionTimeout.class).toInstance((long) connectionTimeout);
    bind(int.class).annotatedWith(MaxContentLength.class).toInstance(maxContentLength);
    bind(long.class).annotatedWith(MaxContentLength.class).toInstance((long) maxContentLength);
    bind(boolean.class).annotatedWith(ClientLogging.class).toInstance(clientLogging);
  }
}
