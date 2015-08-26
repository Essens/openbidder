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

package com.google.openbidder.remarketing.interceptor;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.doubleclick.util.DoubleClickMacros;
import com.google.openbidder.api.bidding.BidInterceptor;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.interceptor.InterceptorChain;
import com.google.openbidder.api.openrtb.ObExt;
import com.google.openbidder.config.impression.PriceName;
import com.google.openbidder.remarketing.model.Remarketing.Action;
import com.google.openbidder.remarketing.services.RemarketingService;
import com.google.openrtb.OpenRtb.BidRequest.Imp;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid.Bid;
import com.google.protobuf.TextFormat;
import com.google.protos.adx.NetworkBid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Remarketing Bid Interceptor.
 */
public class RemarketingBidInterceptor implements BidInterceptor {
  private static final Logger logger = LoggerFactory.getLogger(RemarketingBidInterceptor.class);
  private static final ImmutableList<ObExt.UrlParameter> IMPRESSION_PARAMS =
      ImmutableList.of(ObExt.UrlParameter.newBuilder()
          .setName(PriceName.DEFAULT).setValue(DoubleClickMacros.WINNING_PRICE.key()).build());
  private static final Ordering<Action> ORDER_MAX_CPM = new Ordering<Action>() {
      @Override public int compare(Action left, Action right) {
        assert left != null && right != null;
        return Long.compare(left.getMaxCpm(), right.getMaxCpm());
      }};

  private final RemarketingService remarketingService;

  @Inject
  public RemarketingBidInterceptor(RemarketingService remarketingService) {
    this.remarketingService = checkNotNull(remarketingService);
  }

  @Override
  public void execute(InterceptorChain<BidRequest, BidResponse> chain) {
    NetworkBid.BidRequest dcRequest = chain.request().nativeRequest();
    String googleGid = dcRequest.getGoogleUserId();
    Iterable<Action> actions = remarketingService.getActionsForUser(googleGid);

    if (Iterables.size(actions) > 0) {
      Action winningAction = ORDER_MAX_CPM.max(actions);
      Imp imp = chain.request().openRtb().getImp(0); //TODO(opinali): why only the first?

      if (winningAction != null) {
        Bid.Builder newBid = Bid.newBuilder()
            .setId(imp.getId())
            .setImpid(imp.getId())
            .setPrice(winningAction.getMaxCpm() / 1_000_000.0)
            .setAdm(winningAction.getCreative())
            .addExtension(ObExt.bidClickThroughUrl, winningAction.getClickThroughUrl())
            .setExtension(ObExt.bidImpressionParameter, IMPRESSION_PARAMS);
        chain.response().addBid(newBid);

        if (logger.isDebugEnabled()) {
          logger.debug("Creating bid: {}", TextFormat.shortDebugString(newBid));
        }
      }
    }
    chain.proceed();
  }
}
