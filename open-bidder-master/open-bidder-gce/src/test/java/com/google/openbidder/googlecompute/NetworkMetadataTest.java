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

import com.google.common.collect.ImmutableList;
import com.google.openbidder.util.testing.TestUtil;

import org.junit.Test;

/**
 * Tests for {@link NetworkMetadata}.
 */
public class NetworkMetadataTest {

  @Test
  public void testCommonMethods() {
    NetworkMetadata.AccessConfiguration access1 = new NetworkMetadata.AccessConfiguration();
    access1.setType("type");
    access1.setExternalIp("10.0.0.8");
    NetworkMetadata.AccessConfiguration access2 = new NetworkMetadata.AccessConfiguration();
    access2.setType("type");
    access2.setExternalIp("10.0.0.8");
    NetworkMetadata.AccessConfiguration access3 = new NetworkMetadata.AccessConfiguration();
    access3.setType("type-diff");
    access3.setExternalIp("10.0.0.8");

    TestUtil.testCommonMethods(access1, access2, access3);

    NetworkMetadata.NetworkInterface interf1 = new NetworkMetadata.NetworkInterface();
    interf1.setIp("10.0.0.9");
    interf1.setNetwork("abcde");
    interf1.setAccessConfiguration(ImmutableList.of(access1));
    NetworkMetadata.NetworkInterface interf2 = new NetworkMetadata.NetworkInterface();
    interf2.setIp("10.0.0.9");
    interf2.setNetwork("abcde");
    interf2.setAccessConfiguration(ImmutableList.of(access2));
    NetworkMetadata.NetworkInterface interf3 = new NetworkMetadata.NetworkInterface();
    interf3.setIp("10.0.0.9");
    interf3.setNetwork("abcde-diff");
    interf3.setAccessConfiguration(ImmutableList.of(access3));

    TestUtil.testCommonMethods(interf1, interf2, interf3);

    NetworkMetadata network1 = new NetworkMetadata();
    network1.setNetworkInterfaces(ImmutableList.of(interf1));

    NetworkMetadata network2 = new NetworkMetadata();
    network2.setNetworkInterfaces(ImmutableList.of(interf2));

    NetworkMetadata network3 = new NetworkMetadata();
    network3.setNetworkInterfaces(ImmutableList.of(interf3));

    TestUtil.testCommonMethods(network1, network2, network3);
  }
}
