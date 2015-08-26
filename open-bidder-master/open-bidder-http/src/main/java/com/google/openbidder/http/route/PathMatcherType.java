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

import com.google.common.base.Preconditions;

/**
 * Based on the Servlet specification. Used for relative ordering of paths from highest
 * to lowest: {@link #EXACT}, {@link #PREFIX}, {@link #SUFFIX}, {@link #DEFAULT},
 * {@link #ROOT}.
 */
enum PathMatcherType {
  ROOT,
  DEFAULT,
  EXACT,
  PREFIX,
  SUFFIX;

  public static PathMatcher buildMatcher(String pathSpec) {
    Preconditions.checkNotNull(pathSpec);
    if (pathSpec.equals(DefaultPathMatcher.DEFAULT_PATH)) {
      return new DefaultPathMatcher();
    } else if (pathSpec.equals(RootPathMatcher.ROOT_PATH)) {
      return new RootPathMatcher();
    } else if (pathSpec.startsWith("/") && pathSpec.endsWith("/*")) {
      return new PrefixPathMatcher(pathSpec.substring(0, pathSpec.length() - 1));
    } else if (pathSpec.startsWith("*.") && pathSpec.length() > 2) {
      return new SuffixPathMatcher(pathSpec.substring(1));
    } else {
      return new ExactPathMatcher(pathSpec);
    }
  }
}
