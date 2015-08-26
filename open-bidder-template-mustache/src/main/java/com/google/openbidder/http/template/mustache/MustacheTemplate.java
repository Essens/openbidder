package com.google.openbidder.http.template.mustache;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.openbidder.http.template.AbstractTemplate;

import com.github.mustachejava.Mustache;

import java.io.Writer;
import java.util.Map;

/**
 * Mustache template wrapper.
 */
public class MustacheTemplate extends AbstractTemplate {
  private final Mustache mustache;

  public MustacheTemplate(String templateName, Mustache mustache) {
    super(templateName);
    this.mustache = checkNotNull(mustache);
  }

  @Override
  public void process(Writer writer, Map<String, Object> context) {
    mustache.execute(writer, context);
  }
}
