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
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.openbidder.api.interceptor.UserRequest;
import com.google.openbidder.api.platform.Exchange;
import com.google.openbidder.http.HttpRequest;
import com.google.openrtb.OpenRtb;
import com.google.openrtb.OpenRtb.BidRequest.Imp;
import com.google.openrtb.OpenRtb.BidRequest.Imp.Banner;
import com.google.openrtb.OpenRtb.BidRequest.Imp.Video;
import com.google.openrtb.util.OpenRtbUtils;
import com.google.openrtb.util.ProtoUtils;
import com.google.protobuf.MessageLite;
import com.google.protobuf.TextFormat;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Open Bidder bid request. You can inspect the request either in OpenRTB format with
 * {@link #openRtb()}, or in the exchange-specific, native format with {@link #nativeRequest()}.
 *
 * @see BidInterceptor
 */
public class BidRequest extends UserRequest {
  private final Object nativeRequest;
  private final OpenRtb.BidRequest request;

  /**
   * Creates a bid request.
   *
   * @param exchange Exchange from which the bid request originated
   * @param httpRequest Source HTTP request
   * @param nativeRequest Exchange specific bid request
   * @param request OpenRTB bid request (optional)
   */
  protected BidRequest(
      Exchange exchange, HttpRequest httpRequest,
      @Nullable Object nativeRequest, @Nullable OpenRtb.BidRequest request) {

    super(exchange, httpRequest);
    this.nativeRequest = nativeRequest;
    this.request = request;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  @Override
  public Builder toBuilder() {
    return new Builder()
        .setExchange(getExchange())
        .setHttpRequest(httpRequest())
        .setNativeRequest(nativeRequest)
        .setRequest(request);
  }

  /**
   * @return Exchange specific bid request. The type will depend on {@link #getExchange()}
   * Some exchanges may provided this information, others may return {@code null} when
   * there's no value in providing the native request (e.g. if the exchange's native protocol
   * is OpenRTB-over-protobuf, the native request would be identical to {@link #openRtb()}).
   */
  public final @Nullable <T> T nativeRequest() {
    @SuppressWarnings("unchecked")
    T ret = (T)nativeRequest;
    return ret;
  }

  /**
   * @return The delegate OpenRTB bid request.  This may be {@code null} if the exchange does not
   * support OpenRTB, not even via mapping (or the mapping is disabled).  That should be an
   * uncommon scenario so this method is not Nullable, but if null it will throw an exception.
   * @throws IllegalStateException if the OpenRTB request is not available.
   */
  public final OpenRtb.BidRequest openRtb() {
    checkState(request != null, "OpenRTB request is not available");
    return request;
  }

  /**
   * Iterates all {@link Imp}s.
   *
   * @return All OpenRTB {@link Imp}s.
   */
  public final List<Imp> imps() {
    return openRtb().getImpList();
  }

  /**
   * Filters {@link Imp}s.
   *
   * @return All {@link Imp}s that pass a predicate.
   */
  public final Iterable<Imp> impsWith(Predicate<Imp> predicate) {
    return OpenRtbUtils.impsWith(openRtb(), predicate, true, true);
  }

  /**
   * Finds an {@link Imp} by ID.
   *
   * @return The {@link Imp}s that has the given id, or {@code null} if not found.
   */
  public final @Nullable Imp impWithId(final String id) {
    return OpenRtbUtils.impWithId(openRtb(), id);
  }

  /**
   * Iterate {@link Imp} that contain a {@link Banner}.
   *
   * @return All {@link Imp}s with a {@link Banner}s.
   */
  public final Iterable<Imp> bannerImps() {
    return bannerImpsWith(Predicates.<Imp>alwaysTrue());
  }

  /**
   * Filter {@link Imp}s that contain a {@link Banner}.
   *
   * @param predicate Filters {@link Imp}s; will be invoked
   * exactly once and only on {@link Imp}s that contain a {@link Banner}
   * @return All {@link Imp}s that pass a predicate.
   */
  public final Iterable<Imp> bannerImpsWith(Predicate<Imp> predicate) {
    return OpenRtbUtils.impsWith(openRtb(), predicate, true, false);
  }

  /**
   * Find an {@link Imp} by its ID and its {@link Banner}'s ID.
   *
   * @param impId Impression ID; optional if the Banner IDs are unique within the request
   * @param bannerId Banner ID
   * @return The {@link Imp} for a given impression ID x banner ID,
   * or {@code null} if not found.
   */
  public final @Nullable Imp bannerImpWithId(@Nullable String impId, String bannerId) {
    return OpenRtbUtils.bannerImpWithId(openRtb(), impId, bannerId);
  }

  /**
   * Iterate {@link Imp} that contain a {@link Video}.
   *
   * @return All {@link Imp}s with a {@link Video}s.
   */
  public final Iterable<Imp> videoImps() {
    return videoImpsWith(Predicates.<Imp>alwaysTrue());
  }

  /**
   * Filter {@link Imp}s that contain a {@link Video}.
   *
   * @param predicate Filters {@link Imp}s; will be invoked
   * exactly once and only on {@link Imp}s that contain a {@link Video}
   * @return All {@link Imp}s that pass the predicate.
   */
  public final Iterable<Imp> videoImpsWith(Predicate<Imp> predicate) {
    return OpenRtbUtils.impsWith(openRtb(), predicate, false, true);
  }

  @Override
  protected ToStringHelper toStringHelper() {
    ToStringHelper tsr = super.toStringHelper();

    if (request != null) {
      tsr.add("request",
          TextFormat.shortDebugString(ProtoUtils.filter(request, true, ProtoUtils.NOT_EXTENSION)));
    }

    return tsr;
  }

  /**
   * Builder for {@link UserRequest}.
   */
  public static class Builder extends UserRequest.Builder<Builder>{
    private Object nativeRequest;
    private OpenRtb.BidRequest.Builder request;

    protected Builder() {
    }

    public BidRequest.Builder setNativeRequest(@Nullable Object nativeRequest) {
      this.nativeRequest = nativeRequest;
      return self();
    }

    public @Nullable Object getNativeRequest() {
      return nativeRequest;
    }

    /**
     * Similar to {@link #getNativeRequest()}, but returns the "built"
     * object (the native request property remains unchanged).
     */
    protected Object builtNativeRequest() {
      // Supports protobuf builders; override as necessary for other builders.
      return nativeRequest instanceof MessageLite.Builder
          ? ProtoUtils.built((MessageLite.Builder) nativeRequest)
          : nativeRequest;
    }

    /**
     * Similar to {@link #getNativeRequest()}, but only allowed if the native request
     * was set (throws {@link NullPointerException} otherwise), and always returns a builder
     * (if the native request was set as a built object, it will be converted to builder).
     */
    protected Object nativeBuilder() {
      if (checkNotNull(nativeRequest) instanceof MessageLite) {
        nativeRequest = ProtoUtils.builder((MessageLite) nativeRequest);
      }
      return nativeRequest;
    }

    public BidRequest.Builder setRequest(@Nullable OpenRtb.BidRequestOrBuilder request) {
      this.request = ProtoUtils.builder(request);
      return self();
    }

    public @Nullable OpenRtb.BidRequest.Builder getRequest() {
      return request;
    }

    @Override public BidRequest build() {
      return new BidRequest(
          MoreObjects.firstNonNull(getExchange(), defaultExchange()),
          builtHttpRequest(),
          builtNativeRequest(),
          request == null ? null : request.build());
    }
  }
}
