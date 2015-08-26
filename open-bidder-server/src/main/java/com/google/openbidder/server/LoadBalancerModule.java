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

package com.google.openbidder.server;

import com.google.inject.AbstractModule;
import com.google.openbidder.config.server.LoadBalancerHost;
import com.google.openbidder.config.server.LoadBalancerPort;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Load balancer HTTP server configuration.
 */
@Parameters(separators = "=")
public class LoadBalancerModule extends AbstractModule {
  private static final Logger logger = LoggerFactory.getLogger(LoadBalancerModule.class);

  @Parameter(names = "--load_balancer_host", description = "Load balancer hostname")
  private String loadBalancerHost;

  @Parameter(names = "--load_balancer_port", description = "Load balancer port")
  private int loadBalancerPort = LoadBalancerPort.DEFAULT;

  @Override
  protected void configure() {
    try {
      logger.info("Load balancer host: {}, port: {}", loadBalancerHost, loadBalancerPort);
      bind(String.class).annotatedWith(LoadBalancerHost.class).toInstance(loadBalancerHost == null
          ? InetAddress.getLocalHost().getHostName()
          : loadBalancerHost);
      bind(Integer.class).annotatedWith(LoadBalancerPort.class).toInstance(loadBalancerPort);
    } catch (UnknownHostException e) {
      throw new IllegalStateException(e);
    }
  }
}
