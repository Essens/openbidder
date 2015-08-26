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

package com.google.openbidder.exchange.doubleclick.server;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nullable;

/**
 * Wrapper for an {@link InputStream}.
 */
public final class InputStreamWrapper extends InputStream implements AutoCloseable {
  private InputStream source;

  public void setSource(@Nullable InputStream source) {
    this.source = source;
  }

  public InputStream getSource() {
    return source;
  }

  @Override public int read() throws IOException {
    return source.read();
  }

  @Override public int read(byte[] b) throws IOException {
    return source.read(b);
  }

  @Override public int read(byte[] b, int off, int len) throws IOException {
    return source.read(b, off, len);
  }

  @Override public long skip(long n) throws IOException {
    return source.skip(n);
  }

  @Override public int available() throws IOException {
    return source.available();
  }

  @Override public void close() throws IOException {
    source.close();
    source = null;
  }

  @Override public synchronized void mark(int readlimit) {
    source.mark(readlimit);
  }

  @Override public synchronized void reset() throws IOException {
    source.reset();
  }

  @Override public boolean markSupported() {
    return source.markSupported();
  }
}
