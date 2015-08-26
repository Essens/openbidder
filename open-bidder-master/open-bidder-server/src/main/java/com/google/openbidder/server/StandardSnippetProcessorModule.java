package com.google.openbidder.server;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.openbidder.api.snippet.StandardSnippetProcessor;
import com.google.openbidder.config.bid.CallbackUrl;
import com.google.openbidder.config.bid.ClickUrl;
import com.google.openbidder.config.bid.ImpressionUrl;
import com.google.openrtb.snippet.SnippetProcessor;

import javax.inject.Singleton;

/**
 * Convenience for exchanges that don't need a custom {@link SnippetProcessor}.
 *
 * Note: We cannot simply put the injection annotations in {@link StandardSnippetProcessor}
 * because we don't want to make the api module depend on either Guice or the config module.
 */
public class StandardSnippetProcessorModule extends AbstractModule {
  @Override protected void configure() {
  }

  @Provides
  @Singleton
  public SnippetProcessor provideSnippetProcessor (
      @CallbackUrl String callbackUrl,
      @ImpressionUrl String impressionUrl,
      @ClickUrl String clickUrl) {
    return new StandardSnippetProcessor(callbackUrl, impressionUrl, clickUrl);
  }
}
