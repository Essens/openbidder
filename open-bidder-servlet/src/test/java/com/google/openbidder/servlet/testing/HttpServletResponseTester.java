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

package com.google.openbidder.servlet.testing;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;

/**
 * Encapsulates a HttpServletResponse mock, capturing its output for test validation.
 * This is a little cumbersome, but no great choice as the HttpServletResponse interface
 * has some "write-only" properties such as the state, so a plain mock won't cut it.
 */
@javax.annotation.ParametersAreNonnullByDefault
public class HttpServletResponseTester {
  private final HttpServletResponse httpResponse;
  private final ByteArrayOutputStream os = new ByteArrayOutputStream();
  private final ServletOutputStream servletOutputStream = new AbstractServletOutputStream() {
    @Override public void write(int b) throws IOException {
      osWrite(b);
    }
  };
  private final PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(os));
  private final Integer[] status = new Integer[1];
  private final String[] location = new String[1];
  private final String[] contentType = new String[1];

  /**
   * @throws IOException Unused
   */
  protected void osWrite(int b) throws IOException {
    os.write(b);
  }

  private HttpServletResponseTester(HttpServletResponse httpResponse) throws IOException {
    this.httpResponse = httpResponse;

    doAnswer(new Answer<Void>() {
      @Override public Void answer(InvocationOnMock invocation) {
        status[0] = (Integer) invocation.getArguments()[0];
        return null;
    }}).when(httpResponse).setStatus(anyInt());
    doAnswer(new Answer<Void>() {
      @Override public Void answer(InvocationOnMock invocation) {
        contentType[0] = (String) invocation.getArguments()[0];
        return null;
    }}).when(httpResponse).setContentType(anyString());
    doAnswer(new Answer<Void>() {
      @Override public Void answer(InvocationOnMock invocation) {
        status[0] = HttpServletResponse.SC_MOVED_TEMPORARILY;
        location[0] = (String) invocation.getArguments()[0];
        return null;
    }}).when(httpResponse).sendRedirect(anyString());
    when(httpResponse.getOutputStream()).thenReturn(servletOutputStream);
    when(httpResponse.getWriter()).thenReturn(printWriter);
    when(httpResponse.getCharacterEncoding()).thenReturn("UTF-8");
  }

  public final HttpServletResponse getHttpResponse() {
    return httpResponse;
  }

  public final int getStatus() {
    return status[0];
  }

  public final boolean hasStatus() {
    return status[0] != null;
  }

  public final String getLocation() {
    return location[0];
  }

  public final byte[] getOutput() {
    printWriter.flush();
    return os.toByteArray();
  }

  // TODO(opinali): change this to a full mock impl of HttpServletResponse
  public static HttpServletResponseTester create() throws IOException {
    return new HttpServletResponseTester(mock(HttpServletResponse.class));
  }

  public static HttpServletResponseTester createBadOutput() throws IOException {
    return new HttpServletResponseTester(mock(HttpServletResponse.class)) {
      @Override protected void osWrite(int b) throws IOException {
        throw new IOException("I don't like this integer");
      }
    };
  }

  static abstract class AbstractServletOutputStream extends ServletOutputStream {
    @Override public boolean isReady() {
      return true;
    }

    @Override public void setWriteListener(WriteListener writeListener) {
    }
  }
}
