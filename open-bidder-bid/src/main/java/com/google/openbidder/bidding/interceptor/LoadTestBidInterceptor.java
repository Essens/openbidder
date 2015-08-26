/*
 * Copyright 2015 Google Inc. All Rights Reserved.
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

package com.google.openbidder.bidding.interceptor;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.google.inject.BindingAnnotation;
import com.google.inject.Inject;
import com.google.openbidder.api.bidding.BidInterceptor;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.interceptor.InterceptorChain;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import javax.annotation.Nullable;

/**
 * Useful interceptor for load tests. Simulates both I/O waits or other kinds of wait,
 * and CPU-intensive processing.
 */
public class LoadTestBidInterceptor implements BidInterceptor {
  private final Long delayMillis;
  private final Long workMillis;
  private static long sink;

  @Inject
  public LoadTestBidInterceptor(
      @Nullable @DelayTime Long delayMillis, @Nullable @WorkTime Long workMillis) {
    this.delayMillis = delayMillis;
    this.workMillis = workMillis;
  }

  @Override
  public void execute(InterceptorChain<BidRequest, BidResponse> chain) {
    if (delayMillis != null) {
      try {
        Thread.sleep(delayMillis);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

    if (workMillis != null) {
      try {
        long now = System.nanoTime();
        long end = now + workMillis * 1_000_000;
        Random rnd = SecureRandom.getInstance("SHA1PRNG");

        while (System.nanoTime() < end) {
          for (int i = 0; i < workMillis; ++i) {
            sink = sink * rnd.nextLong() ^ rnd.nextLong();
          }
        }

      } catch (NoSuchAlgorithmException e) {
      }
    }

    chain.proceed();
  }

  @BindingAnnotation
  @Target({ FIELD, PARAMETER, METHOD })
  @Retention(RUNTIME)
  public @interface DelayTime {
  }

  @BindingAnnotation
  @Target({ FIELD, PARAMETER, METHOD })
  @Retention(RUNTIME)
  public @interface WorkTime {
  }
}
