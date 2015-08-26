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

package com.google.openbidder.http.route;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import com.google.openbidder.config.http.Feature;
import com.google.openbidder.config.http.Http404Receiver;
import com.google.openbidder.config.http.Http405Receiver;
import com.google.openbidder.config.http.HttpOptionsReceiver;
import com.google.openbidder.http.HttpReceiver;
import com.google.openbidder.http.HttpReceiverContext;
import com.google.openbidder.http.HttpRequest;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Routes an {@link HttpRequest} to an {@link HttpReceiver}.
 * The router is a receiver itself, so nested routing can be configured.
 */
public class HttpRouter implements HttpReceiver {
  private static final Logger logger = LoggerFactory.getLogger(HttpRouter.class);
  private static final String DUPLICATE_MESSAGE = "Conflicting HTTP handlers for %s %s: %s and %s";
  private static final ImmutableSet<String> NO_METHODS = ImmutableSet.of();

  private final ImmutableTable<PathMatcher, String, HttpRoute> routeTable;
  private final HttpReceiver notFoundReceiver;
  private final HttpReceiver methodNotAllowedReceiver;

  public HttpRouter(
      Set<HttpRoute> httpRoutes,
      Set<Feature> enabledFeatures,
      @HttpOptionsReceiver HttpReceiver optionsReceiver,
      @Http404Receiver HttpReceiver notFoundReceiver,
      @Http405Receiver HttpReceiver methodNotAllowedReceiver) {

    this.notFoundReceiver = notFoundReceiver;
    this.methodNotAllowedReceiver = methodNotAllowedReceiver;

    Table<PathMatcher, String, HttpRoute> routes = TreeBasedTable.create();
    for (HttpRoute httpRoute : httpRoutes) {
      if (httpRoute.hasRequiredFeatures(enabledFeatures) && !httpRoute.getMethods().isEmpty()) {
        PathMatcher matcher = httpRoute.getPathMatcher();
        routes.put(matcher, "OPTIONS", new HttpRoute("default_options", HttpRoute.OPTIONS,
            DefaultPathMatcher.DEFAULT_PATH, optionsReceiver, ImmutableSet.<Feature>of()));

        for (String method : httpRoute.getMethods()) {
          HttpRoute oldRoute = routes.put(matcher, method, httpRoute);

          if (oldRoute != null) {
            throw new IllegalArgumentException(String .format(DUPLICATE_MESSAGE,
                method, matcher.getPathSpec(), httpRoute.getName(), oldRoute.getName()));
          }
        }
      }
    }
    routeTable = ImmutableTable.copyOf(routes);
  }

  @Override
  public void receive(HttpReceiverContext ctx) {
    String requestPath;

    try {
      requestPath = ctx.httpRequest().getUri().getPath();
    } catch (IllegalStateException e) {
      logger.warn(e.toString());
      ctx.httpResponse().setStatusCode(HttpStatus.SC_BAD_REQUEST);
      return;
    }

    for (PathMatcher pathMatcher : routeTable.rowKeySet()) {
      if (pathMatcher.apply(requestPath)) {
        HttpRoute route = routeTable.get(pathMatcher, ctx.httpRequest().getMethod());
        HttpReceiver httpReceiver = route == null
            ? methodNotAllowedReceiver
            : route.getHttpReceiver();

        if (logger.isDebugEnabled() && route != null) {
          logger.debug("Receiver match: {} for: {}", route.getName(), ctx.httpRequest());
        }

        if ("OPTIONS".equals(ctx.httpRequest().getMethod())) {
          ctx.attributes().put("allowedMethods", getAllowedMethods(requestPath));
        }
       

        httpReceiver.receive(ctx);
        return;
      }
    }

    if (logger.isDebugEnabled()) {
      logger.debug("Receiver not found for: {}", ctx.httpRequest());
    }

    notFoundReceiver.receive(ctx);
  }

  public final ImmutableSet<String> getAllowedMethods(String requestPath) {
    for (PathMatcher pathMatcher : routeTable.rowKeySet()) {
      if (pathMatcher.apply(requestPath)) {
        return getAllowedMethods(pathMatcher);
      }
    }

    return NO_METHODS;
  }

  protected final ImmutableSet<String> getAllowedMethods(PathMatcher pathMatcher) {
    return routeTable.row(pathMatcher).keySet();
  }

  @Override
  public String toString() {
    return routeTable.toString();
  }
}
