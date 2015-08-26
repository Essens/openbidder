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

package com.google.openbidder.bidding.interceptor;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Math.min;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.Arrays.asList;

import com.google.common.base.MoreObjects;
import com.google.inject.BindingAnnotation;
import com.google.openbidder.api.bidding.BidInterceptor;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.interceptor.InterceptorChain;
import com.google.openbidder.api.snippet.SnippetMacros;
import com.google.openrtb.OpenRtb.BidRequest.Imp;
import com.google.openrtb.OpenRtb.BidRequest.Imp.Banner;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid.Bid;
import com.google.protobuf.TextFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * An interceptor that bids with values determined by configurable properties.
 * Useful for general testing, including live bidding and load tests.
 */
public class ConfigurableBidInterceptor implements BidInterceptor {
  private static final Logger logger = LoggerFactory.getLogger(ConfigurableBidInterceptor.class);

  private final Bid bid;
  private final Double cpmMultiplier;
  private final Double cpmValue;
  private final float bidProbability;
  private final float errorProbability;
  private final SizeChoice sizeChoice;
  private final boolean isVideo;

  @Inject
  public ConfigurableBidInterceptor(
      @BidPrototype Bid bid,
      @Nullable @CpmMultiplier Double cpmMultiplier,
      @Nullable @CpmValue Double cpmValue,
      @BidProbability float bidProbability,
      @ErrorProbability float errorProbability,
      SizeChoice sizeChoice) {
    this.bid = bid;

    checkArgument(cpmMultiplier == null || cpmMultiplier >= 0);
    checkArgument(cpmValue == null || cpmValue >= 0);
    this.cpmMultiplier = cpmMultiplier == null && cpmValue == null ? (Double) 1.0 : cpmMultiplier;
    this.cpmValue = cpmValue;

    checkArgument(bidProbability >= 0.0 && bidProbability <= 1.0);
    this.bidProbability = bidProbability;

    checkArgument(errorProbability >= 0f && errorProbability <= 1f);
    this.errorProbability = errorProbability;

    this.sizeChoice = checkNotNull(sizeChoice);

    this.isVideo = bid.getAdm().startsWith("http"); // VAST URL

    logger.debug("Initialized with bid data ({}): {}",
        isVideo ? "video" : "banner", TextFormat.shortDebugString(bid));
  }

  @Override
  public void execute(InterceptorChain<BidRequest, BidResponse> chain) {
    double rnd = ThreadLocalRandom.current().nextDouble();

    if (rnd < bidProbability) {

      if (rnd < errorProbability) {
        throw new RuntimeException("I'm sorry Dave, I'm afraid I cannot bid on this request...");
      }

      for (Imp imp : isVideo ? chain.request().videoImps() : chain.request().bannerImps()) {
        Bid.Builder bid = this.bid.toBuilder()
            .setId(imp.getId())
            .setImpid(imp.getId())
            .setPrice(cpmMultiplier == null
                ? cpmValue
                : cpmValue == null
                    ? imp.getBidfloor() * cpmMultiplier
                    : min(imp.getBidfloor() * cpmMultiplier, cpmValue));

        if (isVideo) {
          bid.setW(imp.getVideo().getW());
          bid.setH(imp.getVideo().getH());
          chain.response().addBid(bid);
        } else {
          if (bid.getAdm().contains(SnippetMacros.OB_IMPRESSION_URL.key())) {
            bid.setNurl(SnippetMacros.OB_IMPRESSION_URL.key());
          }

          Banner b = imp.getBanner();
          List<Integer> wAll = allWidths(imp);
          List<Integer> hAll = allHeights(imp);

          if (sizeChoice == SizeChoice.ALL) {
            for (int i = 0; i < wAll.size(); ++i) {
              bid.setW(wAll.get(i)).setH(hAll.get(i));
              chain.response().addBid(bid);
            }
          } else {
            int w = chooseSize(b.getW(), b.getWmin(), b.getWmax(), wAll, rnd);
            int h = chooseSize(b.getH(), b.getHmin(), b.getHmax(), hAll, rnd);

            if (w != 0 && h != 0) {
              bid.setW(w).setH(h);
              chain.response().addBid(bid);
            }
          }
        }

        if (logger.isDebugEnabled()) {
          logger.debug("Creating bid: {}", TextFormat.shortDebugString(bid));
        }
      }
    }

    chain.proceed();
  }

  protected List<Integer> allWidths(Imp imp) {
    return imp.getBanner().hasW()
        ? asList(imp.getBanner().getW())
        : Collections.<Integer>emptyList();
  }

  protected List<Integer> allHeights(Imp imp) {
    return imp.getBanner().hasH()
        ? asList(imp.getBanner().getH())
        : Collections.<Integer>emptyList();
  }

  protected int chooseSize(int single, int vMin, int vMax, List<Integer> vAll, double rnd) {
    if (single != 0) {
      return single;
    }
    switch (sizeChoice) {
      case MIN:
        return vMin;
      case MAX:
        return vMax;
      case RANDOM:
        if (vAll.size() != 0) {
          return vAll.get(min((int)(vAll.size() * rnd), vAll.size() - 1));
        }
        break;
      default:
    }
    return 0;
  }

  public static enum SizeChoice {
    MIN,
    MAX,
    RANDOM,
    ALL
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues()
        .add("bid", bid)
        .add("cpmMultiplier", cpmMultiplier)
        .add("cpmValue", cpmValue)
        .add("bidProbability", bidProbability)
        .add("errorProbability", errorProbability)
        .toString();
  }

  @BindingAnnotation
  @Target({ FIELD, PARAMETER, METHOD })
  @Retention(RUNTIME)
  public @interface BidPrototype {
  }

  @BindingAnnotation
  @Target({ FIELD, PARAMETER, METHOD })
  @Retention(RUNTIME)
  public @interface CpmMultiplier {
  }

  @BindingAnnotation
  @Target({ FIELD, PARAMETER, METHOD })
  @Retention(RUNTIME)
  public @interface CpmValue {
  }

  @BindingAnnotation
  @Target({ FIELD, PARAMETER, METHOD })
  @Retention(RUNTIME)
  public @interface BidProbability {
    float DEFAULT = 1f;
  }

  @BindingAnnotation
  @Target({ FIELD, PARAMETER, METHOD })
  @Retention(RUNTIME)
  public @interface ErrorProbability {
    float DEFAULT = 0f;
  }
}
