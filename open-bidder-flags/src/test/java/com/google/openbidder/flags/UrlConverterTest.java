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

import static org.junit.Assert.assertEquals;

import com.google.openbidder.flags.util.UriConverter;
import com.google.openbidder.flags.util.UriValidator;

import com.beust.jcommander.ParameterException;

import org.junit.Test;

import java.net.URI;

/**
 * Tests for {@link UriConverter} and {@link UriValidator}.
 */
public class UrlConverterTest {

  @Test
  public void testConverter() {
    UriConverter converter = new UriConverter();
    URI uri = converter.convert("http://localhost:8080/path?a=5&b=6");
    assertEquals("http", uri.getScheme());
    assertEquals("localhost", uri.getHost());
    assertEquals(8080, uri.getPort());
    assertEquals("/path", uri.getPath());
    assertEquals("a=5&b=6", uri.getQuery());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConverter_bad() {
    UriConverter converter = new UriConverter();
    converter.convert("this doesn't look like a URL");
  }

  @Test
  public void testValidator() {
    UriValidator validator = new UriValidator();
    validator.validate("url", "http://localhost:8080/path?a=5&b=6");
  }

  @Test(expected = ParameterException.class)
  public void testValidator_bad() {
    UriValidator validator = new UriValidator();
    validator.validate("url", "this doesn't look like a URL");
  }
}
