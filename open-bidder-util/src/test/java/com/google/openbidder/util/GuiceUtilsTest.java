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

package com.google.openbidder.util;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;

import org.junit.Test;

import java.util.List;

/**
 * Tests for {@link GuiceUtils}.
 */
public class GuiceUtilsTest {
  static interface If {}
  static class Impl implements If {}

  @Test
  public void loadClasses_countingBidInterceptor_ok() {
    GuiceUtils.loadClasses(If.class, Impl.class.getName());
  }

  @Test
  public void loadClasses_unknownInterceptor_ok() {
    GuiceUtils.loadClasses(If.class, "this.impl.DoesNotExist");
  }

  @Test(expected = IllegalArgumentException.class)
  public void loadClasses_wrongBaseClass_error() {
    GuiceUtils.loadClasses(If.class, String.class.getName());
  }

  @Test
  public void loadInstances() {
    Injector injector = Guice.createInjector(Stage.DEVELOPMENT, new Module() {
      @Override public void configure(Binder binder) {
        binder.bind(String.class).toInstance("Test");
      }
    });
    List<? extends String> instances =
        GuiceUtils.loadInstances(injector, String.class, "java.lang.String");
    assertEquals(ImmutableList.of("Test"), instances);
  }
}
