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

package com.google.openbidder.flags;

import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.Stage;

import com.beust.jcommander.ParameterException;

import org.junit.Test;

import java.util.List;

/**
 * Tests for {@link FlagsModuleBuilder}.
 */
public class FlagsModuleBuilderTest {

  @Test
  public void testConfigure() throws Exception {
    AbstractModule module = new AbstractModule(){
      @Override protected void configure() {}
    };
    List<Module> modules = ImmutableList.<Module>of(module);

    Module rootModule = new FlagsModuleBuilder()
        .addModule(module)
        .addModules(modules)
        .build();
    Guice.createInjector(Stage.DEVELOPMENT, rootModule);
  }

  @Test
  public void testBadParam() throws Exception {
    try {
      Guice.createInjector(Stage.DEVELOPMENT, new FlagsModuleBuilder().build("--bad-parameter"));
    } catch (CreationException e) {
      assertTrue(e.getCause() instanceof ParameterException);
    }
  }

  @Test
  public void testHelpParam() throws Exception {
    try {
      new FlagsModuleBuilder().build("--help");
    } catch (CreationException e) {
      assertTrue(e.getCause() instanceof ParameterException);
    }
  }
}
