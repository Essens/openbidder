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

package com.google.openbidder.api.interceptor;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.openbidder.util.ReflectionUtils;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Base implementation for {@link InterceptorController}.
 *
 * @param <Req> The request type for this controller
 * @param <Resp> The response type for this controller
 */
public class StandardInterceptorController<Req extends UserRequest, Resp extends UserResponse<Resp>>
    extends AbstractIdleService
    implements InterceptorController<Req, Resp> {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final ImmutableList<? extends Interceptor<Req, Resp>> interceptors;
  private final ImmutableMap<Interceptor<Req, Resp>, Timer> interceptorTimers;

  public StandardInterceptorController(List<? extends Interceptor<Req, Resp>> interceptors,
      MetricRegistry metricRegistry) {
    this.interceptors = ImmutableList.copyOf(interceptors);
    logger.info("Interceptors: {}",
        Lists.transform(this.interceptors, ReflectionUtils.TO_CLASSNAME));

    ImmutableMap.Builder<Interceptor<Req, Resp>, Timer> interceptorTimers = ImmutableMap.builder();
    for (Interceptor<Req, Resp> interceptor : getInterceptors()) {
      interceptorTimers.put(interceptor, metricRegistry.register(
          MetricRegistry.name(interceptor.getClass(), "execute"), new Timer()));
    }
    this.interceptorTimers = interceptorTimers.build();

    final ImmutableList<String> interceptorNames = ImmutableList.copyOf(Lists.transform(
        this.interceptors, new Function<Interceptor<Req, Resp>, String>() {
          @Override public String apply(Interceptor<Req, Resp> interceptor) {
            return ReflectionUtils.TO_PRETTYCLASSNAME.apply(interceptor);
          }}));

    metricRegistry.register(MetricRegistry.name(getClass(), "interceptors"),
        new Gauge<List<String>>() {
          @Override public List<String> getValue() {
            return interceptorNames;
          }});
  }

  @Override
  public @Nullable <R> R getResource(Class<R> resourceType, Interceptor<Req, Resp> interceptor) {
    checkNotNull(resourceType);
    checkNotNull(interceptor);

    if (resourceType == Timer.class) {
      @SuppressWarnings("unchecked")
      R resource = (R) interceptorTimers.get(interceptor);
      return resource;
    } else {
      return null;
    }
  }

  @Override
  public void onRequest(Req request, Resp response) {
    checkState(isRunning());
    InterceptorChain<Req, Resp> interceptorChain = InterceptorChain.create(this, request, response);
    interceptorChain.proceed();
  }

  @Override
  public ImmutableList<? extends Interceptor<Req, Resp>> getInterceptors() {
    return interceptors;
  }

  @Override
  protected void startUp() {
    logger.info("Starting up...");

    ReflectionUtils.invokePostConstruct(getInterceptors());

    logger.info("Startup complete.");
  }

  @Override
  protected void shutDown() {
    logger.info("Shutting down...");

    ReflectionUtils.invokePreDestroy(getInterceptors());

    logger.info("Shutdown complete.");
  }

  protected ToStringHelper toStringHelper() {
    return MoreObjects.toStringHelper(this).omitNullValues()
      .add("state", state())
      .add("interceptors", Lists.transform(interceptors, ReflectionUtils.TO_SIMPLECLASSNAME));
  }

  @Override
  public final String toString() {
    return toStringHelper().toString();
  }
}
