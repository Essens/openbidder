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

package com.google.openbidder.api.interceptor;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.openbidder.util.ReflectionUtils;

import com.codahale.metrics.MetricRegistry;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Implements the Composite design pattern for {@link Interceptor}s. This makes easy to create
 * groups of interceptors that are easy to configure as part of a larger chain. It's also useful
 * when a group of interceptors share some of their configuration or have inter-dependent
 * lifecycles, still you don't want to merge them in a single monolithic interceptor.
 * Each composite should derive this class, and also implement some interceptor interface.
 * <p>
 * Different from regular {@link Interceptor}s, component interceptors cannot use dependency
 * injection, but they won't need that because the interceptor construction will be performed
 * by the composite, so you can inject necessary confogurations there and pass it over to the
 * constructors of components.  Lifecycle annotations ({@code PostConstruct} / {@code PreDestroy})
 * will work normally, subordinated to the composite's lifecycle.
 *
 * @param <Req> The request type for this interceptor
 * @param <Resp> The response type for this interceptor
 */
public abstract class CompositeInterceptor<Req extends UserRequest, Resp extends UserResponse<Resp>>
    implements Interceptor<Req, Resp> {

  private final ImmutableList<? extends Interceptor<Req, Resp>> componentInterceptors;
  private final MetricRegistry metricRegistry;

  protected CompositeInterceptor(List<? extends Interceptor<Req, Resp>> componentInterceptors,
      MetricRegistry metricRegistry) {
    this.componentInterceptors = ImmutableList.copyOf(componentInterceptors);
    this.metricRegistry = metricRegistry;
  }

  public ImmutableList<? extends Interceptor<Req, Resp>> getComponentInterceptors() {
    return componentInterceptors;
  }

  @Override
  public void execute(final InterceptorChain<Req, Resp> chain) {
    final InterceptorChain<Req, Resp> componentChain = InterceptorChain.create(
        new CompositeController<>(componentInterceptors, metricRegistry),
        chain.request(), chain.response());
    componentChain.proceed();
    chain.proceed();
  }

  @PostConstruct
  public void postConstruct() {
    ReflectionUtils.invokePostConstruct(componentInterceptors);
  }

  @PreDestroy
  public void preDestroy() {
    ReflectionUtils.invokePreDestroy(componentInterceptors);
  }

  @Override
  public final String toString() {
    return toStringHelper().toString();
  }

  protected ToStringHelper toStringHelper() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("componentInterceptors",
            Lists.transform(componentInterceptors, ReflectionUtils.TO_SIMPLECLASSNAME));
  }

  private static final class CompositeController
      <Req extends UserRequest, Resp extends UserResponse<Resp>>
      extends StandardInterceptorController<Req, Resp> {
    private CompositeController(
        List<? extends Interceptor<Req, Resp>> interceptors, MetricRegistry metricRegistry) {
      super(interceptors, metricRegistry);
    }
  }
}
