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

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.openbidder.config.http.Feature;
import com.google.openbidder.http.HttpReceiver;

import java.util.Set;

import javax.annotation.Nullable;

/**
 * A {@link HttpReceiver} tied to a particular path and set of HTTP methods.
 */
public class HttpRoute {
  public static final Set<String> DELETE = ImmutableSet.of("DELETE");
  public static final Set<String> GET = ImmutableSet.of("GET");
  public static final Set<String> HEAD = ImmutableSet.of("HEAD");
  public static final Set<String> OPTIONS = ImmutableSet.of("OPTIONS");
  public static final Set<String> POST = ImmutableSet.of("POST");
  public static final Set<String> PUT = ImmutableSet.of("PUT");

  protected static final Set<Feature> NO_REQUIRED_FEATURES = ImmutableSet.of();

  private final String name;
  private final Set<String> methods;
  private final PathMatcher pathMatcher;
  private final HttpReceiver httpReceiver;
  private final ImmutableSet<Feature> requiredFeatures;

  protected HttpRoute(
      @Nullable String name,
      Set<String> methods,
      String pathSpec,
      HttpReceiver httpReceiver,
      Set<Feature> features) {

    this.name = name == null ? getClass().getSimpleName() : name;
    this.methods = ImmutableSet.copyOf(methods);
    this.pathMatcher = PathMatcherType.buildMatcher(Preconditions.checkNotNull(pathSpec));
    this.httpReceiver = Preconditions.checkNotNull(httpReceiver);
    this.requiredFeatures = ImmutableSet.copyOf(features);
  }

  public final String getName() {
    return name;
  }

  public final Set<String> getMethods() {
    return methods;
  }

  protected final PathMatcher getPathMatcher() {
    return pathMatcher;
  }

  public final String getPathSpec() {
    return pathMatcher.getPathSpec();
  }

  public final HttpReceiver getHttpReceiver() {
    return httpReceiver;
  }

  public final Set<Feature> getRequiredFeatures() {
    return requiredFeatures;
  }

  public final boolean hasRequiredFeatures(Set<Feature> enabledFeatures) {
    return !Sets.intersection(enabledFeatures, requiredFeatures).isEmpty();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(name, methods, pathMatcher, httpReceiver, requiredFeatures);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (!(obj instanceof HttpRoute)) {
      return false;
    }
    HttpRoute other = (HttpRoute) obj;
    return Objects.equal(name, other.name)
        && Objects.equal(methods, other.methods)
        && Objects.equal(pathMatcher, other.pathMatcher)
        && Objects.equal(httpReceiver, other.httpReceiver)
        && Objects.equal(requiredFeatures, other.requiredFeatures);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("name", name)
        .add("methods", methods)
        .add("pathMatcher", pathMatcher)
        .add("httpReceiver", httpReceiver)
        .add("features", requiredFeatures)
        .toString();
  }

  public static HttpRoute get(
      String name, String pathSpec, HttpReceiver httpReceiver, Feature... requiredFeatures) {
    return create(name, GET, pathSpec, httpReceiver, ImmutableSet.copyOf(requiredFeatures));
  }

  public static HttpRoute post(
      String name, String pathSpec, HttpReceiver httpReceiver, Feature... requiredFeatures) {
    return create(name, POST, pathSpec, httpReceiver, ImmutableSet.copyOf(requiredFeatures));
  }

  public static HttpRoute create(
      String name, Set<String> methods, String pathSpec,
      HttpReceiver httpReceiver, Set<Feature> requiredFeatures) {
    return new HttpRoute(name, methods, pathSpec, httpReceiver, requiredFeatures);
  }
}
