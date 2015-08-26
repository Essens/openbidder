package com.google.openbidder.http.template.mustache;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.openbidder.http.template.TemplateEngine;

import com.github.mustachejava.DefaultMustacheFactory;

import javax.inject.Singleton;

/**
 * Guice module for template engine bindings.
 */
public class MustacheModule extends AbstractModule {

  @Override
  protected void configure() {
  }

  @Provides
  @Singleton
  public TemplateEngine provideMustacheTemplateEngine() {
    return new MustacheTemplateEngine(new DefaultMustacheFactory());
  }
}
