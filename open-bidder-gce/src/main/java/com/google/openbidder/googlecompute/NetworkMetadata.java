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

package com.google.openbidder.googlecompute;

import com.google.api.client.util.Key;
import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Instance network meta-data retrieved from the instance meta-data server.
 *
 * See the <a href="https://developers.google.com/compute/docs/metadata">
 * Compute Engine documentation</a> for more details.
 */
public class NetworkMetadata {

  /**
   * Network interface external access configuration
   */
  public static class AccessConfiguration {
    private static final Function<AccessConfiguration, String> GET_IP =
        new Function<NetworkMetadata.AccessConfiguration, String>() {
      @Override public @Nullable String apply(AccessConfiguration accessConfig) {
        assert accessConfig != null;
        return accessConfig.getExternalIp();
      }};

    @Key(value = "externalIp")
    private String externalIp;
    @Key(value = "type")
    private String type;

    /**
     * @return External IP of the instance
     */
    public final String getExternalIp() {
      return externalIp;
    }

    public final void setExternalIp(String externalIp) {
      this.externalIp = externalIp;
    }

    /**
     * @return Type of network interface.
     */
    public final String getType() {
      return type;
    }

    public final void setType(String type) {
      this.type = type;
    }

    @Override public int hashCode() {
      return Objects.hashCode(type, externalIp);
    }

    @Override public boolean equals(@Nullable Object obj) {
      if (obj == this) {
        return true;
      } else if (!(obj instanceof AccessConfiguration)) {
        return false;
      }

      AccessConfiguration other = (AccessConfiguration) obj;

      return Objects.equal(type, other.type)
          && Objects.equal(externalIp, other.externalIp);
    }

    @Override public String toString() {
      return MoreObjects.toStringHelper(this).omitNullValues()
          .add("externalIp", externalIp)
          .add("type", type)
          .toString();
    }
  }

  /**
   * The network interface.
   */
  public static class NetworkInterface {
    private static final Function<NetworkInterface, String> GET_IP =
        new Function<NetworkMetadata.NetworkInterface, String>() {
      @Override public @Nullable String apply(NetworkInterface netIf) {
        assert netIf != null;
        return netIf.getIp();
      }};

    @Key(value = "accessConfiguration")
    private List<AccessConfiguration> accessConfiguration;
    @Key(value = "ip")
    private String ip;
    @Key(value = "network")
    private String network;

    /**
     * @return List of external access configurations
     */
    public final List<AccessConfiguration> getAccessConfiguration() {
      return accessConfiguration;
    }

    public final void setAccessConfiguration(List<AccessConfiguration> accessConfiguration) {
      this.accessConfiguration = accessConfiguration;
    }

    /**
     * @return Internal ip of the instance.
     */
    public final String getIp() {
      return ip;
    }

    public final void setIp(String ip) {
      this.ip = ip;
    }

    /**
     * @return Name of the network
     */
    public final String getNetwork() {
      return network;
    }

    public final void setNetwork(String network) {
      this.network = network;
    }

    @Override public int hashCode() {
      return Objects.hashCode(network, ip);
    }

    @Override public boolean equals(@Nullable Object obj) {
      if (obj == this) {
        return true;
      } else if (!(obj instanceof NetworkInterface)) {
        return false;
      }

      NetworkInterface other = (NetworkInterface) obj;

      return Objects.equal(network, other.network)
          && Objects.equal(ip, other.ip)
          && Objects.equal(accessConfiguration, other.accessConfiguration);
    }

    @Override public String toString() {
      return MoreObjects.toStringHelper(this).omitNullValues()
          .add("ip", ip)
          .add("network", network)
          .add("accessConfiguration",
              Lists.transform(accessConfiguration, AccessConfiguration.GET_IP))
          .toString();
    }
  }

  @Key(value = "networkInterface")
  private List<NetworkInterface> networkInterfaces;

  /**
   * @return Network interfaces
   */
  public final List<NetworkInterface> getNetworkInterfaces() {
    return networkInterfaces;
  }

  public final void setNetworkInterfaces(List<NetworkInterface> networkInterfaces) {
    this.networkInterfaces = networkInterfaces;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(networkInterfaces);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (obj == this) {
      return true;
    } else if (!(obj instanceof NetworkMetadata)) {
      return false;
    }

    NetworkMetadata other = (NetworkMetadata) obj;

    return Objects.equal(networkInterfaces, other.networkInterfaces);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("networkInterfaces", Lists.transform(networkInterfaces, NetworkInterface.GET_IP))
        .toString();
  }
}
