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

import com.google.api.services.compute.model.AccessConfig;
import com.google.api.services.compute.model.Instance;
import com.google.api.services.compute.model.Metadata;
import com.google.api.services.compute.model.Metadata.Items;
import com.google.api.services.compute.model.NetworkInterface;
import com.google.api.services.compute.model.ServiceAccount;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.openbidder.cloudstorage.impl.GoogleCloudStorageUtil;
import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.project.ProjectUser;
import com.google.openbidder.ui.resource.support.ResourceId;
import com.google.openbidder.util.Clock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Base class for bidder and load balancer {@link Instance} builders.
 */
public abstract class InstanceBuilder {

  private static final Logger logger = LoggerFactory.getLogger(InstanceBuilder.class);

  private static final int MAX_BOOTSTRAP_SCRIPT_SIZE = 15000;
  private static final String ACCESS_CONFIG_ONE_TO_ONE_NAT = "ONE_TO_ONE_NAT";
  // Google Compute accounts currently only have one, hard-coded, associated service account.
  private static final String DEFAULT_SERVICE_ACCOUNT = "default";

  public static final String IMAGE_DEFAULT = "default";
  public static final String IMAGE_CUSTOM = "custom";

  // Common metadata.
  public static final String METADATA_PROJECT_VERSION = "project_version";
  public static final String METADATA_USER_DIST = "user_dist_uri";
  public static final String METADATA_ZONE_HOST = "zone_host";
  public static final String METADATA_STARTUP_SCRIPT = "startup-script";
  public static final String METADATA_IMAGE = "image";
  public static final String METADATA_IMAGE_TYPE = "image_type";

  private final Clock clock;
  private final String projectVersion;
  private final String bootstrapScript;

  protected InstanceBuilder(Clock clock, String projectVersion, String bootstrapScript) {
    this.clock = clock;
    this.projectVersion = projectVersion;
    this.bootstrapScript = bootstrapScript;

    Preconditions.checkState(
        bootstrapScript.getBytes(Charsets.UTF_8).length <= MAX_BOOTSTRAP_SCRIPT_SIZE,
        String.format("Bootstrap script cannot be over %d bytes",
            MAX_BOOTSTRAP_SCRIPT_SIZE));
  }

  protected ImmutableMap.Builder<String, String> createMetadata(
      Project project, ResourceId zoneId) {
    ImmutableMap.Builder<String, String> metadata = ImmutableMap.<String, String>builder()
      .put(METADATA_PROJECT_VERSION, projectVersion)
      .put(METADATA_STARTUP_SCRIPT, bootstrapScript);

    String zoneHost = project.getZoneHost(zoneId.getResourceName());
    if (!Strings.isNullOrEmpty(zoneHost)) {
      metadata.put(METADATA_ZONE_HOST, zoneHost);
    }
    String uri = GoogleCloudStorageUtil.cleanupBucketUri(project.getUserDistUri());
    if (!Strings.isNullOrEmpty(uri)) {
      metadata.put(METADATA_USER_DIST, uri);
    }
    return metadata;
  }

  /**
   * @return {@link List} of {@link NetworkInterface} for an instance and {@link Project}
   */
  protected ImmutableList<NetworkInterface> buildInstanceNetwork(Project project,
      boolean addExternalInterface) {
    NetworkInterface network = new NetworkInterface();
    ResourceName networkName = ComputeResourceType.NETWORK.buildName(
        project, project.getNetworkName());
    network.setNetwork(networkName.getResourceUrl());
    if (addExternalInterface) {
      AccessConfig accessConfigs = new AccessConfig();
      accessConfigs.setType(ACCESS_CONFIG_ONE_TO_ONE_NAT);
      network.setAccessConfigs(ImmutableList.of(accessConfigs));
    }
    return ImmutableList.of(network);
  }

  /**
   * @return {@link com.google.api.services.compute.model.ServiceAccount} for a list of
   *          Oauth2 scopes
   */
  protected ImmutableList<ServiceAccount> buildServiceAccounts(List<String> scopes) {
    ServiceAccount serviceAccounts = new ServiceAccount();
    serviceAccounts.setEmail(DEFAULT_SERVICE_ACCOUNT);
    serviceAccounts.setScopes(scopes);
    return ImmutableList.of(serviceAccounts);
  }

  protected Metadata buildMetadata(Map<String, String> entries) {
    List<Items> items = new ArrayList<>();
    for (Entry<String, String> entry : entries.entrySet()) {
      items.add(new Items().setKey(entry.getKey()).setValue(entry.getValue()));
    }
    return new Metadata().setItems(items);
  }

  /**
   * @return An instance description
   */
  protected String buildDescription(ProjectUser projectUser) {
    return String.format("Started by %s at %s", projectUser.getEmail(), clock.now().toDateTime());
  }

  static String readScriptResource(Resource resource) {
    try (InputStream is = resource.getInputStream()) {
      BufferedReader br = new BufferedReader(new InputStreamReader(is));
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line).append('\n'); // Force Unix terminator
      }
      logger.info("Read bootstrap script {}", resource.getFilename());
      return sb.toString();
    } catch (IOException e) {
      throw new IllegalStateException("Cannot read bootstrap script", e);
    }
  }
}
