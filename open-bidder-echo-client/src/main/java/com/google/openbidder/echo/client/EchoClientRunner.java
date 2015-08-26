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

package com.google.openbidder.echo.client;

import com.google.common.base.Strings;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.openbidder.client.ClientModule;
import com.google.openbidder.flags.FlagsModuleBuilder;
import com.google.openbidder.netty.client.NettyClient;
import com.google.openbidder.netty.client.NettyClientModule;
import com.google.openbidder.system.SystemModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.util.Arrays;
import java.util.List;

/**
 * HTTP/1.1 client that sends line-by-line input using chunked transfer encoding.
 */
public class EchoClientRunner {

  private static final Logger logger = LoggerFactory.getLogger(EchoClientRunner.class);

  public static void main(String[] args) {
    SLF4JBridgeHandler.install();
    boolean success = false;
    try {
      new EchoClientRunner().run(args);
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
    EchoClientFactory clientFactory = injector.getInstance(EchoClientFactory.class);
    clientFactory.startAsync().awaitRunning();
    NettyClient<String> client = clientFactory.connect();
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    for (;;) {
      System.out.print("Send: ");
      String line = in.readLine();
      if (Strings.isNullOrEmpty(line)) {
        client.close();
        break;
      }
      if (!client.isConnected()) {
        client = clientFactory.connect();
      }
      client.send(line);
    }
    clientFactory.stopAsync().awaitTerminated();
  }

  protected List<Module> getModules() {
    return Arrays.<Module>asList(
        new SystemModule(),
        new ClientModule(),
        new NettyClientModule(),
        new EchoClientModule()
    );
  }
}
