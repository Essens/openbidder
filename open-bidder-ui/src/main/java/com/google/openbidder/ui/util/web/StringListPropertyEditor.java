/*
 * Copyright 2012 Google Inc. All Rights Reserved.
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

package com.google.openbidder.ui.util.web;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

import java.beans.PropertyEditorSupport;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Custom {@link java.beans.PropertyEditor} for a list of Strings, with a custom separator that
 * defaults to a new-line.
 */
public class StringListPropertyEditor extends PropertyEditorSupport {

  private static final String DEFAULT_SEPARATOR = "\n";

  private final Pattern separator;
  private final String joiner;

  public StringListPropertyEditor() {
    this(DEFAULT_SEPARATOR);
  }

  public StringListPropertyEditor(String separator) {
    this(Pattern.compile(Pattern.quote(separator)), separator);
  }

  public StringListPropertyEditor(Pattern separator, String joiner) {
    this.separator = checkNotNull(separator);
    this.joiner = checkNotNull(joiner);
  }

  @Override
  public void setAsText(String text) {
    if (Strings.isNullOrEmpty(text)) {
      setValue(ImmutableList.of());
    } else {
      setValue(ImmutableList.copyOf(Splitter.on(separator)
          .omitEmptyStrings().trimResults().split(text)));
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public String getAsText() {
    return Joiner.on(joiner).join((List<String>) getValue());
  }
}
