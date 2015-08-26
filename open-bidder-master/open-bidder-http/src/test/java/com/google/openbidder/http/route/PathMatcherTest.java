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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.openbidder.util.testing.TestUtil;

import org.junit.Test;

/**
 * Tests for {@link PathMatcherType},
 */
public class PathMatcherTest {

  private PathMatcher specs[] = {
      get(""), // root
      get("/"), // default
      get("/foo/bar"), // exact
      get("/foo"), // exact
      get("/foo/bar/*"), // prefix
      get("/foo/*"), // prefix
      get("/*"), // prefix
      get("*.html"), // suffix
  };

  @Test
  public void testCommonMethods() {
    PathMatcher pm1 = get("/");
    PathMatcher pm2 = get("/");
    PathMatcher pm3 = get("/*");
    TestUtil.testCommonMethods(pm1, pm2, pm3);

    assertSame(PathMatcherType.DEFAULT, pm1.getMatchType());
    assertEquals("/", pm1.getPathSpec());
  }

  @Test
  public void testCompare() {
    for (int i = 0; i < specs.length; i++) {
      PathMatcher first = specs[i];
      for (int j = 0; j < specs.length; j++) {
        PathMatcher second = specs[j];
        if (i < j) {
          assertTrue(first + " should be less than " + second
              + " was " + first.compareTo(second), first.compareTo(second) < 0);
        } else if (i == j) {
          assertTrue(first + " should be equal to " + second
              + " was " + first.compareTo(second), first.compareTo(second) == 0);
        } else {
          assertTrue(first + " should be greater than " + second
              + " was " + first.compareTo(second), first.compareTo(second) > 0);
        }
      }
    }
  }

  @Test(expected = NullPointerException.class)
  public void buildMatcher_null_nullPointerException() {
    PathMatcherType.buildMatcher(null);
  }

  @Test
  public void testExact() {
    PathMatcher matcher = PathMatcherType.buildMatcher("/foo");
    assertEquals(PathMatcherType.EXACT, matcher.getMatchType());
    assertEquals("/foo", matcher.getPathSpec());
    assertTrue(matcher.apply("/foo"));
    assertFalse(matcher.apply("/foo/"));
    assertFalse(matcher.apply("/"));
    assertFalse(matcher.apply("/foobar"));
    assertFalse(matcher.apply("/a/b/c"));
  }

  @Test
  public void testPrefix() {
    PathMatcher matcher = PathMatcherType.buildMatcher("/foo/*");
    assertEquals(PathMatcherType.PREFIX, matcher.getMatchType());
    assertEquals("/foo/*", matcher.getPathSpec());
    assertFalse(matcher.apply("/foo"));
    assertTrue(matcher.apply("/foo/"));
    assertTrue(matcher.apply("/foo/bar"));
    assertFalse(matcher.apply(""));
    assertFalse(matcher.apply("/foo.html"));
    assertFalse(matcher.apply("/"));
    assertFalse(matcher.apply("/foobar"));
    assertFalse(matcher.apply("/a/b/c"));
  }

  @Test
  public void testPrefix2() {
    PathMatcher matcher = PathMatcherType.buildMatcher("/*");
    assertEquals(PathMatcherType.PREFIX, matcher.getMatchType());
    assertEquals("/*", matcher.getPathSpec());
    assertTrue(matcher.apply("/foo"));
    assertTrue(matcher.apply("/foo/"));
    assertTrue(matcher.apply("/foo/bar"));
    assertFalse(matcher.apply(""));
    assertTrue(matcher.apply("/foo.html"));
    assertTrue(matcher.apply("/"));
    assertTrue(matcher.apply("/foobar"));
    assertTrue(matcher.apply("/a/b/c"));
  }

  @Test
  public void testSuffix() {
    PathMatcher matcher = PathMatcherType.buildMatcher("*.html");
    assertEquals(PathMatcherType.SUFFIX, matcher.getMatchType());
    assertEquals("*.html", matcher.getPathSpec());
    assertTrue(matcher.apply("foo.html"));
    assertTrue(matcher.apply("/foo/bar.html"));
    assertTrue(matcher.apply("/a/b/c/foo.html"));
    assertFalse(matcher.apply(""));
    assertFalse(matcher.apply("/foo.html/bar"));
    assertFalse(matcher.apply("/"));
    assertFalse(matcher.apply("/a/b/c"));
  }

  @Test
  public void testDefault() {
    PathMatcher matcher = PathMatcherType.buildMatcher("/");
    assertEquals(PathMatcherType.DEFAULT, matcher.getMatchType());
    assertEquals("/", matcher.getPathSpec());
    assertFalse(matcher.apply("foo.html"));
    assertFalse(matcher.apply("/foo/bar.html"));
    assertFalse(matcher.apply("/a/b/c/foo.html"));
    assertFalse(matcher.apply(""));
    assertFalse(matcher.apply("/foo.html/bar"));
    assertTrue(matcher.apply("/"));
    assertFalse(matcher.apply("/a/b/c"));
  }

  @Test
  public void testRoot() {
    PathMatcher matcher = PathMatcherType.buildMatcher("");
    assertEquals(PathMatcherType.ROOT, matcher.getMatchType());
    assertEquals("", matcher.getPathSpec());
    assertFalse(matcher.apply("foo.html"));
    assertFalse(matcher.apply("/foo/bar.html"));
    assertFalse(matcher.apply("/a/b/c/foo.html"));
    assertTrue(matcher.apply(""));
    assertFalse(matcher.apply("/foo.html/bar"));
    assertFalse(matcher.apply("/"));
    assertFalse(matcher.apply("/a/b/c"));
  }

  private static PathMatcher get(String pathSpec) {
    return PathMatcherType.buildMatcher(pathSpec);
  }
}
