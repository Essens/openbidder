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

package com.google.openbidder.http.message;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import com.google.common.base.Charsets;
import com.google.common.net.MediaType;
import com.google.openbidder.http.HttpMessage;
import com.google.openbidder.http.request.StandardHttpRequest;
import com.google.protobuf.ByteString;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Tests for {@link HttpMessage}.
 */
public class ContentHolderTest {
  static final String str = "content";
  static final byte[] bytes = str.getBytes(Charsets.UTF_8);

  @Test
  public void test() {
    ContentHolder contentHolder = new ContentHolder("", -1, ContentHolder.State.NONE);
    assertEquals("", contentHolder.self());
    assertEquals(-1, contentHolder.getLength());
    contentHolder.setLength(100);
    assertEquals(100, contentHolder.getLength());
    assertSame(Charsets.UTF_8, contentHolder.getCharset());
    contentHolder.setDefaultCharset(Charsets.US_ASCII);
    assertSame(Charsets.US_ASCII, contentHolder.getDefaultCharset());
    assertSame(Charsets.US_ASCII, contentHolder.getCharset());
    contentHolder = new ContentHolder(
        StandardHttpRequest.newBuilder().setMediaType(MediaType.ANY_TYPE),
        -1,
        ContentHolder.State.NONE);
    contentHolder.setDefaultCharset(Charsets.US_ASCII);
    assertSame(contentHolder.getDefaultCharset(), contentHolder.getCharset());
    contentHolder = new ContentHolder(
        StandardHttpRequest.newBuilder().setMediaType(MediaType.JSON_UTF_8),
        -1,
        ContentHolder.State.NONE);
    contentHolder.setDefaultCharset(Charsets.US_ASCII);
    assertSame(Charsets.UTF_8, contentHolder.getCharset());
  }

