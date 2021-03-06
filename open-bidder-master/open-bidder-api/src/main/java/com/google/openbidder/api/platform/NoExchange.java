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

package com.google.openbidder.api.platform;

import com.google.inject.Binder;

/**
 * Dummy {@link Exchange} to be used by test code that doesn't depend on particular exchanges.
 */
public final class NoExchange extends Exchange {
  public static final NoExchange INSTANCE = new NoExchange();

  private NoExchange() {
    super("no-exchange");
  }

  @Override
  public Object newNativeResponse() {
    throw new UnsupportedOperationException();
  }

  public static class Module implements com.google.inject.Module {
    @Override public void configure(Binder binder) {
      binder.bind(Exchange.class).toInstance(INSTANCE);
    }
  }
}
