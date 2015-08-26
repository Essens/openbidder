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

package com.google.openbidder.http;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Objects;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enum-style class for a protocol.
 */
public class Protocol {
  public static final Protocol HTTP_1_0 = new Protocol("HTTP", 1, 0);
  public static final Protocol HTTP_1_1 = new Protocol("HTTP", 1, 1);

  private static final Pattern RE_PROTOCOL = Pattern.compile("^(\\w+)/(\\d+)\\.(\\d+)$");

  private final String name;
  private final int majorVersion;
  private final int minorVersion;
  private final String text;

  private Protocol(String name, int majorVersion, int minorVersion) {
    checkArgument(majorVersion >= 0,
        "Expected non-negative majorVersion, got " + majorVersion);
    checkArgument(minorVersion >= 0,
        "Expected non-negative minorVersion, got " + minorVersion);
    this.name = name.trim().toUpperCase();
    this.majorVersion = majorVersion;
    this.minorVersion = minorVersion;
    this.text = String.format("%s/%d.%d", name, majorVersion, minorVersion);
  }

  public final String name() {
    return name;
  }

  public final int majorVersion() {
    return majorVersion;
  }

  public final int minorVersion() {
    return minorVersion;
  }

  public final String text() {
    return text;
  }

  @Override
  public int hashCode() {
    return text.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (!(obj instanceof Protocol)) {
      return false;
    }
    Protocol other = (Protocol) obj;
    return Objects.equal(text, other.text);
  }

  @Override
  public String toString() {
    return text;
  }

  public static Protocol decode(String protocolText) {
    if (HTTP_1_1.text.equals(protocolText)) {
      return HTTP_1_1;
    } else if (HTTP_1_0.text.equals(protocolText)) {
      return HTTP_1_0;
    } else {
      Matcher matcher = RE_PROTOCOL.matcher(protocolText);
      if (!matcher.matches()) {
        throw new IllegalArgumentException("Unknown protocol format: " + protocolText);
      }
      return new Protocol(
          matcher.group(1),
          Integer.parseInt(matcher.group(2)),
          Integer.parseInt(matcher.group(3)));
    }
  }
}
