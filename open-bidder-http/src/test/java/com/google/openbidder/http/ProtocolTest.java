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

package com.google.openbidder.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import com.google.openbidder.util.testing.TestUtil;

import org.junit.Test;

/**
 * Tests for {@link Protocol}.
 */
public class ProtocolTest {

  @Test
  public void testCommonMethods() {
    Protocol protocol1 = Protocol.decode("HTTP/1.0");
    Protocol protocol2 = Protocol.decode("HTTP/1.0");
    Protocol protocol3 = Protocol.decode("HTTP/1.1");
    TestUtil.testCommonMethods(protocol1, protocol2, protocol3);
  }

  @Test(expected = IllegalArgumentException.class)
  public void constructor_emptyName_exception() {
    Protocol.decode("/1.1");
  }

  @Test(expected = IllegalArgumentException.class)
  public void constructor_negativeMajorVersion_exception() {
    Protocol.decode("HTTP/-1.1");
  }

  @Test(expected = IllegalArgumentException.class)
  public void constructor_negativeMinorVersion_exception() {
    Protocol.decode("HTTP/1.-1");
  }

  @Test
  public void testGetters() {
    Protocol protocol = Protocol.decode("FTP/22.19");
    assertEquals("FTP", protocol.name());
    assertEquals(22, protocol.majorVersion());
    assertEquals(19, protocol.minorVersion());
    assertEquals("FTP/22.19", protocol.text());
  }

  @Test
  public void decode_known() {
    assertSame(Protocol.HTTP_1_0, Protocol.decode("HTTP/1.0"));
    assertSame(Protocol.HTTP_1_1, Protocol.decode("HTTP/1.1"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void decode_nameOnly_exception() {
    Protocol.decode("foo");
  }

  @Test(expected = IllegalArgumentException.class)
  public void decode_noSlash_exception() {
    Protocol.decode("HTTP 1.1");
  }

  @Test(expected = IllegalArgumentException.class)
  public void decode_noMajor_exception() {
    Protocol.decode("FTP/.3");
  }

  @Test(expected = IllegalArgumentException.class)
  public void decode_noMinor_exception() {
    Protocol.decode("FTP/17");
  }
}
