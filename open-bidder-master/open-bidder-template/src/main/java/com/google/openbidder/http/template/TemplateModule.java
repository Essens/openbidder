package com.google.openbidder.http.template;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.openbidder.config.http.Http404Receiver;
import com.google.openbidder.config.http.Http405Receiver;
import com.google.openbidder.config.template.Http404Template;
import com.google.openbidder.config.template.Http405Template;
import com.google.openbidder.http.HttpReceiver;
import com.google.openbidder.http.receiver.MethodNotAllowedHttpReceiver;
import com.google.openbidder.http.receiver.NotFoundHttpReceiver;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import javax.inject.Singleton;

/**
 * Template module.
 */
@Parameters(separators = "=")
public class TemplateModule extends AbstractModule {

  @Parameter(names = "--template_http_404",
      description = "Template name for HTTP 404 not found responses")
  private String templateHttp404 = Http404Template.DEFAULT;

  @Parameter(names = "--template_http_405",
      description = "Template name for HTTP 405 method not allowed responses")
  private String templateHttp405 = Http405Template.DEFAULT;

  @Override
  protected void configure() {
    bind(String.class).annotatedWith(Http404Template.class).toInstance(templateHttp404);
    bind(HttpReceiver.class).annotatedWith(Http404Receiver.class)
        .to(NotFoundHttpReceiver.class).in(Scopes.SINGLETON);
    bind(String.class).annotatedWith(Http405Template.class).toInstance(templateHttp405);
    bind(HttpReceiver.class).annotatedWith(Http405Receiver.class)
        .to(MethodNotAllowedHttpReceiver.class).in(Scopes.SINGLETON);
  }

  @Provides
  @Singleton
  @Http404Template
  public Template provideNotFoundTemplate(
      @Http404Template String notFoundTemplate,
      TemplateEngine templateEngine) {

    return templateEngine.load(notFoundTemplate);
  }

  @Provides
  @Singleton
  @Http405Template
  public Template provideMethodNotAllowedTemplate(
      @Http405Template String methodNotAllowedTemplate,
      TemplateEngine templateEngine) {

    return templateEngine.load(methodNotAllowedTemplate);
  }
}
