/*
 * Copyright 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.openbidder.server;

import static org.junit.Assert.assertNotNull;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.openbidder.http.HttpModule;
import com.google.openbidder.http.template.TemplateModule;
import com.google.openbidder.http.template.mustache.MustacheModule;

import com.beust.jcommander.JCommander;

import org.junit.Test;

import java.util.List;

/**
 * Tests for {@link ServerModule}.
 */
public class ServerModuleTest {

  @Test
  public void testModule() {
    List<Module> modules = ImmutableList.<Module>of(
        new HttpModule(),
        new TemplateModule(),
        new MustacheModule(),
        new ServerModule());
    JCommander jcommander = new JCommander(modules);
    jcommander.parse();
    assertNotNull(Guice.createInjector(Stage.DEVELOPMENT, modules));
  }
}
