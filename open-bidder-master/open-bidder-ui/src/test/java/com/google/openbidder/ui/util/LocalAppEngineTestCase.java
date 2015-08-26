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

package com.google.openbidder.ui.util;


import com.google.openbidder.ui.entity.AuthorizationToken;
import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.entity.Subscriber;
import com.google.openbidder.ui.entity.UserPreference;

import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.impl.translate.opt.BigDecimalLongTranslatorFactory;
import com.googlecode.objectify.impl.translate.opt.joda.JodaTimeTranslators;

import org.junit.Before;

/**
 * {@link AppEngineTestCase} that uses a locally created
 * {@link com.googlecode.objectify.ObjectifyFactory}.
 */
public class LocalAppEngineTestCase extends AppEngineTestCase {

  protected ObjectifyFactory factory;

  @Before
  public void setupFactory() {
    factory = new TestObjectifyFactory();
    JodaTimeTranslators.add(factory);
    factory.getTranslators().add(new BigDecimalLongTranslatorFactory(1));
    factory.register(AuthorizationToken.class);
    factory.register(Project.class);
    factory.register(Subscriber.class);
    factory.register(UserPreference.class);
  }

  @Override
  protected ObjectifyFactory getFactory() {
    return factory;
  }
}
