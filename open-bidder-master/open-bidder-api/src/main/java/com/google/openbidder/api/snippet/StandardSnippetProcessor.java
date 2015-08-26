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

package com.google.openbidder.api.snippet;

import static java.util.Arrays.asList;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.openbidder.api.openrtb.ObExt;
import com.google.openrtb.OpenRtb.BidRequest.ImpressionOrBuilder;
import com.google.openrtb.snippet.OpenRtbSnippetProcessor;
import com.google.openrtb.snippet.SnippetMacroType;
import com.google.openrtb.snippet.SnippetProcessor;
import com.google.openrtb.snippet.SnippetProcessorContext;
import com.google.openrtb.snippet.UndefinedMacroException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Singleton;

/**
 * Default {@link SnippetProcessor}.
 */
@Singleton
public class StandardSnippetProcessor extends OpenRtbSnippetProcessor {
  private static final Logger logger = LoggerFactory.getLogger(SnippetProcessor.class);
  public static final StandardSnippetProcessor STD_NULL =
      new StandardSnippetProcessor(null, null, null) {
    @Override public String process(SnippetProcessorContext ctx, String snippet) {
      return SnippetProcessor.NULL.process(ctx, snippet);
    }
  };

  private final @Nullable String callbackUrl;
  private final @Nullable String impressionUrl;
  private final @Nullable String clickUrl;

  @Override protected List<SnippetMacroType> registerMacros() {
    return ImmutableList.<SnippetMacroType>builder()
        .addAll(super.registerMacros())
        .addAll(asList(SnippetMacros.values()))
        .build();
  }

  /**
   * Creates a processor for some configuration.
   */
  public StandardSnippetProcessor(String callbackUrl, String impressionUrl, String clickUrl) {
    this.callbackUrl = Strings.isNullOrEmpty(callbackUrl) ? null : callbackUrl;
    this.clickUrl = preprocess(SnippetMacros.OB_CALLBACK_URL, this.callbackUrl, clickUrl);
    this.impressionUrl = preprocess(SnippetMacros.OB_CALLBACK_URL, this.callbackUrl, impressionUrl);
  }

  @Override
  protected void processMacroAt(SnippetProcessorContext ctx,
      StringBuilder sb, SnippetMacroType macroDef) {
    if (macroDef instanceof SnippetMacros) {
      switch ((SnippetMacros) macroDef) {
        case OB_CALLBACK_URL:
          if (Strings.isNullOrEmpty(callbackUrl)) {
            throw new UndefinedMacroException(SnippetMacros.OB_CALLBACK_URL);
          } else {
            sb.append(callbackUrl);
          }
          break;

        case OB_IMPRESSION_URL:
          if (Strings.isNullOrEmpty(impressionUrl)) {
            throw new UndefinedMacroException(SnippetMacros.OB_IMPRESSION_URL);
          } else {
            generateUrl(
                impressionUrl, ctx.bid().getExtension(ObExt.bid).getImpressionParameterList(), sb);
          }
          break;

        case OB_CLICK_URL:
          if (Strings.isNullOrEmpty(clickUrl)) {
            throw new UndefinedMacroException(SnippetMacros.OB_CLICK_URL);
          } else {
            generateUrl(
                clickUrl, ctx.bid().getExtension(ObExt.bid).getClickParameterList(), sb);
          }
          break;

        case OB_AD_CLICKTHROUGH_URL:
          if (ctx.bid().getExtension(ObExt.bid).getClickThroughUrlCount() == 0) {
            throw new UndefinedMacroException(SnippetMacros.OB_AD_CLICKTHROUGH_URL,
                "Bid's ad clickThroughUrl is not set");
          } else {
            sb.append(ctx.bid().getExtension(ObExt.bid).getClickThroughUrl(0));
          }
          break;

        case OB_AD_CREATIVE_URL:
          if (!ctx.bid().hasIurl()) {
            throw new UndefinedMacroException(SnippetMacros.OB_AD_CREATIVE_URL,
                "Bid's ad creativeUrl is not set");
          } else {
            sb.append(ctx.bid().getIurl());
          }
          break;

        case OB_AD_WIDTH: {
          if (ctx.bid().hasW()) {
            sb.append(ctx.bid().getW());
          } else {
            ImpressionOrBuilder imp = findImp(ctx, SnippetMacros.OB_AD_WIDTH);
            sb.append(imp.hasBanner()
                ? imp.getBannerOrBuilder().getW()
                    : imp.getVideoOrBuilder().getW());
          }
          break;
        }

        case OB_AD_HEIGHT: {
          if (ctx.bid().hasH()) {
            sb.append(ctx.bid().getH());
          } else {
            ImpressionOrBuilder imp = findImp(ctx, SnippetMacros.OB_AD_HEIGHT);
            sb.append(imp.hasBanner()
                ? imp.getBannerOrBuilder().getH()
                : imp.getVideoOrBuilder().getH());
          }
          break;
        }
      }
    }

    super.processMacroAt(ctx, sb, macroDef);
  }

  protected void generateUrl(
      String url, List<? extends ObExt.Bid.UrlParameter> parameters, StringBuilder sb) {
    sb.append(url);

    if (!parameters.isEmpty()) {
      char sep = url.lastIndexOf('?') == -1 ? '?' : '&';

      for (ObExt.Bid.UrlParameter entry : parameters) {
        sb.append(sep).append(entry.getName());
        sb.append("=").append(entry.getValue());
        sep = '&';
      }
    }
  }

  private static @Nullable String preprocess(
      SnippetMacros base, @Nullable String urlCallback, String url) {
    if (Strings.isNullOrEmpty(url)) {
      return null;
    }

    if (urlCallback == null) {
      if (url.contains(base.key())) {
        logger.warn("Base callback URL not set, {} cannot be used. Macro value {} ignored",
            base.key(), url);
        return null;
      } else {
        return url;
      }
    }

    String ret = urlEncode(url.replace(base.key(), urlCallback), new StringBuilder());
    return Strings.isNullOrEmpty(ret) ? null : ret;
  }

  @Override public ToStringHelper toStringHelper() {
    return super.toStringHelper()
        .add("callbackUrl", callbackUrl)
        .add("clickUrl", clickUrl)
        .add("impressionUrl", impressionUrl);
  }
}
