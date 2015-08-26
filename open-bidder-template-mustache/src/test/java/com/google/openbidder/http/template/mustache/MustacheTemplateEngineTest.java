package com.google.openbidder.http.template.mustache;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;

import com.github.mustachejava.DefaultMustacheFactory;

import org.junit.Test;

import java.io.StringReader;

/**
 * Tests for {@link MustacheTemplateEngine}.
 */
public class MustacheTemplateEngineTest {

  @Test
  public void testEngine() {
    MustacheTemplateEngine engine = new MustacheTemplateEngine(new DefaultMustacheFactory());
    MustacheTemplate template = engine.compile("test", new StringReader("??{{key}}!!"));
    assertEquals(
        "??value!!",
        template.process(ImmutableMap.<String, Object>of("key", "value")));
  }
}
