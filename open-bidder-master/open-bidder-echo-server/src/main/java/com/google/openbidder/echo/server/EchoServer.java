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

package com.google.openbidder.echo.server;

import com.google.inject.Module;
import com.google.openbidder.netty.server.NettyServerModule;
import com.google.openbidder.server.ServerModule;
import com.google.openbidder.server.ServiceWrapper;
import com.google.openbidder.system.SystemModule;

import java.util.Arrays;
import java.util.List;

/**
 * Start a server that echoes HTTP payloads back to the client.
 */
public class EchoServer extends ServiceWrapper {

  public EchoServer(String[] args) {
    super(args);
  }

  public static void main(String[] args) {
    new EchoServer(args).main();
  }

  @Override
  protected List<Module> getModules() {
    return Arrays.<Module>asList(
        new SystemModule(),
        new ServerModule(),
        new NettyServerModule(),
        new EchoServerModule()
    );
  }
}
