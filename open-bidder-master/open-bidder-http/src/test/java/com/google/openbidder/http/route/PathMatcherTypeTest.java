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

package com.google.openbidder.http.route;

import static org.junit.Assert.assertEquals;

import com.google.openbidder.http.route.PathMatcher;
import com.google.openbidder.http.route.PathMatcherType;

import org.junit.Test;

public class PathMatcherTypeTest {

  @Test
  public void buildMatcher_root() {
    expectRoot("");
  }

  @Test
  public void buildMatcher_default() {
    expectDefault("/");
  }

  @Test
  public void buildMatcher_exact() {
    expectExact("/foo", "/foo/", "/foo/bar", "/foo/bar/", "/foo*", "/foo*html", "/foo/*.html",
        "*.", "foo/*", "foo/bar/*");
  }

  @Test
  public void buildMatcher_prefix() {
    expectPrefix("/foo/*", "/*", "/foo/bar/*", "/foo/*/bar/*", "/foo/*.html/bar/*");
  }

  @Test
  public void buildMatcher_suffix() {
    expectSuffix("*.html", "*.*.", "*.*");
  }

  private static void expectExact(String... paths) {
    expect(PathMatcherType.EXACT, paths);
  }

  private static void expectPrefix(String... paths) {
    expect(PathMatcherType.PREFIX, paths);
  }

  private static void expectSuffix(String... paths) {
    expect(PathMatcherType.SUFFIX, paths);
  }

  private static void expectRoot(String... paths) {
    expect(PathMatcherType.ROOT, paths);
  }

  private static void expectDefault(String... paths) {
    expect(PathMatcherType.DEFAULT, paths);
  }

  private static void expect(PathMatcherType type, String... paths) {
    for (String path : paths) {
      PathMatcher matcher = PathMatcherType.buildMatcher(path);
      assertEquals("Expected " + type + " for " + path, type, matcher.getMatchType());
      assertEquals("Expected " + path + " for pathSpec, got " + matcher.getPathSpec(),
          path, matcher.getPathSpec());
    }
  }
}
