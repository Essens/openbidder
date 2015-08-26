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

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import com.google.openbidder.http.receiver.DefaultHttpReceiverContext;
import com.google.openbidder.http.request.StandardHttpRequest;
import com.google.openbidder.http.response.StandardHttpResponse;
import com.google.openbidder.http.template.receiver.MethodNotAllowedHttpReceiver;
import com.google.openbidder.http.template.receiver.NotFoundHttpReceiver;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;

/**
 * Tests for {@link Template}.
 */
public class TemplateTest {

  @Test
  public void testTemplate() {
    CounterTemplate template = new CounterTemplate();
    assertEquals("counter", template.getName());
    ImmutableMap<String, Object> ctx = ImmutableMap.<String, Object>of();
    StringWriter writer = new StringWriter();
    template.process(writer, ctx);
    assertEquals("", writer.toString());
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    template.process(os, null, ctx);
    assertEquals(0, os.size());
    assertEquals(3, template.processCounter);
    template.receive(new DefaultHttpReceiverContext(
        StandardHttpRequest.newBuilder().setUri("http://a.io").build(),
        StandardHttpResponse.newBuilder()));
    assertEquals(4, template.processCounter);
    assertEquals(1, template.receiveCounter);
  }

  @Test
  public void testMethodNotAllowed() {
    CounterTemplate counterTemplate = new CounterTemplate();
    new MethodNotAllowedHttpReceiver(counterTemplate).receive(createContext("HEAD"));
    assertEquals(0, counterTemplate.receiveCounter);
    new MethodNotAllowedHttpReceiver(counterTemplate).receive(createContext("GET"));
    assertEquals(1, counterTemplate.receiveCounter);
  }

  @Test
  public void testNotFound() {
    CounterTemplate counterTemplate = new CounterTemplate();
    new NotFoundHttpReceiver(counterTemplate).receive(createContext("HEAD"));
    assertEquals(0, counterTemplate.receiveCounter);
    new NotFoundHttpReceiver(counterTemplate).receive(createContext("GET"));
    assertEquals(1, counterTemplate.receiveCounter);
  }

  protected DefaultHttpReceiverContext createContext(String method) {
    return new DefaultHttpReceiverContext(
        StandardHttpRequest.newBuilder().setUri("a.io").setMethod(method).build(),
        StandardHttpResponse.newBuilder());
  }
}
