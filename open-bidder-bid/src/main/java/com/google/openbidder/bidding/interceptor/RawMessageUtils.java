/*
 * Copyright 2015 Google Inc. All Rights Reserved.
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

package com.google.openbidder.bidding.interceptor;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Utilities to manipulate "raw" messages.
 */
public class RawMessageUtils {

  public static <M extends Message> boolean matches(M testMsg, M matchMsg) {
    return matches(testMsg, matchMsg, null);
  }

  @SuppressWarnings("unchecked")
  private static <M extends Message> boolean matches(M testMsg, M matchMsg,
      @Nullable List<Message> tempReuse) {
    List<Message> temp = tempReuse;

    for (Map.Entry<FieldDescriptor, Object> fdo : matchMsg.getAllFields().entrySet()) {
      FieldDescriptor fd = fdo.getKey();
      Object testField = testMsg.getField(fd);
      Object matchField = fdo.getValue();

      if (fd.getType() == FieldDescriptor.Type.MESSAGE) {
        if (fd.isRepeated()) {
          if (!matches((Collection<Message>) testField, (Collection<Message>) matchField,
              temp = (temp == null ? new ArrayList<Message>() : temp))) {
            return false;
          }
        } else {
          if (!(testMsg.hasField(fd) && matches((Message) testField, (Message) matchField, temp))) {
            return false;
          }
        }
      } else {
        if (fd.isRepeated()) {
          if (!((Collection<?>) testField).containsAll((Collection<?>) matchField)) {
            return false;
          }
        } else {
          if (!(testMsg.hasField(fd) && testField.equals(matchField))) {
            return false;
          }
        }
      }
    }
    return true;
  }

  private static boolean matches(Collection<Message> testMsgs, Collection<Message> matchMsgs,
      List<Message> temp) {
    temp.clear();
    temp.addAll(testMsgs);
    for (Message matchMsg : matchMsgs) {
      boolean found = false;
      for (Iterator<Message> testIter = temp.iterator(); testIter.hasNext(); ) {
        if (matches(testIter.next(), matchMsg)) {
          testIter.remove();
          found = true;
          break;
        }
      }
      if (!found) {
        return false;
      }
    }
    return true;
  }
}
