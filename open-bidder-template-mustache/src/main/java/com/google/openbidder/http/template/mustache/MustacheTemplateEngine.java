package com.google.openbidder.http.template.mustache;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.openbidder.http.template.AbstractTemplateEngine;

import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.Reader;

/**
 * Template engine for Mustache.
 */
public class MustacheTemplateEngine extends AbstractTemplateEngine {
  private final MustacheFactory mustacheFactory;

  public MustacheTemplateEngine(MustacheFactory mustacheFactory) {
    this.mustacheFactory = checkNotNull(mustacheFactory);
  }

  @Override
  public MustacheTemplate compile(String templateName, Reader reader) {
    Mustache mustache = mustacheFactory.compile(reader, templateName);
    return new MustacheTemplate(templateName, mustache);
  }
}
