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

package com.google.openbidder.http.util;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMultimap;

import org.junit.Test;

/**
 * Tests for {@link HttpUtil}.
 */
public class HttpUtilTest {

  @Test
  public void buildUri() {
    HttpUtil.buildUri("http://a.io/path");
  }

  @Test(expected = IllegalArgumentException.class)
  public void buildUri_bad() {
    HttpUtil.buildUri("this is not a uri");
  }

  @Test
  public void testToMultimap() {
    assertEquals(
        ImmutableMultimap.of("a", "x", "a", "y"),
        HttpUtil.toMultimap("a", "x", "a", "y"));
  }

  @Test
  public void concatPaths_nothing_root() {
    assertEquals("/", HttpUtil.concatPaths());
  }

  @Test
  public void concatPaths_empty_root() {
    assertEquals("/", HttpUtil.concatPaths(""));
  }

  @Test
  public void concatPaths_root_root() {
    assertEquals("/", HttpUtil.concatPaths("/"));
  }

  @Test
  public void concatPaths_something_slashAdded() {
    assertEquals("/foo", HttpUtil.concatPaths("foo"));
  }

  @Test
  public void concatPaths_trailingSlash_slashRemoved() {
    assertEquals("/foo", HttpUtil.concatPaths("foo/"));
  }

  @Test
  public void concatPaths_internalSlash_slashUnchanged() {
    assertEquals("/foo/bar", HttpUtil.concatPaths("foo/bar"));
  }

  @Test
  public void concatPaths_emptyPlusSomething_something() {
    assertEquals("/foo", HttpUtil.concatPaths("", "foo"));
  }

  @Test
  public void concatPaths_emptyPlusSlashSomething_something() {
    assertEquals("/foo", HttpUtil.concatPaths("", "/foo"));
  }

  @Test
  public void concatPaths_emptyPlusSomethingSlash_something() {
    assertEquals("/foo", HttpUtil.concatPaths("", "foo/"));
  }

  @Test
  public void concatPaths_twoSomethings_something() {
    assertConcat("/foo/bar", "foo", "bar");
    assertConcat("/foo/bar", "/foo", "bar");
    assertConcat("/foo/bar", "foo/", "bar");
    assertConcat("/foo/bar", "/foo/", "bar");
  }

  private void assertConcat(String expected, String prefix, String suffix) {
    assertEquals(expected, HttpUtil.concatPaths(prefix, suffix));
    assertEquals(expected, HttpUtil.concatPaths(prefix, "/" + suffix));
    assertEquals(expected, HttpUtil.concatPaths(prefix, suffix + "/"));
    assertEquals(expected, HttpUtil.concatPaths(prefix, "/" + suffix + "/"));
  }
}
