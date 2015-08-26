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

package com.google.openbidder.http.template;

import com.google.common.base.Preconditions;
import com.google.openbidder.http.HttpReceiverContext;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Base {@link Template} implementation.
 */
public abstract class AbstractTemplate implements Template {

  private final String name;

  protected AbstractTemplate(String name) {
    this.name = Preconditions.checkNotNull(name);
  }

  @Override
  public final String getName() {
    return name;
  }

  @Override
  public void receive(HttpReceiverContext ctx) {
    process(ctx.httpResponse().contentWriter(), ctx.attributes());
  }

  @Override
  public String process(Map<String, Object> context) {
    try (StringWriter writer = new StringWriter()) {
      process(writer, context);
      return writer.toString();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public void process(OutputStream os, @Nullable Charset charset, Map<String, Object> context) {
    try (OutputStreamWriter writer = charset == null
        ? new OutputStreamWriter(os)
        : new OutputStreamWriter(os, charset)) {
      process(writer, context);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }
}
