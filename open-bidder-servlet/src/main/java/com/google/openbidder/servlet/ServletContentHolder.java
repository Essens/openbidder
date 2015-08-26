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

package com.google.openbidder.servlet;

import com.google.openbidder.http.message.ContentHolder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Specialized {@link ContentHolder} for Servlets.
 */
public class ServletContentHolder extends ContentHolder {

  public ServletContentHolder(HttpServletRequest self, State state) {
    super(self, self.getContentLength(), state);
  }

  public ServletContentHolder(HttpServletResponse self, State state) {
    super(self, -1, state);
  }

  @Override
  public Charset getCharset() {
    Object self = self();
    String encoding = self instanceof HttpServletRequest
        ? ((HttpServletRequest) self).getCharacterEncoding()
        : ((HttpServletResponse) self).getCharacterEncoding();
    return encoding == null ? super.getCharset() : Charset.forName(encoding);
  }

  @Override
  protected InputStream toInputStream() {
    try {
      return ((HttpServletRequest) self()).getInputStream();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  protected BufferedReader toInputReader() {
    try {
      return ((HttpServletRequest) self()).getReader();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  protected OutputStream toOutputStream() {
    try {
      return ((HttpServletResponse) self()).getOutputStream();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  protected PrintWriter toOutputWriter() {
    try {
      return ((HttpServletResponse) self()).getWriter();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  protected boolean outputCloseShouldFlush() {
    return false;
  }
}
