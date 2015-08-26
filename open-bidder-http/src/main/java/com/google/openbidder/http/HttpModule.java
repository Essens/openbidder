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

package com.google.openbidder.http;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import com.google.openbidder.config.http.HttpOptionsReceiver;
import com.google.openbidder.http.receiver.OptionsHttpReceiver;
import com.google.openbidder.http.route.HttpRoute;

/**
 * HTTP bindings.
 */
public class HttpModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(HttpReceiver.class).annotatedWith(HttpOptionsReceiver.class)
        .to(OptionsHttpReceiver.class).in(Scopes.SINGLETON);
    Multibinder.newSetBinder(binder(), HttpRoute.class);
  }
}
