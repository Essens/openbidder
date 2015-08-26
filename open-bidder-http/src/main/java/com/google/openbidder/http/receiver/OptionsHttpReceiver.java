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

package com.google.openbidder.http.receiver;

import com.google.common.base.Joiner;
import com.google.openbidder.http.HttpReceiver;
import com.google.openbidder.http.HttpReceiverContext;

import java.util.Set;

/**
 * Receiver for OPTIONS.
 */
public class OptionsHttpReceiver implements HttpReceiver {

  @Override
  public void receive(HttpReceiverContext ctx) {
    @SuppressWarnings("unchecked")
    Set<String> allowedMethods = (Set<String>) ctx.attributes().get("allowedMethods");
    ctx.httpResponse().setHeader("Allow", Joiner.on(" ").join(allowedMethods));
  }
}
