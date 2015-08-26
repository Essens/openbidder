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

package com.google.openbidder.http;

import static org.junit.Assert.assertEquals;

import com.google.openbidder.http.response.StandardHttpResponse;

import org.junit.Test;

/**
 * Tests for {@link HttpSender}, {@link HttpSenderCallback}.
 */
public class HttpSenderTest {

  @Test
  public void testSend() {
    final HttpResponse httpResponse = StandardHttpResponse.newBuilder().build();
    HttpSender sender = new HttpSender() {
      @Override public void send(HttpSenderCallback callback) {
        callback.receive(httpResponse);
      }
    };
    final int[] counter = new int[]{ 0 };
    HttpSenderCallback callback = new HttpSenderCallback() {
      @Override public void receive(HttpResponse httpResponse) {
        ++counter[0];
      }
    };
    sender.send(callback);
    assertEquals(1, counter[0]);
    sender.send(HttpSenderCallback.DISCARD);
    assertEquals(1, counter[0]);
  }
}
