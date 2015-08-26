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

package com.google.openbidder.jetty;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.Service;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import javax.inject.Inject;

/**
 * Wrap an embedded Jetty service in a Guava {@link Service}.
 */
public class JettyServer extends AbstractIdleService {

  private static final Logger logger = LoggerFactory.getLogger(JettyServer.class);

  private final Server server;

  @Inject
  JettyServer(
      Server server,
      Set<Connector> connectorSet) {

    checkNotNull(connectorSet);
    checkArgument(!connectorSet.isEmpty());
    this.server = checkNotNull(server);
    for (Connector connector : connectorSet) {
      logger.info("Adding connector {}: {}", connector.getName(), connector);
    }
    server.setConnectors(connectorSet.toArray(new Connector[connectorSet.size()]));
  }

  @Override
  protected void startUp() throws Exception {
    logger.info("Starting WebService...");
    server.start();
    logger.info("WebService successfully started");
  }

  @Override
  protected void shutDown() throws Exception {
    logger.info("Stopping WebService...");
    server.stop();
    logger.info("WebService successfully stopped");
  }
}
