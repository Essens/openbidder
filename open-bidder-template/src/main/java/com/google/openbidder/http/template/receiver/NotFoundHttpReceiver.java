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

package com.google.openbidder.http.template.receiver;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.openbidder.config.template.Http404Template;
import com.google.openbidder.http.HttpReceiver;
import com.google.openbidder.http.HttpReceiverContext;
import com.google.openbidder.http.template.Template;

import org.apache.http.HttpStatus;

import javax.inject.Inject;

public class NotFoundHttpReceiver implements HttpReceiver {
  private final Template templateHttp404;

  @Inject
  public NotFoundHttpReceiver(@Http404Template Template templateHttp404) {
    this.templateHttp404 = checkNotNull(templateHttp404);
  }

  @Override
  public void receive(HttpReceiverContext ctx) {
    ctx.httpResponse().setStatusCode(HttpStatus.SC_NOT_FOUND);
    if ("GET".equals(ctx.httpRequest().getMethod())) {
      // TODO(wshields): validate request Accept header
      templateHttp404.receive(ctx);
    }
  }
}