  @Test
  public void testExternalOS() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ContentHolder contentHolder = new ContentHolder(baos, -1, ContentHolder.State.OUTPUT);
    assertNotNull(contentHolder.toString());
    assertSame(Charsets.UTF_8, contentHolder.getDefaultCharset());
    contentHolder.outputOut().write(bytes);
    assertEquals(ContentHolder.State.OUTPUT_STREAM, contentHolder.getState());
    contentHolder.outputOut().write(new byte[0]);
    assertEquals(7, baos.size());
    assertNotNull(contentHolder.toString());
    contentHolder.outputClose();
    assertEquals(ContentHolder.State.OUTPUT_CLOSED, contentHolder.getState());
  }

  @Test
  public void testExternalOSWriter() {
    ByteString.Output bso = ByteString.newOutput();
    ContentHolder contentHolder = new ContentHolder(bso, -1, ContentHolder.State.OUTPUT);
    assertNotNull(contentHolder.toString());
    assertSame(Charsets.UTF_8, contentHolder.getDefaultCharset());
    contentHolder.outputWriter().write(str);
    assertEquals(ContentHolder.State.OUTPUT_WRITER, contentHolder.getState());
    contentHolder.outputWriter().write("");
    contentHolder.outputWriter().flush();
    assertNotNull(contentHolder.toString());
    assertEquals(7, bso.size());
    contentHolder.outputClose();
  }

  @Test
  public void testInputEmpty() throws IOException {
    ContentHolder contentHolder = new ContentHolder("", -1, ContentHolder.State.NONE);
    contentHolder.outputClose();
    assertEquals(ContentHolder.State.OUTPUT_CLOSED, contentHolder.getState());
    assertEquals(0, contentHolder.inputIn().available());
    assertEquals(ContentHolder.State.INPUT_STREAM, contentHolder.getState());
    assertEquals(0, contentHolder.inputIn().available());
  }

  public void testInputFromSelf() throws IOException {
    ContentHolder contentHolder = new ContentHolder(
        new ByteArrayInputStream(new byte[0]), -1, ContentHolder.State.INPUT);
    assertEquals(0, contentHolder.inputIn().available());
    assertEquals(0, contentHolder.inputIn().available());
  }

  public void testReaderFromSelf() throws IOException {
    ContentHolder contentHolder = new ContentHolder(
        new ByteArrayInputStream(new byte[0]), -1, ContentHolder.State.INPUT);
    assertNull(contentHolder.inputReader().readLine());
    assertNull(contentHolder.inputReader().readLine());
  }

  @Test
  public void testPrintThenReader() throws IOException {
    ContentHolder contentHolder = new ContentHolder("", -1, ContentHolder.State.NONE);
    contentHolder.outputWriter().print(str);
    assertEquals(ContentHolder.State.OUTPUT_WRITER, contentHolder.getState());
    contentHolder.outputWriter().print("");
    contentHolder.outputClose();
    assertEquals(ContentHolder.State.OUTPUT_CLOSED, contentHolder.getState());
    assertEquals(str, contentHolder.inputReader().readLine());
    assertEquals(ContentHolder.State.INPUT_READER, contentHolder.getState());
    assertNull(contentHolder.inputReader().readLine());
  }

  @Test
  public void testWriteThenIS() throws IOException {
    ContentHolder contentHolder = new ContentHolder("", -1, ContentHolder.State.NONE);
    contentHolder.outputOut().write(bytes);
    contentHolder.outputOut().write(new byte[0]);
    contentHolder.outputClose();
    assertEquals(ContentHolder.State.OUTPUT_CLOSED, contentHolder.getState());
    assertArrayEquals(bytes, readContentBytes(contentHolder.inputIn()));
  }

  @Test(expected = IllegalStateException.class)
  public void testISThenReader() throws IOException {
    ContentHolder contentHolder = new ContentHolder("", -1, ContentHolder.State.NONE);
    contentHolder.outputClose();
    assertEquals(0, contentHolder.inputIn().available());
    assertEquals(ContentHolder.State.INPUT_STREAM, contentHolder.getState());
    contentHolder.inputReader();
  }

  @Test(expected = IllegalStateException.class)
  public void testReaderThenIS() throws IOException {
    ContentHolder contentHolder = new ContentHolder("", -1, ContentHolder.State.NONE);
    contentHolder.outputClose();
    assertNull(contentHolder.inputReader().readLine());
    assertEquals(ContentHolder.State.INPUT_READER, contentHolder.getState());
    contentHolder.inputIn();
  }

  @Test(expected = IllegalStateException.class)
  public void testOSThenWriter() throws IOException {
    ContentHolder contentHolder = new ContentHolder(
        new ByteArrayOutputStream(), -1, ContentHolder.State.OUTPUT);
    contentHolder.outputOut().write(bytes);
    contentHolder.outputWriter();
  }

  @Test(expected = IllegalStateException.class)
  public void testWriterThenOS() {
    ContentHolder contentHolder = new ContentHolder(
        new ByteArrayOutputStream(), -1, ContentHolder.State.OUTPUT);
    contentHolder.outputWriter().print(str);
    contentHolder.outputOut();
  }

  @Test(expected = IllegalStateException.class)
  public void testOSNotOutput() {
    ContentHolder contentHolder = new ContentHolder("", -1, ContentHolder.State.NONE);
    contentHolder.inputIn();
    contentHolder.outputOut();
  }

  @Test(expected = IllegalStateException.class)
  public void testWriterNotOutput() {
    ContentHolder contentHolder = new ContentHolder("", -1, ContentHolder.State.NONE);
    contentHolder.inputIn();
    contentHolder.outputWriter();
  }

  @Test(expected = IllegalStateException.class)
  public void testISNotInput() {
    ContentHolder contentHolder = new ContentHolder("", -1, ContentHolder.State.NONE);
    contentHolder.outputOut();
    contentHolder.inputIn();
  }

  @Test(expected = IllegalStateException.class)
  public void testReaderNotInput() {
    ContentHolder contentHolder = new ContentHolder("", -1, ContentHolder.State.NONE);
    contentHolder.outputOut();
    contentHolder.inputReader();
  }

  @Test(expected = IllegalStateException.class)
  public void testCloseNotInput() {
    ContentHolder contentHolder = new ContentHolder("", -1, ContentHolder.State.INPUT);
    contentHolder.outputClose();
  }

  @Test
  public void testCloseFlush() {
    ContentHolder contentHolder = new ContentHolder("", -1, ContentHolder.State.NONE);
    contentHolder.outputWriter().print("x");
    contentHolder.outputClose();
    assertEquals(ContentHolder.State.OUTPUT_CLOSED, contentHolder.getState());

    contentHolder = new ContentHolder("", -1, ContentHolder.State.NONE) {
      @Override protected boolean outputCloseShouldFlush() {
        return false;
      }};
    contentHolder.outputWriter().print("x");
    contentHolder.outputClose();
    assertEquals(ContentHolder.State.OUTPUT_CLOSED, contentHolder.getState());
  }

  static byte[] readContentBytes(InputStream is) {
    try {
      byte[] ret = new byte[is.available()];
      is.read(ret);
      assertEquals(0, is.available());
      return ret;
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }
}
