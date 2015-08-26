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

package com.google.openbidder.server;

import com.google.common.base.Function;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.Service.State;
import com.google.common.util.concurrent.SettableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * {@link Service}-related helper functions and utilities.
 */
public final class ServiceUtil {
  private static final Logger logger = LoggerFactory.getLogger(ServiceUtil.class);

  private ServiceUtil() {
  }

  public static final Function<Service, Future<State>> START =
      new Function<Service, Future<Service.State>>() {
        @Override public Future<Service.State> apply(final Service service) {
          assert service != null;
          final SettableFuture<State> future = SettableFuture.create();
          service.addListener(new Service.Listener() {
            @Override public void running() {
              logger.info("Service {}: start succeeded", service.getClass().getSimpleName());
              future.set(Service.State.RUNNING);
            }
            @Override public void failed(State from, Throwable e) {
              logger.warn("Service " + service.getClass().getSimpleName() + ": start failed", e);
              future.set(Service.State.FAILED);
            }
          }, Executors.newSingleThreadExecutor());
          service.startAsync();
          return future;
        }
      };

  public static final Function<Service, Future<Service.State>> STOP =
      new Function<Service, Future<Service.State>>() {
        @Override public Future<Service.State> apply(final Service service) {
          assert service != null;
          final SettableFuture<Service.State> future = SettableFuture.create();
          service.addListener(new Service.Listener() {
            @Override public void terminated(Service.State from) {
              logger.info("Service {}: stop succeeded", service.getClass().getSimpleName());
              future.set(Service.State.TERMINATED);
            }
            @Override public void failed(State from, Throwable e) {
              logger.warn("Service " + service.getClass().getSimpleName() + ": stop failed", e);
              future.set(Service.State.FAILED);
            }
          }, Executors.newSingleThreadExecutor());
          service.stopAsync();
          return future;
        }
      };

  public static final Function<Service, String> NAME = new Function<Service, String>() {
    @Override public String apply(Service service) {
      assert service != null;
      return service.getClass().getSimpleName();
    }
  };
}
