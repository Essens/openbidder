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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.openbidder.api.testing.bidding.TestBidResponseBuilder;
import com.google.openbidder.util.testing.TestUtil;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid.Bid;

import org.junit.Test;

/**
 * Unit tests for {@link BidResponse}.
 */
public class BidResponseTest {

  @Test
  public void testCommonMethods() {
    TestUtil.testCommonMethods(
        TestBidResponseBuilder.create().build().addBid(buildHtmlBid("1", 0.1)));
  }

  @Test
  public void testResponse() {
    BidResponse response = TestBidResponseBuilder.create().build()
        .putMetadata("c", 30)
        .putAllMetadata(ImmutableMap.<String, Object>of());
    assertEquals(ImmutableMap.of("c", 30), ImmutableMap.copyOf(response.metadata()));

    response.openRtb().setCur("USD");
    response.seatBid("unused");
    assertNotNull(response.seatBid());
    assertNotNull(response.seatBid("x"));
    assertTrue(Iterables.isEmpty(response.bids()));
    Bid bid1 = buildHtmlBid("1", 0.1).build();
    response.addBid(bid1);
    Bid bid2 = buildHtmlBid("2", 0.1).build();
    response.addBid("x", bid2);
    assertTrue(response.bids("none").isEmpty());
    assertEquals(bid1, response.bidWithId("1").build());
    assertEquals(bid1, response.bidWithAdid("ad1").build());
    assertEquals(bid2, response.bidWithId("x", "2").build());
    assertEquals(bid2, response.bidWithAdid("x", "ad2").build());
    assertNull(response.bidWithAdid("x", "ad1"));
    assertNull(response.bidWithAdid("none", "ad1"));
    assertNotNull(response.bidWithAdid(null, "ad1"));
    Predicate<Bid.Builder> filterGoodBids = new Predicate<Bid.Builder>(){
      @Override public boolean apply(Bid.Builder bid) {
        return !"unused".equals(bid.getId());
      }};
    assertEquals(2, Iterables.size(response.bidsWith(filterGoodBids)));
    assertTrue(Iterables.isEmpty(response.bidsWith("none", filterGoodBids)));
  }

  public void testNativeOpenrtb() {
    BidResponse response = BidResponse.newBuilder().build();
    response.nativeResponse(); // lazy create
    response.nativeResponse(); // get
    assertNull(response.openRtb());
  }

  public void testOpenrtbNative() {
    BidResponse response = BidResponse.newBuilder().build();
    response.openRtb(); // lazy create
    response.openRtb(); // get
    assertNull(response.nativeResponse());
  }

  @Test
  public void testUpdater() {
    BidResponse response = TestBidResponseBuilder.create().build();
    Function<Bid.Builder, Boolean> noUpdates = new Function<Bid.Builder, Boolean>() {
      @Override public Boolean apply(Bid.Builder bid) {
        return false;
      }};
    assertFalse(response.updateBids(noUpdates));
  }

  @Test
  public void testFilter() {
    BidResponse response = TestBidResponseBuilder.create().build()
        .addBid(buildHtmlBid("1", 0.1))
        .addBid(buildHtmlBid("2", 0.1))
        .addBid(buildHtmlBid("3", 0.2));
    response.seatBid("unused");
    response.filterBids(Predicates.<Bid.Builder>alwaysTrue());
    assertEquals(3, Iterables.size(response.bids()));
    response.addBid("x", buildHtmlBid("unused", 0.1));
    response.addBid("x", buildHtmlBid("4", 0.1));
    assertTrue(response.filterBids("x", new Predicate<Bid.Builder>() {
      @Override public boolean apply(Bid.Builder bid) {
        return !"4".equals(bid.getId());
      }}));
    assertEquals(1, response.bids("x").size());
  }

  @Test
  public void testSeatBid() {
    BidResponse response = TestBidResponseBuilder.create().build();
    assertNotNull(response.seatBid());
    assertNotNull(response.seatBid("x"));
  }

  @Test
  public void testAddBid() {
    BidResponse response = TestBidResponseBuilder.create().build();
    Bid.Builder bid = Bid.newBuilder().setId("1").setImpid("1").setPrice(1.0);
    response.addBid(bid);
    response.addBid("x", bid);
  }

  @Test
  public void testBids() {
    BidResponse response = TestBidResponseBuilder.create().build();
    response.addBid(Bid.newBuilder().setId("1").buildPartial());
    response.addBid("x", Bid.newBuilder().setId("2").buildPartial());
    assertEquals(2,  Iterables.size(response.bids()));
    assertEquals(1,  response.bids("x").size());
    assertNotNull(response.bidWithId("1"));
    assertNotNull(response.bidWithId("x", "2"));
    assertNull(response.bidWithId("none"));
    assertNull(response.bidWithId("x", "none"));
    assertFalse(Iterables.isEmpty(response.bidsWith(new Predicate<Bid.Builder>() {
      @Override public boolean apply(Bid.Builder bid) {
        return "1".equals(bid.getId());
      }
    })));
    assertFalse(Iterables.isEmpty(response.bidsWith("x", new Predicate<Bid.Builder>() {
      @Override public boolean apply(Bid.Builder bid) {
        return "2".equals(bid.getId());
      }
    })));
    assertTrue(Iterables.isEmpty(response.bidsWith(Predicates.<Bid.Builder>alwaysFalse())));
    assertTrue(Iterables.isEmpty(response.bidsWith("x", Predicates.<Bid.Builder>alwaysFalse())));
    assertTrue(Iterables.isEmpty(response.bidsWith("none", Predicates.<Bid.Builder>alwaysFalse())));
  }

  static class BuilderToBid implements Function<Bid.Builder, Bid> {
    static final BuilderToBid INSTANCE = new BuilderToBid();
    @Override public Bid apply(Bid.Builder builder) {
      return builder.buildPartial();
    }
    static Iterable<Bid> toBids(Iterable<Bid.Builder> builders) {
      return Iterables.transform(builders, INSTANCE);
    }
  }

  private static Bid.Builder buildHtmlBid(String id, double price) {
    return Bid.newBuilder()
        .setId(id)
        .setAdid("ad" + id)
        .setImpid("imp" + id)
        .setPrice(price);
  }
}
