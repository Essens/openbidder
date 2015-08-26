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

package com.google.openbidder.requester;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.openbidder.client.ClientModule;
import com.google.openbidder.flags.FlagsModuleBuilder;
import com.google.openbidder.http.HttpModule;
import com.google.openbidder.netty.client.NettyClient;
import com.google.openbidder.netty.client.NettyClientModule;
import com.google.openbidder.system.SystemModule;
import com.google.protobuf.ByteString;
import com.google.protos.adx.NetworkBid.BidRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.net.ConnectException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Requester tool application.
 */
public class RequesterTool {

  public static AtomicInteger requestedSent = new AtomicInteger(0);
  public static AtomicInteger responsesReceived = new AtomicInteger(0);

  private static final Logger logger = LoggerFactory.getLogger(RequesterTool.class);
  private final Random random = new Random();

  public static void main(String[] args) {
    SLF4JBridgeHandler.install();
    boolean success = false;
    try {
      new RequesterTool().run(args);
      success = true;
    } catch (ConnectException e) {
      logger.error("Connection error", e);
    } catch (Throwable t) {
      logger.error("Initialization failure", t);
    } finally {
      if (!success) {
        Runtime.getRuntime().exit(1);
      }
    }
  }

  private void run(String[] args) throws Exception {
    Module rootModule = new FlagsModuleBuilder().addModules(getModules()).build(args);
    Injector injector = Guice.createInjector(Stage.PRODUCTION, rootModule);
    RequesterClientFactory clientFactory = injector.getInstance(RequesterClientFactory.class);
    clientFactory.startAsync().awaitRunning();
    NettyClient<BidRequest> client = clientFactory.connect();
    ScheduledExecutorService scheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor();
    final AtomicInteger step = new AtomicInteger(0);
    final long start = System.currentTimeMillis();
    scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
      @Override public void run() {
        long now = System.currentTimeMillis();
        long responses = responsesReceived.get();
        logger.info("{}: {} sent, {} received ({}/second)",
            step.incrementAndGet(),
            requestedSent.get(),
            responsesReceived,
            responses / (now - start) * 1000.0d);
      }},
      1, 1, TimeUnit.SECONDS);
    client.send(randomBidRequest());
    logger.info("Stopping");
    clientFactory.stopAsync().awaitTerminated();
  }

  protected List<Module> getModules() {
    return Arrays.<Module>asList(
        new SystemModule(),
        new ClientModule(),
        new HttpModule(),
        new NettyClientModule(),
        new RequesterModule()
    );
  }

  private BidRequest randomBidRequest() {
    return BidRequest.newBuilder()
        .setId(randomId())
        .setGoogleUserId("google user")
        .build();
  }

  private ByteString randomId() {
    byte[] id = new byte[16];
    random.nextBytes(id);
    return ByteString.copyFrom(id);
  }
}

