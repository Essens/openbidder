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

package com.google.openbidder.ui.rpc;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;

import javax.annotation.Nullable;

/**
 * A message in an RPC response intended for the user.
 */
public class RpcMessage {

  private final RpcMessageType messageType;
  private final String message;
  @Nullable private final String stackTrace;

  public RpcMessage(RpcMessageType messageType, String message) {
    this(messageType, message, null);
  }

  public RpcMessage(RpcMessageType messageType, String message, @Nullable String stackTrace) {
    this.messageType = checkNotNull(messageType);
    this.message = message;
    this.stackTrace = stackTrace;
  }

  public RpcMessageType getMessageType() {
    return messageType;
  }

  public String getMessage() {
    return message;
  }

  @Nullable
  public String getStackTrace() {
    return stackTrace;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("messageType", messageType)
        .add("message", message)
        .add("stackTrace", stackTrace)
        .toString();
  }
}
