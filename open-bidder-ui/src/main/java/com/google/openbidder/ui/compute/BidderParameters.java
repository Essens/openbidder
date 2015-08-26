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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.openbidder.config.server.BidderAdminPort;
import com.google.openbidder.config.server.BidderListenPort;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Parameters for launching a bidder.
 */
@Named
public class BidderParameters {

  private final String defaultJvmParameters;
  private final String defaultMainParameters;
  private final ImmutableList<String> defaultBidInterceptors;
  private final ImmutableList<String> defaultImpressionInterceptors;
  private final ImmutableList<String> defaultClickInterceptors;
  private final ImmutableList<String> defaultMatchInterceptors;
  private final ImmutableList<String> defaultOauth2Scopes;
  private final boolean enableExternalIps;
  private final Resource bootstrapScript;
  private final String requestPort;
  private final String adminPort;
  private final String dcMatchUrl;

  @Inject
  public BidderParameters(
      @Value("${Bidder.DefaultJVMParameters}") String defaultJvmParameters,
      @Value("${Bidder.DefaultMainParameters}") String defaultMainParameters,
      @Value("#{defaultBidInterceptors}") Iterable<String> defaultBidInterceptors,
      @Value("#{defaultImpressionInterceptors}") Iterable<String> defaultImpressionInterceptors,
      @Value("#{defaultClickInterceptors}") Iterable<String> defaultClickInterceptors,
      @Value("#{defaultMatchInterceptors}") Iterable<String> defaultMatchInterceptors,
      @Value("#{defaultBidderScopes}") Iterable<String> defaultOauth2Scopes,
      @Value("${Bidder.EnableExternalIps}") Boolean enableExternaBidderlIps,
      @Value("${Bidder.BootstrapScript}") Resource bidderBootstrapScript,
      @Value("${Match.DoubleClick.RedirectUrl}") String dcMatchUrl) {

    this.defaultJvmParameters = checkNotNull(defaultJvmParameters);
    this.defaultMainParameters = checkNotNull(defaultMainParameters);
    this.defaultBidInterceptors = ImmutableList.copyOf(defaultBidInterceptors);
    this.defaultImpressionInterceptors = ImmutableList.copyOf(defaultImpressionInterceptors);
    this.defaultClickInterceptors = ImmutableList.copyOf(defaultClickInterceptors);
    this.defaultMatchInterceptors = ImmutableList.copyOf(defaultMatchInterceptors);
    this.defaultOauth2Scopes = ImmutableList.copyOf(defaultOauth2Scopes);
    this.enableExternalIps = checkNotNull(enableExternaBidderlIps);
    this.bootstrapScript = checkNotNull(bidderBootstrapScript);
    this.requestPort = String.valueOf(BidderListenPort.DEFAULT);
    this.adminPort = String.valueOf(BidderAdminPort.DEFAULT);
    this.dcMatchUrl = dcMatchUrl;
  }

  /**
   * @return Default JVM parameters passed to the bidder's process
   */
  public String getDefaultJvmParameters() {
    return defaultJvmParameters;
  }

  /**
   * @return Default main parameters passed to the bidder's process
   */
  public String getDefaultMainParameters() {
    return defaultMainParameters;
  }

  /**
   * @return Default fully-qualified names for bidder interceptors
   */
  public ImmutableList<String> getDefaultBidInterceptors() {
    return defaultBidInterceptors;
  }

  /**
   * @return Default fully-qualified names for impression tracking interceptors
   */
  public ImmutableList<String> getDefaultImpressionInterceptors() {
    return defaultImpressionInterceptors;
  }

  /**
   * @return Default fully-qualified names for click tracking interceptors
   */
  public ImmutableList<String> getDefaultClickInterceptors() {
    return defaultClickInterceptors;
  }

  /**
   * @return Default fully-qualified names for pixel-matching interceptors
   */
  public ImmutableList<String> getDefaultMatchInterceptors() {
    return defaultMatchInterceptors;
  }

  /**
   * @return Oauth2 scopes needed by the bidder instances.
   */
  public ImmutableList<String> getDefaultOauth2Scopes() {
    return defaultOauth2Scopes;
  }

  /**
   * @return Whether bidders should be assigned external ips.  Without an external IP, bidders
   *          are not reachable directly by ssh
   */
  public boolean isEnableExternalIps() {
    return enableExternalIps;
  }

  /**
   * @return Script to bootstrap bidders on Google Compute Engine
   */
  public Resource getBootstrapScript() {
    return bootstrapScript;
  }

  /**
   * @return Port the bidders listen for requests
   */
  public String getRequestPort() {
    return requestPort;
  }

  /**
   * @return Port the bidders listen for admin requests
   */
  public String getAdminPort() {
    return adminPort;
  }

  public String getDcMatchUrl() {
    return dcMatchUrl;
  }
}
