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

package com.google.openbidder.exchange.doubleclick.testing;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.doubleclick.crypto.DoubleClickCrypto;
import com.google.doubleclick.openrtb.DoubleClickLinkMapper;
import com.google.doubleclick.openrtb.DoubleClickOpenRtbMapper;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.exchange.doubleclick.server.OpenBidderExtMapper;
import com.google.openrtb.mapper.OpenRtbMapper;
import com.google.protobuf.ByteString;
import com.google.protos.adx.NetworkBid;

import com.codahale.metrics.MetricRegistry;

/**
 * Extends {@link com.google.openbidder.api.bidding.BidRequest.Builder}
 * with additional features and defaults for unit testing on DoubleClick Ad Exchange.
 */
public class TestBidRequestBuilder
extends com.google.openbidder.api.testing.bidding.TestBidRequestBuilder {
  private OpenRtbMapper<
      NetworkBid.BidRequest, NetworkBid.BidResponse,
      NetworkBid.BidRequest.Builder, NetworkBid.BidResponse.Builder> mapper;

  protected TestBidRequestBuilder() {
    setNativeRequest(NetworkBid.BidRequest.newBuilder());
  }

  public static TestBidRequestBuilder create() {
    return new TestBidRequestBuilder();
  }

  public TestBidRequestBuilder setMapper(
      OpenRtbMapper<
          NetworkBid.BidRequest, NetworkBid.BidResponse,
          NetworkBid.BidRequest.Builder, NetworkBid.BidResponse.Builder> mapper) {
    this.mapper = checkNotNull(mapper);
    return this;
  }

  public OpenRtbMapper<
      NetworkBid.BidRequest, NetworkBid.BidResponse,
      NetworkBid.BidRequest.Builder, NetworkBid.BidResponse.Builder> getMapper() {
    if (mapper == null) {
      mapper = new DoubleClickOpenRtbMapper(
          new MetricRegistry(),
          DoubleClickTestUtil.getMetadata(),
          new DoubleClickCrypto.Hyperlocal(DoubleClickTestUtil.ZERO_KEYS),
          ImmutableList.of(DoubleClickLinkMapper.INSTANCE, OpenBidderExtMapper.INSTANCE));
    }
    return mapper;
  }

  /**
   * {@inheritDoc}
   */
  @Override public NetworkBid.BidRequest.Builder nativeBuilder() {
    return (NetworkBid.BidRequest.Builder) super.nativeBuilder();
  }

  /**
   * WARNING: the minimum CPM values are provided here in micros (the DoubleClick standard unit),
   * not in the currency unit (= 1000000 micros) like the overridden method.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public TestBidRequestBuilder setRequest(String id, Object... adGroupMincpm) {
    NetworkBid.BidRequest.Builder nativeBuilder = nativeBuilder()
        .setId(ByteString.copyFromUtf8(id));

    for (int i = 0; i < adGroupMincpm.length; i += 3) {
      nativeBuilder.addAdslot(NetworkBid.BidRequest.AdSlot.newBuilder()
          .setId(((Number) adGroupMincpm[i + 0]).intValue())
          .addWidth(728)
          .addHeight(90)
          .addMatchingAdData(NetworkBid.BidRequest.AdSlot.MatchingAdData.newBuilder()
              .setAdgroupId(((Number) adGroupMincpm[i + 1]).longValue())
              .setMinimumCpmMicros(((Number) adGroupMincpm[i + 2]).longValue())));
    }

    return this;
  }

  @Override public BidRequest build() {
    NetworkBid.BidRequest.Builder nativeBuilder = nativeBuilder();

    if (nativeBuilder != null) {
      if (!nativeBuilder.hasId()) {
        nativeBuilder.setId(DoubleClickTestUtil.REQUEST_ID);
      }

      setRequest(getMapper().toOpenRtbBidRequest(nativeBuilder.buildPartial()));
    }

    return super.build();
  }
}
