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

package com.google.openbidder.ui.util.db;

import com.google.common.collect.ImmutableList;

import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.impl.translate.TranslatorFactory;
import com.googlecode.objectify.impl.translate.opt.BigDecimalLongTranslatorFactory;
import com.googlecode.objectify.impl.translate.opt.joda.JodaTimeTranslators;

import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

/**
 * An {@link ObjectifyFactory} that takes a list of entity classes to register and can be
 * injected into DAOs.
 */
public class SpringObjectifyFactory extends ObjectifyFactory {
  private List<Class<?>> entityClasses = Collections.emptyList();
  private List<TranslatorFactory<?, ?>> translatorFactories = Collections.emptyList();

  @PostConstruct
  public final void postConstruct() {
    JodaTimeTranslators.add(this);
    getTranslators().add(new BigDecimalLongTranslatorFactory(1));

    for (TranslatorFactory<?, ?> translator : translatorFactories) {
      getTranslators().add(translator);
    }

    for (Class<?> entityClass : entityClasses) {
      register(entityClass);
    }

    ObjectifyService.setFactory(this);
  }

  public void setEntityClasses(List<Class<?>> entityClasses) {
    this.entityClasses = ImmutableList.copyOf(entityClasses);
  }

  public void setTranslatorFactories(List<? extends TranslatorFactory<?, ?>> translatorFactories) {
    this.translatorFactories = ImmutableList.copyOf(translatorFactories);
  }
}
