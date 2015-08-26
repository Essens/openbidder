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

import static com.google.common.base.Objects.equal;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.openbidder.api.interceptor.UserRequest;
import com.google.openbidder.api.platform.Exchange;
import com.google.openbidder.http.HttpRequest;
import com.google.openrtb.OpenRtb;
import com.google.openrtb.OpenRtb.BidRequest.Impression;
import com.google.openrtb.OpenRtb.BidRequest.Impression.Banner;
import com.google.openrtb.OpenRtb.BidRequest.Impression.Video;
import com.google.openrtb.util.OpenRtbUtils;
import com.google.openrtb.util.ProtoUtils;
import com.google.protobuf.MessageLite;

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
   * there's no value in the native request (it's identical to the OpenRTB request, or
   * mapping is perfect so no additional information would be found in the native request)
   */
  public final @Nullable <T> T nativeRequest() {
    @SuppressWarnings("unchecked")
    T ret = (T)nativeRequest;
    return ret;
  }

  /**
   * @return The delegate OpenRTB bid request.
   */
  public final OpenRtb.BidRequest openRtb() {
    return request;
  }

  /**
   * Iterates all {@link Impression}s.
   *
   * @return All OpenRTB {@link Impression}s.
   */
  public final List<Impression> imps() {
    return openRtb().getImpList();
  }

  /**
   * Filters {@link Impression}s.
   *
   * @return All {@link Impression}s that pass a predicate.
   */
  public final Iterable<Impression> impsWith(Predicate<Impression> predicate) {
    return OpenRtbUtils.impsWith(openRtb(), predicate, true, true);
  }

  /**
   * Finds an {@link Impression} by ID.
   *
   * @return The {@link Impression}s that has the given id, or {@code null} if not found.
   */
  public final @Nullable Impression impWithId(final String id) {
    return OpenRtbUtils.impWithId(openRtb(), id);
  }

  /**
   * Iterate {@link Impression} that contain a {@link Banner}.
   *
   * @return All {@link Impression}s with a {@link Banner}s.
   */
  public final Iterable<Impression> bannerImps() {
    return bannerImpsWith(Predicates.<Impression>alwaysTrue());
  }

  /**
   * Filter {@link Impression}s that contain a {@link Banner}.
   *
   * @param predicate Filters {@link Impression}s; will be invoked
   * exactly once and only on {@link Impression}s that contain a {@link Banner}
   * @return All {@link Impression}s that pass a predicate.
   */
  public final Iterable<Impression> bannerImpsWith(Predicate<Impression> predicate) {
    return OpenRtbUtils.impsWith(openRtb(), predicate, true, false);
  }

  /**
   * Find an {@link Impression} by its ID and its {@link Banner}'s ID.
   *
   * @param impId Impression ID; optional if the Banner IDs are unique within the request
   * @param bannerId Banner ID
   * @return The {@link Impression} for a given impression ID x banner ID,
   * or {@code null} if not found.
   */
  public final @Nullable Impression bannerImpWithId(@Nullable String impId, String bannerId) {
    return OpenRtbUtils.bannerImpWithId(openRtb(), impId, bannerId);
  }

  /**
   * Iterate {@link Impression} that contain a {@link Video}.
   *
   * @return All {@link Impression}s with a {@link Video}s.
   */
  public final Iterable<Impression> videoImps() {
    return videoImpsWith(Predicates.<Impression>alwaysTrue());
  }

  /**
   * Filter {@link Impression}s that contain a {@link Video}.
   *
   * @param predicate Filters {@link Impression}s; will be invoked
   * exactly once and only on {@link Impression}s that contain a {@link Video}
   * @return All {@link Impression}s that pass the predicate.
   */
  public final Iterable<Impression> videoImpsWith(Predicate<Impression> predicate) {
    return OpenRtbUtils.impsWith(openRtb(), predicate, false, true);
  }

  @Override
  protected ToStringHelper toStringHelper() {
    ToStringHelper tsr = super.toStringHelper();

    if (request != null) {
      tsr.add("request", ProtoUtils.filter(request, true, ProtoUtils.NOT_EXTENSION));
    }

    return tsr;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof BidRequest) || !super.equals(obj)) {
      return false;
    }

    BidRequest other = (BidRequest) obj;
    return equal(request, other.request);
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

    @Override
    public BidRequest build() {
      return new BidRequest(
          getExchange(),
          builtHttpRequest(),
          builtNativeRequest(),
          request == null ? null : request.build());
    }
  }
}
