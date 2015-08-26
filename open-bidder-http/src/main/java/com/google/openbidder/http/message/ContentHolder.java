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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.net.MediaType;
import com.google.protobuf.ByteString;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;

/**
 * Handles message content, initially for message builders and then for built messages.
 */
public class ContentHolder {
  /** Source or owner of the content, or the content itself if there's no parent object. */
  private final Object self;
  /** Content's default charset (can also be provided dynamically by overriding useCharset(). */
  private Charset defaultCharset = Charsets.UTF_8;
  /** Content's length if known (>= 0), or -1 if unknown. */
  private int length;
  private State state;
  private InputStream is;
  private OutputStream os;
  private PrintWriter writer;
  private BufferedReader reader;

  public ContentHolder(Object self, int length, State state) {
    this.self = checkNotNull(self);
    setLength(length);
    this.state = checkNotNull(state);
  }

  public final Object self() {
    return self;
  }

  public final Charset getDefaultCharset() {
    return defaultCharset;
  }

  public final void setDefaultCharset(Charset defaultCharset) {
    this.defaultCharset = checkNotNull(defaultCharset);
  }

  public final int getLength() {
    return length;
  }

  public final void setLength(int length) {
    checkArgument(length >= -1);
    this.length = length;
  }

  public final State getState() {
    return state;
  }

  public final IllegalStateException badState(String message) {
    throw new IllegalStateException(message + ": " + toString());
  }

  public Charset getCharset() {
    if (self instanceof HttpMessageOrBuilder) {
      MediaType mediaType = ((HttpMessageOrBuilder) self).getMediaType();
      if (mediaType != null) {
        Charset charset = mediaType.charset().orNull();
        if (charset != null) {
          return charset;
        }
      }
    }
    return defaultCharset;
  }

  // Output methods (used by builders)

  public final OutputStream outputOut() {
    switch (state) {
      case OUTPUT_STREAM:
        break;
      case NONE:
        os = newOutputStream();
        break;
      case OUTPUT:
        os = toOutputStream();
        break;
      case OUTPUT_WRITER:
        throw badState("Cannot use the OutputStream after using the PrintWriter");
      default:
        throw badState("Not in output state");
    }
    state = State.OUTPUT_STREAM;
    return os;
  }

  protected OutputStream toOutputStream() {
    return os == null ? (OutputStream) self : os;
  }

  protected OutputStream newOutputStream() {
    return ByteString.newOutput();
  }

  public final PrintWriter outputWriter() {
    switch (state) {
      case OUTPUT_WRITER:
        break;
      case NONE:
        os = newOutputStream();
        //$FALL-THROUGH$
      case OUTPUT:
        writer = toOutputWriter();
        break;
      case OUTPUT_STREAM:
        throw badState("Cannot use the PrintWriter after using the OutputStream");
      default:
        throw badState("Not in output state");
    }
    state = State.OUTPUT_WRITER;
    return writer;
  }

  protected PrintWriter toOutputWriter() {
    return new PrintWriter(new OutputStreamWriter(toOutputStream(), getCharset()), false);
  }

  public void outputClose() {
    switch (state) {
      case NONE:
        break;
      case OUTPUT:
      case OUTPUT_STREAM:
      case OUTPUT_WRITER:
        if (outputCloseShouldFlush()) {
          if (writer != null && writer.checkError()) { // also flushes
            throw badState("Error in the PrintWriter");
          }
          if (os != null) {
            try {
              os.flush();
            } catch (IOException e) {
              throw new IllegalStateException(e);
            }
          }
        }
        writer = null;
        break;
      default:
        throw badState("Not in output state");
    }
    state = State.OUTPUT_CLOSED;
  }

  protected boolean outputCloseShouldFlush() {
    return true;
  }

  // Input methods (used by messages)

  protected InputStream inputFromOutput(OutputStream os) {
    if (os instanceof ByteString.Output) {
      ByteString bs = ((ByteString.Output) os).toByteString();
      length = bs.size();
      return bs.newInput();
    } else {
      length = 0;
      return newInputStream();
    }
  }

  protected InputStream newInputStream() {
    return ByteString.EMPTY.newInput();
  }

  public final InputStream inputIn() {
    switch (state) {
      case INPUT_STREAM:
        break;
      case INPUT_READER:
        throw badState("Cannot use the InputStream after using the BufferedReader");
      case OUTPUT_CLOSED:
        is = inputFromOutput(os);
        os = null;
        break;
      case NONE:
        is = newInputStream();
        break;
      case INPUT:
        is = toInputStream();
        break;
      default:
        throw badState("Not in input state");
    }
    state = State.INPUT_STREAM;
    return is;
  }

  protected InputStream toInputStream() {
    return is == null ? (InputStream) self : is;
  }

  public final BufferedReader inputReader() {
    if (reader == null) {
      switch (state) {
        case INPUT_READER:
          break;
        case NONE:
          is = newInputStream();
          reader = toInputReader();
          break;
        case OUTPUT_CLOSED:
          is = inputFromOutput(os);
          os = null;
          //$FALL-THROUGH$
        case INPUT:
          reader = toInputReader();
          break;
        case INPUT_STREAM:
          throw badState("Cannot use the BufferedReader after using the InputStream");
        default:
          throw badState("Not in input state");
      }
    }
    state = State.INPUT_READER;
    return reader;
  }

  protected BufferedReader toInputReader() {
    return new BufferedReader(new InputStreamReader(toInputStream(), getCharset()));
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("self", toClass(self))
        .add("defaultCharset", defaultCharset)
        .add("length", length == -1 ? null : length)
        .add("state", state)
        .add("os", toClass(os))
        .add("is", toClass(is))
        .add("writer", toClass(writer))
        .add("reader", toClass(reader))
        .toString();
  }

  private static String toClass(Object obj) {
    return obj == null ? null : "<" + obj.getClass().getSimpleName() + '>';
  }

  public static enum State {
    NONE,
    OUTPUT,
    OUTPUT_STREAM,
    OUTPUT_WRITER,
    OUTPUT_CLOSED,
    INPUT,
    INPUT_STREAM,
    INPUT_READER,
  }
}
