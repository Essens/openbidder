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

package com.google.openbidder.sample;

import com.google.common.collect.ImmutableList;
import com.google.openbidder.exchange.doubleclick.testing.DoubleClickTestUtil;

import org.junit.Test;

import java.util.List;
import java.util.Properties;

/**
 * Test the full {@link SamplesServer} initialization and shutdown.
 */
public class SamplesServerTest {

  @Test
  public void testConfigure() throws Exception {
    Properties sysProps = (Properties) System.getProperties().clone();
    System.setProperty("jetty.home", System.getProperty("java.io.tmpdir"));

    ImmutableList.Builder<String> args = ImmutableList.<String>builder().add(
        "--platform=GOOGLE_COMPUTE",
        "--service_account=default",
        "--p12_file_path=/dev/null",
        "--service_account_id=0",
        "--api_project_id=0",
        "--api_project_number=0",
        "--storage_oauth2_scope=0",
        "--load_balancer_host=http://localhost",
        "--load_balancer_port=18080",
        "--listen_port=18081",
        "--admin_port=18082",
        "--bid_interceptors="
            + "com.google.openbidder.api.testing.bidding.CountingBidInterceptor",
        "--impression_interceptors="
            + "com.google.openbidder.impression.interceptor.SimpleImpressionInterceptor",
        "--click_interceptors="
            + "com.google.openbidder.click.interceptor.SimpleClickInterceptor",
        "--doubleclick_local_resources",
        "--doubleclick_encryption_key=" + DoubleClickTestUtil.zeroKeyEncoded(),
        "--doubleclick_integrity_key=" + DoubleClickTestUtil.zeroKeyEncoded()
      );

    List<String> list = args.build();
    SamplesServer server = new SamplesServer(list.toArray(new String[list.size()]));
    server.startAsync().awaitRunning();
    server.stopAsync().awaitTerminated();

    System.setProperties((Properties) sysProps.clone());
  }
}
