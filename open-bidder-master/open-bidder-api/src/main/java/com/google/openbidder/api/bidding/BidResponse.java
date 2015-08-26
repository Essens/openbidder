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

package com.google.openbidder.api.bidding;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.openbidder.api.interceptor.UserResponse;
import com.google.openbidder.api.platform.Exchange;
import com.google.openbidder.http.HttpResponse;
import com.google.openrtb.OpenRtb;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid.Bid;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid.BidOrBuilder;
import com.google.openrtb.util.OpenRtbUtils;
import com.google.openrtb.util.ProtoUtils;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Open Bidder bid response. You can build either an OpenRTB response (most methods support this),
 * or the exchange-specific "native" response (using only {@link #nativeResponse()}, but not both.
 *
 * @see BidInterceptor
 */
public class BidResponse extends UserResponse<BidResponse> {
  private OpenRtb.BidResponse.Builder response;
  private Object nativeResponse;

  /**
   * Creates a bid response.
   */
  protected BidResponse(Exchange exchange, HttpResponse.Builder httpResponseBuilder) {
    super(exchange, httpResponseBuilder);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  @Override
  public Builder toBuilder() {
    return newBuilder()
        .setExchange(getExchange())
        .setHttpResponse(httpResponse().build().toBuilder());
  }

  /**
   * @return The delegate OpenRTB bid response builder.
   */
  public final OpenRtb.BidResponse.Builder openRtb() {
    if (response == null && nativeResponse == null) {
      response = OpenRtb.BidResponse.newBuilder();
    }
    return response;
  }

  /**
   * @return The delegate native response. The returned type depends on the exchange, but it
   * will be a mutable form (for protoc-based response models, it should be a builder).
   */
  public final <T> T nativeResponse() {
    if (response == null && nativeResponse == null) {
      nativeResponse = getExchange().newNativeResponse();
    }
    @SuppressWarnings("unchecked")
    T ret = (T) nativeResponse;
    return ret;
  }

  /**
   * @return The OpenRTB SeatBid with the specified ID; will be created if not existent.
   * The ID should be present in the request's wseat.
   *
   * @see #seatBid() Use for the anonymous seat.
   */
  public final SeatBid.Builder seatBid(String seat) {
    return OpenRtbUtils.seatBid(openRtb(), seat);
  }

  /**
   * @return The anonymous OpenRTB SeatBid, used by non-seat-specific bids (the seat ID is not set).
   * Will be created if not existent.
   */
  public final SeatBid.Builder seatBid() {
    return OpenRtbUtils.seatBid(openRtb());
  }

  /**
   * Adds a bid (in the anonymous seat).
   * @see #seatBid()
   */
  public final BidResponse addBid(BidOrBuilder bid) {
    SeatBid.Builder seatBid = seatBid();
    if (bid instanceof Bid) {
      seatBid.addBid((Bid) bid);
    } else {
      seatBid.addBid((Bid.Builder) bid);
    }
    return self();
  }

  /**
   * Adds a bid to a specific seat.
   */
  public final BidResponse addBid(String seat, BidOrBuilder bid) {
    SeatBid.Builder seatBid = seatBid(seat);
    if (bid instanceof Bid) {
      seatBid.addBid((Bid) bid);
    } else {
      seatBid.addBid((Bid.Builder) bid);
    }
    return self();
  }

  /**
   * Iterates all bids.
   *
   * @return Read-only sequence of all bis in the response.
   * May have bids from multiple seats, grouped by seat
   */
  public final Iterable<Bid.Builder> bids() {
    return OpenRtbUtils.bids(openRtb());
  }

  /**
   * Iterates all bids from a specific seat.
   *
   * @param seat Seat ID, or {@code null} to select the anonymous seat
   * @return View for the seat's internal sequence of bids; or an empty, read-only
   * view if that seat doesn't exist.
   */
  public final List<Bid.Builder> bids(@Nullable String seat) {
    return OpenRtbUtils.bids(openRtb(), seat);
  }

  /**
   * Finds a bid by ID.
   *
   * @param id Bid ID, assumed to be unique within the response
   * @return Matching bid's builder, or {@code null} if not found
   */
  public final @Nullable Bid.Builder bidWithId(String id) {
    return OpenRtbUtils.bidWithId(openRtb(), id);
  }

  /**
   * Finds a bid by seat and ID.
   *
   * @param seat Seat ID, or {@code null} to select the anonymous seat
   * @param id Bid ID, assumed to be unique within the seat
   * @return Matching bid's builder, or {@code null} if not found
   */
  public final @Nullable Bid.Builder bidWithId(@Nullable String seat, String id) {
    return OpenRtbUtils.bidWithId(openRtb(), seat, id);
  }

  /**
   * Finds a bid by ad ID.
   *
   * @param adid Bid's ad ID, assumed to be unique within the response
   * @return Matching bid's builder, or {@code null} if not found
   */
  public final @Nullable Bid.Builder bidWithAdid(String adid) {
    checkNotNull(adid);

    for (SeatBid.Builder seatbid : openRtb().getSeatbidBuilderList()) {
      for (Bid.Builder bid : seatbid.getBidBuilderList()) {
        if (adid.equals(bid.getAdid())) {
          return bid;
        }
      }
    }
    return null;
  }

  /**
   * Finds a bid by seat and ad ID.
   *
   * @param seat Seat ID, or {@code null} to select the anonymous seat
   * @param adid Bid's ad ID, assumed to be unique within the seat
   * @return Matching bid's builder, or {@code null} if not found
   */
  public final @Nullable Bid.Builder bidWithAdid(@Nullable String seat, String adid) {
    checkNotNull(adid);

    for (SeatBid.Builder seatbid : openRtb().getSeatbidBuilderList()) {
      if (seatbid.hasSeat() ? seatbid.getSeat().equals(seat) : seat == null) {
        for (Bid.Builder bid : seatbid.getBidBuilderList()) {
          if (adid.equals(bid.getAdid())) {
            return bid;
          }
        }
        return null;
      }
    }
    return null;
  }

  /**
   * Finds bids by a custom criteria.
   *
   * @param filter Selection criteria
   * @return Read-only sequence of bids that satisfy the filter.
   * May have bids from multiple seats, grouped by seat
   */
  public final Iterable<Bid.Builder> bidsWith(Predicate<Bid.Builder> filter) {
    return OpenRtbUtils.bidsWith(openRtb(), filter);
  }

  /**
   * Finds bids by a custom criteria.
   *
   * @param seat Seat ID, or {@code null} to select the anonymous seat
   * @param filter Selection criteria
   * @return Sequence of all bids that satisfy the filter.
   * May have bids from multiple seats, grouped by seat
   */
  public final Iterable<Bid.Builder> bidsWith(
      @Nullable String seat, Predicate<Bid.Builder> filter) {
    return OpenRtbUtils.bidsWith(openRtb(), seat, filter);
  }

  /**
   * Updates bids, from all seats.
   *
   * @param updater Update function. The {@code apply()} method can decide or not to update each
   * object, and it's expected to return {@code true} for objects that were updated
   * @return {@code true} if at least one bid was updated
   * @see ProtoUtils#update(Iterable, Function) for more general updating support
   */
  public final boolean updateBids(Function<Bid.Builder, Boolean> updater) {
    return OpenRtbUtils.updateBids(openRtb(), updater);
  }

  /**
   * Updates bids from a given seat.
   *
   * @param seat Seat ID, or {@code null} to select the anonymous seat
   * @param updater Update function. The {@code apply()} method can decide or not to update each
   * object, and it's expected to return {@code true} for objects that were updated
   * @return {@code true} if at least one bid was updated
   * @see ProtoUtils#update(Iterable, Function) for more general updating support
   */
  public final boolean updateBids(@Nullable String seat, Function<Bid.Builder, Boolean> updater) {
    return OpenRtbUtils.updateBids(openRtb(), seat, updater);
  }

  /**
   * Filters bids, from all seats.
   *
   * @param filter Predicate to filter which bids to keep; bids that don't pass the filter
   * will be removed
   * @return {@code true} if any bid was removed
   * @see ProtoUtils#filter(Iterable, Predicate) for more general filtering support
   */
  public final boolean filterBids(Predicate<Bid.Builder> filter) {
    return OpenRtbUtils.filterBids(openRtb(), filter);
  }

  /**
   * Filter bids from a given seat.
   *
   * @param seat Seat ID, or {@code null} to select the anonymous seat
   * @param filter Returns {@code true} to keep bid, {@code false} to remove
   * @return {@code true} if any bid was removed
   * @see ProtoUtils#filter(Iterable, Predicate) for more general filtering support
   */
  public final boolean filterBids(@Nullable String seat, Predicate<Bid.Builder> filter) {
    return OpenRtbUtils.filterBids(openRtb(), seat, filter);
  }

  @Override
  public ToStringHelper toStringHelper() {
    return super.toStringHelper()
        .add("response", response == null ? null : response.buildPartial());
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof BidResponse) || !super.equals(obj)) {
      return false;
    }

    BidResponse other = (BidResponse) obj;
    return nativeResponse == other.nativeResponse
        && Objects.equal(
            response == null ? null : response.buildPartial(),
            other.response == null ? null : other.response.buildPartial());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(super.hashCode(),
        response == null ? 0 : response.buildPartial().hashCode());
  }

  /**
   * Builder for {@link BidResponse}.
   */
  public static class Builder extends UserResponse.Builder<Builder> {
    protected Builder() {
    }

    @Override
    public BidResponse build() {
      return new BidResponse(getExchange(), getHttpResponse());
    }
  }
}
