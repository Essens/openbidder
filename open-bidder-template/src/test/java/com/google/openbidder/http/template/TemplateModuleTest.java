/*
 * Copyright 2014 Google Inc. All Rights Reserved.
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

package com.google.openbidder.http.template;

import static org.junit.Assert.assertNotNull;

import com.google.common.collect.ImmutableList;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.Stage;

import org.junit.Test;

import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Map;

/**
 * Tests for {@link TemplateModule}.
 */
public class TemplateModuleTest {

  @Test
  public void testModule() {
    List<Module> modules = ImmutableList.<Module>of(
        new Module() {
          @Override public void configure(Binder binder) {
            binder.bind(TemplateEngine.class).to(TestTemplateEngine.class).in(Scopes.SINGLETON);
          }},
        new TemplateModule());
    assertNotNull(Guice.createInjector(Stage.DEVELOPMENT, modules));
  }

  static class TestTemplateEngine extends AbstractTemplateEngine {
    @Override public Template compile(String templateName, Reader reader) {
      return new AbstractTemplate("test") {
        @Override public void process(Writer writer, Map<String, Object> context) {
        }};
    }
  }
}
