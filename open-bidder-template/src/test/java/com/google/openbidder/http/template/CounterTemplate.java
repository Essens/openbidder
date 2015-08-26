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

import com.google.openbidder.http.HttpReceiverContext;

import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Map;

public class CounterTemplate extends AbstractTemplate {
  public int processCounter;
  public int receiveCounter;

  public CounterTemplate() {
    super("counter");
  }

  @Override public void process(Writer writer, Map<String, Object> context) {
    ++processCounter;
  }

  @Override
  public void process(OutputStream os, Charset charset, Map<String, Object> context) {
    ++processCounter;
    super.process(os, charset, context);
  }

  @Override
  public String process(Map<String, Object> context) {
    ++processCounter;
    return super.process(context);
  }

  @Override public void receive(HttpReceiverContext ctx) {
    ++receiveCounter;
    super.receive(ctx);
  }
}
