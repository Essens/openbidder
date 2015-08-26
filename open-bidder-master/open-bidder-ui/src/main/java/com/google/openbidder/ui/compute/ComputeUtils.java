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
import com.google.api.services.compute.model.Image;
import com.google.api.services.compute.model.Instance;
import com.google.api.services.compute.model.Metadata;
import com.google.api.services.compute.model.NetworkInterface;
import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.openbidder.ui.resource.support.ResourceId;
import com.google.openbidder.ui.resource.support.ResourceType;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

/**
 * Google Compute Engine related utilities.
 */
public class ComputeUtils {
  public static final Comparator<Image> IMAGE_COMPARATOR = new Comparator<Image>() {
    @Override public int compare(Image image1, Image image2) {
      int diff;
      if ((diff = image1.getName().compareTo(image2.getName())) != 0) {
        return diff;
      } else {
        return image2.getCreationTimestamp().compareTo(image1.getCreationTimestamp());
      }
  }};

  /**
   * @return a {@link java.util.List} of {@link Metadata} items into a {@link Map}
   */
  public static ImmutableMap<String, String> toMap(Metadata metadata) {
    ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
    if (metadata!= null && metadata.getItems() != null) {
      for (Metadata.Items items : metadata.getItems()) {
        builder.put(items.getKey(), items.getValue());
      }
    }
    return builder.build();
  }

  /**
   * @return Network resource URL for the {@link Instance} or {@code null} if none found.
   */
  @Nullable
  public static String getNetwork(Instance instance) {
    List<NetworkInterface> networkInterfaces = instance.getNetworkInterfaces();
    if (networkInterfaces == null || networkInterfaces.isEmpty()) {
      return null;
    }
    return networkInterfaces.get(0).getNetwork();
  }

  /**
   * @return The URI of the given resource translated from a Google Compute resource URL
   */
  public static String toResourceUri(
      long projectId,
      @Nullable String zoneName,
      ResourceType resourceType,
      String computeResourceUrl) {

    ResourceName resourceName = ResourceName.parseResource(computeResourceUrl);
    if (Strings.isNullOrEmpty(zoneName)) {
      return resourceType.getResourceId(
          Long.toString(projectId), resourceName.getResourceName()).getResourceUri();
    }
    return resourceType.getResourceId(
        Long.toString(projectId), zoneName, resourceName.getResourceName()).getResourceUri();
  }

  public static String toResourceUri(
      long projectId,
      ResourceType resourceType,
      String computeResourceUrl) {
    return toResourceUri(projectId, null, resourceType, computeResourceUrl);
  }

  /**
   * @return Internal IP for an instance or {@code null} if not found.
   */
  @Nullable
  public static String getInternalIp(Instance instance) {
    List<NetworkInterface> networkInterfaces = instance.getNetworkInterfaces();
    if (networkInterfaces == null || networkInterfaces.isEmpty()) {
      return null;
    }
    return networkInterfaces.get(0).getNetworkIP();
  }

  /**
   * @return External IP for an instance or {@code null} if not found.
   */
  @Nullable
  public static String getExternalIp(Instance instance) {
    List<NetworkInterface> networkInterfaces = instance.getNetworkInterfaces();
    if (networkInterfaces == null || networkInterfaces.isEmpty()) {
      return null;
    }
    List<AccessConfig> accessConfigs = networkInterfaces.get(0).getAccessConfigs();
    if (accessConfigs == null || accessConfigs.isEmpty()) {
      return null;
    }
    return accessConfigs.get(0).getNatIP();
  }

  /**
   * Utility function to extract white space separated parameters from instance metadata.
   */
  public static ImmutableList<String> getParametersFromMetadata(Instance instance, String key) {
    ImmutableMap<String, String> metadataMap = toMap(instance.getMetadata());
    String jvmParameterString = metadataMap.get(key);
    if (!Strings.isNullOrEmpty(jvmParameterString)) {
      return ImmutableList.copyOf(Splitter.on(CharMatcher.WHITESPACE)
          .omitEmptyStrings()
          .trimResults()
          .split(jvmParameterString));
    }
    return ImmutableList.of();
  }

  /**
   * @return the standard image project API that the specified image belongs to.
   * For example, image centos-6-v20130522 should return "centos-cloud" and image
   * debian-6-squeeze-v20130522 should return "debian-cloud".
   */
  public static String findStandardImageProjectApi(String defaultImageName) {
    String matchingProjectApi = null;
    for (ProjectMapper pm : ProjectMapper.values()) {
      if (pm.imageNamePattern().matcher(defaultImageName).matches()) {
        matchingProjectApi = pm.projectName();
        break;
      }
    }
    return Preconditions.checkNotNull(matchingProjectApi);
  }

  /**
   * @return the image resource type from an image resourceId.
   */
  public static ComputeResourceType findImageResourceType(ResourceId imageResourceId) {
    return imageResourceId.getResourceType() == ResourceType.DEFAULT_IMAGE
        ? ComputeResourceType.DEFAULT_IMAGE
        : ComputeResourceType.CUSTOM_IMAGE;
  }

  public static enum ProjectMapper {
    DEBIAN("debian-cloud", "^debian-.*", true),
    DEBIAN_BACKPORTS("debian-cloud", "^backports-debian-.*", true),
    CENTOS_7("centos-cloud", "^centos-7-.*", true),
    CENTOS("centos-cloud", "^centos-*", false),
    COREOS("coreos-cloud", "^coreos-stable-.*", false),
    OPENSUSE("opensuse-cloud", "^opensuse-.*", false),
    RHEL_7("rhel-cloud", "^rhel-7-.*", true),
    RHEL("rhel-cloud", "^rhel-.*", false),
    SLES("suse-cloud", "^sles-.*", false),
    ;

    private final String projectName;
    private final Pattern imageNamePattern;
    private final boolean supported;

    private ProjectMapper(String name, String imageNamePattern, boolean supported) {
      this.projectName = name;
      this.imageNamePattern = Pattern.compile(imageNamePattern);
      // This property is a hack for testing, not intended to be enabled by users
      this.supported = supported;
    }

    public String projectName() {
      return projectName;
    }

    public Pattern imageNamePattern() {
      return imageNamePattern;
    }

    public boolean supported() {
      return supported;
    }

    public static ProjectMapper valueOfProjectName(String projectName) {
      for (ProjectMapper pm : values()) {
        if (pm.projectName.equals(projectName)) {
          return pm;
        }
      }
      return null;
    }

    public static boolean enableUnsupported() {
      return Boolean.getBoolean("EnableUnsupportedImages");
    }
  }
}
