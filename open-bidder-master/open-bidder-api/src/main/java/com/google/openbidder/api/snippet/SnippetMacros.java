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

package com.google.openbidder.api.snippet;

import com.google.common.collect.ImmutableMap;
import com.google.openrtb.snippet.SnippetMacroType;

import java.util.Map;

/**
 * Macros for snippet generation.
 * <p>
 * The HTML snippets (adContent property) support a number of macros, both from OpenBidder and
 * possibly from the exchange. See the exchange documentation about its snippet macros; OpenBidder
 * will not process or otherwise interfere with these. But OpenBidder will process its own macros,
 * so that the snippet sent to the exchange will be already have those macros expanded. See the
 * javadocs for the enumerated values, for each macro text {@link #key()} and description.
 * <p>
 * Additionally, you can use the syntax %{...}% for URL encoding. Nesting can be used, e.g.
 * %{A%{B}%}% will encode A and doubly-encode B. This nesting is typically necessary when URLs
 * have parameter that contain other URLs, so each server decodes and redirects to the next URL.
 */
public enum SnippetMacros implements SnippetMacroType {

  /**
   * Base URL for the bidder zone, ex.: "http://myzone.mybidders.com".
   * The value comes from the system property {@code OpenBidder.CallbackUrl}.
   */
  OB_CALLBACK_URL("${OB_CALLBACK_URL}"),
  /**
   * Base URL for impression callback, ex.: "http://myzone.mybidders.com/impression".
   * Can be based on the callback URL macro, like: "${OB_CALLBACK_URL}/impression"
   * The value comes from the configuration property {@code Impression.Url}.
   */
  OB_IMPRESSION_URL("${OB_IMPRESSION_URL}"),
  /**
   * Base URL for click callback, ex.: "http://myzone.mybidders.com/click".
   * Can be based on the callback URL macro, like: "OB_CALLBACK_URL/click"
   * The value comes from the configuration property {@code Click.Url}.
   */
  OB_CLICK_URL("${OB_CLICK_URL}"),
  /**
   * Creative URL, ex.: "http://mycontent.com/creative.png".
   * The value comes from the ad, if declared (an error will be raised if not).
   */
  OB_AD_CREATIVE_URL("${OB_AD_CREATIVE_URL}"),
  /**
   * Ad's clickthrough URL.
   */
  OB_AD_CLICKTHROUGH_URL("${OB_AD_CLICKTHROUGH_URL}"),
  /**
   * With of the ad slot / creative.
   */
  OB_AD_WIDTH("${OB_AD_WIDTH}"),
  /**
   * With of the ad slot / creative.
   */
  OB_AD_HEIGHT("${OB_AD_HEIGHT}"),
  ;

  private static final Map<String, SnippetMacros> LOOKUP;

  static {
    ImmutableMap.Builder<String, SnippetMacros> builder = ImmutableMap.builder();
    for (SnippetMacros snippetMacro : values()) {
      builder.put(snippetMacro.key, snippetMacro);
    }
    LOOKUP = builder.build();
  }

  private final String key;

  private SnippetMacros(String key) {
    this.key = key;
  }

  /**
   * Returns the key for this macro (string that will be substituted when the macro is processed).
   */
  @Override
  public final String key() {
    return key;
  }

  /**
   * @return {@link SnippetMacros} instance by key name
   * @throws IllegalArgumentException if no matching macro found
   */
  public static SnippetMacros lookupByKey(String macroName) {
    SnippetMacros macro = LOOKUP.get(macroName);
    if (macro == null) {
      throw new IllegalArgumentException("Unknown macro: " + macroName);
    }
    return macro;
  }
}
