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

package com.google.openbidder.metrics.http;

import com.google.common.net.MediaType;
import com.google.openbidder.http.HttpReceiver;
import com.google.openbidder.http.HttpReceiverContext;

/**
 * Processes ping request.
 */
public class PingHttpReceiver implements HttpReceiver {

  @Override
  public void receive(HttpReceiverContext ctx) {
    ctx.httpResponse()
        .setStatusOk()
        .setMediaType(MediaType.PLAIN_TEXT_UTF_8)
        .setHeader("Cache-Control", "must-revalidate,no-cache,no-store")
        .printContent("pong");
  }
}
