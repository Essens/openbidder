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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Optional base {@link TemplateEngine} implementation.
 */
public abstract class AbstractTemplateEngine implements TemplateEngine {

  @Override
  public Template load(String templateName, Charset charset) {
    checkNotNull(charset);
    URL url = Resources.getResource(templateName);
    try {
      return compile(templateName, new InputStreamReader(url.openStream(), charset));
    } catch (IOException e) {
      throw new IllegalArgumentException("Invalid resource " + templateName, e);
    }
  }

  @Override
  public Template load(String templateName) {
    return load(templateName, Charsets.UTF_8);
  }
}
