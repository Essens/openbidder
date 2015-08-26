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
import static com.google.common.base.Preconditions.checkState;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.google.common.base.MoreObjects;
import com.google.inject.BindingAnnotation;
import com.google.openbidder.api.bidding.BidInterceptor;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.interceptor.InterceptorChain;
import com.google.openrtb.OpenRtb.BidRequest.Impression;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid.Bid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
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
  private final boolean isVideo;

  @Inject
  public ConfigurableBidInterceptor(
      @BidPrototype Bid bid,
      @Nullable @CpmMultiplier Double cpmMultiplier,
      @Nullable @CpmValue Double cpmValue,
      @BidProbability float bidProbability,
      @ErrorProbability float errorProbability) {
    this.bid = bid;

    checkState((cpmMultiplier == null) || (cpmValue == null),
        "Cannot set both cpmMultiplier and cpmValue");

    if (cpmMultiplier != null) {
      checkArgument(cpmMultiplier >= 0);
      this.cpmMultiplier = cpmMultiplier;
      this.cpmValue = null;
    } else if (cpmValue != null) {
      checkArgument(cpmValue >= 0);
      this.cpmMultiplier = null;
      this.cpmValue = cpmValue;
    } else {
      this.cpmMultiplier = 1.0;
      this.cpmValue = null;
    }

    checkArgument(bidProbability >= 0.0 && bidProbability <= 1.0);
    this.bidProbability = bidProbability;

    checkArgument(errorProbability >= 0f && errorProbability <= 1f);
    this.errorProbability = errorProbability;

    this.isVideo = bid.getAdm().startsWith("http"); // VAST URL

    logger.debug("Initialized with bid data ({}):\n{}", isVideo ? "video" : "banner", bid);
  }

  @Override
  public void execute(InterceptorChain<BidRequest, BidResponse> chain) {
    double rnd = ThreadLocalRandom.current().nextDouble();

    if (rnd < bidProbability) {

      if (rnd < errorProbability) {
        throw new RuntimeException("I'm sorry Dave, I'm afraid I cannot bid on this request...");
      }

      for (Impression imp : isVideo ? chain.request().videoImps() : chain.request().bannerImps()) {
        Bid.Builder bid = this.bid.toBuilder()
            .setId(imp.getId())
            .setImpid(imp.getId())
            .setPrice(cpmValue == null ? imp.getBidfloor() * cpmMultiplier : cpmValue);

        if (!isVideo && (imp.getBanner().hasWmin() || imp.getBanner().hasHmin())) {
          bid.setW(imp.getBanner().getWmin());
          bid.setH(imp.getBanner().getHmin());
        }

        chain.response().addBid(bid);

        if (logger.isDebugEnabled()) {
          logger.debug("Creating bid:\n{}", bid.build());
        }
      }
    }

    chain.proceed();
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
