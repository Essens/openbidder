/*
 * Copyright 2014 Google Inc. All Rights Reserved.
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

package com.google.openbidder.deals;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.doubleclick.util.DoubleClickMetadata;
import com.google.openbidder.api.bidding.BidInterceptor;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.interceptor.InterceptorChain;
import com.google.openbidder.deals.model.Deals.PreferredDeal;
import com.google.openrtb.OpenRtb.BidRequest.Imp;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid.Bid;
import com.google.protobuf.TextFormat;
import com.google.protos.adx.NetworkBid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

/**
 * An interceptor that bids on Ad Exchange Preferred Deals.
 */
public class PreferredDealsInterceptor implements BidInterceptor {
  private static final Logger logger = LoggerFactory.getLogger(PreferredDealsInterceptor.class);
  private final PreferredDealCollection preferredDealCollection;
  private final DoubleClickMetadata doubleClickMetadata;

  @Inject
  PreferredDealsInterceptor(
      PreferredDealCollection preferredDealCollection,
      DoubleClickMetadata doubleClickMetadata) {
    this.preferredDealCollection = checkNotNull(preferredDealCollection);
    this.doubleClickMetadata = checkNotNull(doubleClickMetadata);
  }

  @Override
  public void execute(InterceptorChain<BidRequest, BidResponse> chain) {

    NetworkBid.BidRequest dcRequest = chain.request().nativeRequest();
    String sellerNetwork = doubleClickMetadata.getSellerNetworks().get(
        dcRequest.getSellerNetworkId());

    if (logger.isDebugEnabled()) {
      logger.debug("Bidding on the seller network: {} with the id: {}",
          dcRequest.getSellerNetworkId(), sellerNetwork);
    }

    Long preferredDealFixedCpm = findMatchingFixedCpm(sellerNetwork);
    if (preferredDealFixedCpm != null) {
      for (Imp imp : chain.request().openRtb().getImpList()) {
        Bid.Builder newBid = Bid.newBuilder()
            .setId(imp.getId())
            .setImpid(imp.getId())
            .setPrice(preferredDealFixedCpm / 1_000_000.0);
        chain.response().addBid(newBid);

        if (logger.isDebugEnabled()) {
          logger.debug("Creating bid: {}", TextFormat.shortDebugString(newBid));
        }
      }
    }

    // if no matching deals available, we do nothing.

    chain.proceed();

  }
  /**
   * Finds the <a href="http://support.google.com/adxbuyer/bin/topic.py?hl=en&topic=2813035">
   * preferred deals</a> available between the specific publisher and the buyer and returns the
   * maximum fixed CPM of all matching preferred deals. If no matching preferred deal exists,
   * returns null.
   */
  private Long findMatchingFixedCpm (final String sellerNetwork) {
    Collection<PreferredDeal> preferredDeals = preferredDealCollection.getAll();
    List<PreferredDeal> matchedPreferredDeal = new ArrayList<>(Collections2.filter(preferredDeals,
        new Predicate<PreferredDeal>() {
          @Override public boolean apply(PreferredDeal deal) {
            return !deal.hasAdvertiserName()
                && deal.hasFixedCpm()
                && deal.hasSellerNetwork()
                && deal.getSellerNetwork() == sellerNetwork;
          }
        }));

    if (matchedPreferredDeal.isEmpty()) {
      return null;
    }

    return Collections.max(matchedPreferredDeal, new Comparator<PreferredDeal>() {
        @Override public int compare(PreferredDeal deal1, PreferredDeal deal2) {
          return ((Long) deal1.getFixedCpm()).compareTo(deal2.getFixedCpm());
        }}).getFixedCpm();
  }
}
