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

package com.google.openbidder.ui.resource.support;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import org.springframework.http.HttpMethod;

import java.util.EnumSet;
import java.util.Set;

/**
 * Resource related API methods.
 */
public enum ResourceMethod {
  LIST(HttpMethod.GET),
  GET(HttpMethod.GET),
  CREATE(HttpMethod.POST),
  UPDATE(HttpMethod.PUT),
  DELETE(HttpMethod.DELETE);

  private static final Function<ResourceMethod, HttpMethod> TO_HTTP_METHOD =
      new Function<ResourceMethod, HttpMethod>() {
        @Override
        public HttpMethod apply(ResourceMethod resourceMethod) {
          return resourceMethod.getHttpMethod();
        }
      };

  private final HttpMethod httpMethod;

  private ResourceMethod(HttpMethod httpMethod) {
    this.httpMethod = httpMethod;
  }

  public HttpMethod getHttpMethod() {
    return httpMethod;
  }

  public static EnumSet<HttpMethod> toHttpMethod(Set<ResourceMethod> resourceMethods) {
    return EnumSet.copyOf(Collections2.transform(resourceMethods, TO_HTTP_METHOD));
  }
}
