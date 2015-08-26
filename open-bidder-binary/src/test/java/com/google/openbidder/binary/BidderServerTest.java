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

package com.google.openbidder.binary;

import com.google.common.collect.ImmutableList;
import com.google.openbidder.system.Platform;

import org.junit.Test;

import java.util.List;
import java.util.Properties;

/**
 * Test the full {@link BidderServer} initialization and shutdown.
 */
public class BidderServerTest {

  @Test
  public void testJettyGoogleCompute() throws Exception {
    testConfigure(false, Platform.GOOGLE_COMPUTE);
  }

  @Test
  public void testNettyGoogleCompute() throws Exception {
    testConfigure(true, Platform.GOOGLE_COMPUTE);
  }

//  @Test TODO
//  public void testGeneric() throws Exception {
//    testConfigure(netty, Platform.GENERIC);
//  }

  private void testConfigure(boolean useJetty, Platform platform) throws Exception {
    Properties sysProps = (Properties) System.getProperties().clone();

    ImmutableList.Builder<String> args = ImmutableList.<String>builder().add(
        "--platform=" + platform.name(),
        "--api_project_id=0",
        "--api_project_number=0",
        "--load_balancer_host=http://localhost",
        "--load_balancer_port=18080",
        "--listen_port=18081",
        "--admin_port=18082");
    if (platform == Platform.GENERIC) {
      args.add(
          "--p12_file_path=/dev/null",
          "--service_account_id=0");
    } else {
      args.add("--service_account=default");
    }

    if (useJetty) {
      args.add("--jetty");
      System.setProperty("jetty.home", System.getProperty("java.io.tmpdir"));
    } else {
      args.add("--netty");
    }

    List<String> list = args.build();
    BidderServer server = new BidderServer(list.toArray(new String[list.size()])) {};
    server.startAsync().awaitRunning();
    server.stopAsync().awaitTerminated();
    System.setProperties((Properties) sysProps.clone());
  }
}
