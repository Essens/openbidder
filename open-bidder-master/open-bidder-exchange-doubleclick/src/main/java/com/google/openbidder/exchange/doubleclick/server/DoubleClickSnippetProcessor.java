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

package com.google.openbidder.exchange.doubleclick.server;

import com.google.doubleclick.util.DoubleClickMacros;
import com.google.openbidder.api.snippet.StandardSnippetProcessor;
import com.google.openbidder.config.bid.CallbackUrl;
import com.google.openbidder.config.bid.ClickUrl;
import com.google.openbidder.config.bid.ImpressionUrl;
import com.google.openrtb.snippet.OpenRtbMacros;
import com.google.openrtb.snippet.SnippetMacroType;
import com.google.openrtb.snippet.SnippetProcessor;
import com.google.openrtb.snippet.SnippetProcessorContext;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * {@link SnippetProcessor} for DoubleClick Ad Exchange.
 */
@Singleton
public class DoubleClickSnippetProcessor extends StandardSnippetProcessor {
  public static final DoubleClickSnippetProcessor DC_NULL =
      new DoubleClickSnippetProcessor(null, null, null) {
    @Override public String process(SnippetProcessorContext ctx, String snippet) {
      return SnippetProcessor.NULL.process(ctx, snippet);
    }
  };

  @Inject
  public DoubleClickSnippetProcessor(
      @CallbackUrl String callbackUrl,
      @ImpressionUrl String impressionUrl,
      @ClickUrl String clickUrl) {
    super(callbackUrl, impressionUrl, clickUrl);
  }

  @Override protected void processMacroAt(SnippetProcessorContext ctx,
      StringBuilder sb, SnippetMacroType macroDef) {
    if (macroDef instanceof OpenRtbMacros) {
      switch ((OpenRtbMacros) macroDef) {
        case AUCTION_PRICE: {
          sb.append(DoubleClickMacros.WINNING_PRICE.key());
          return;
        }

        default:
      }
    }

    super.processMacroAt(ctx, sb, macroDef);
  }
}
