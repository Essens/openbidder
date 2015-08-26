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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.openbidder.http.HttpResponse;
import com.google.openbidder.http.receiver.DefaultHttpReceiverContext;
import com.google.openbidder.http.route.HttpRouter;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that services requests to an {@link HttpRouter}.
 */
public class ReceiverServlet extends HttpServlet {
  private final Map<Integer, HttpRouter> httpRouters;

  public ReceiverServlet(Map<Integer, HttpRouter> httpRouters) {
    this.httpRouters = checkNotNull(httpRouters);
  }

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    HttpResponse.Builder httpResponseBuilder = new ServletHttpResponseBuilder(resp);
    httpRouters.get(req.getServerPort()).receive(new DefaultHttpReceiverContext(
        new ServletHttpRequest(req), httpResponseBuilder));
    httpResponseBuilder.build();
  }
}
