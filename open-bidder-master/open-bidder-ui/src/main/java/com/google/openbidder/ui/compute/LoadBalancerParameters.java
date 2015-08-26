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

package com.google.openbidder.ui.compute;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.openbidder.config.server.LoadBalancerPort;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Parameters for launching load balancer instances.
 */
@Named
public class LoadBalancerParameters {

  private final Resource bootstrapScript;
  private final ImmutableList<String> defaultOauth2Scopes;
  private final String statPort;
  private final String requestPort;

  @Inject
  public LoadBalancerParameters(
      @Value("${LoadBalancer.BootstrapScript}") Resource bootstrapScriptName,
      @Value("#{defaultLoadBalancerScopes}") Iterable<String> defaultOauth2Scopes,
      @Value("${LoadBalancer.StatPort}") String statPort) {

    this.bootstrapScript = Preconditions.checkNotNull(bootstrapScriptName);
    this.defaultOauth2Scopes = ImmutableList.copyOf(defaultOauth2Scopes);
    this.statPort = Preconditions.checkNotNull(statPort);
    this.requestPort = String.valueOf(LoadBalancerPort.DEFAULT);
  }

  /**
   * @return Script used to bootstrap Google Compute load balancers
   */
  public Resource getBootstrapScript() {
    return bootstrapScript;
  }

  /**
   * @return Oauth2 scopes needed by the load balancer instances.
   */
  public ImmutableList<String> getDefaultOauth2Scopes() {
    return defaultOauth2Scopes;
  }

  /**
   * @return Port number that HaProxy's stats page is running on.
   */
  public String getStatPort() {
    return statPort;
  }

  /**
   * @return Port number for bid requests.
   */
  public String getRequestPort() {
    return requestPort;
  }
}
