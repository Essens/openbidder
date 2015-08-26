/*
 * Copyright 2015 Google Inc. All Rights Reserved.
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

import static java.util.Arrays.asList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.openrtb.OpenRtb.BidRequest;
import com.google.openrtb.OpenRtb.BidRequest.Imp;
import com.google.openrtb.OpenRtb.BidRequest.User;
import com.google.openrtb.OpenRtb.ContentCategory;

import org.junit.Test;

/**
 * Tests for {@link RawMessageUtils}.
 */
public class RawMessageUtilsTest {

  @Test
  public void testMatchScalar() {
    BidRequest matchMsg = BidRequest.newBuilder().setAllimps(true).buildPartial();
    assertTrue(RawMessageUtils.matches(
        BidRequest.newBuilder().setAllimps(true).buildPartial(),
        matchMsg));
    assertFalse(RawMessageUtils.matches(
        BidRequest.newBuilder().setAllimps(false).buildPartial(),
        matchMsg));
    assertFalse(RawMessageUtils.matches(
        BidRequest.newBuilder().buildPartial(),
        matchMsg));
  }

  @Test
  public void testMatchRepeated() {
    BidRequest matchMsg = BidRequest.newBuilder().addAllBcat(asList(
        ContentCategory.IAB1, ContentCategory.IAB2, ContentCategory.IAB3)).buildPartial();
    assertTrue(RawMessageUtils.matches(
        BidRequest.newBuilder().addAllBcat(asList(
            ContentCategory.IAB1, ContentCategory.IAB2, ContentCategory.IAB3, ContentCategory.IAB4))
            .buildPartial(),
        matchMsg));
    assertFalse(RawMessageUtils.matches(
        BidRequest.newBuilder().addAllBcat(asList(
            ContentCategory.IAB1, ContentCategory.IAB2, ContentCategory.IAB4, ContentCategory.IAB5))
            .buildPartial(),
        matchMsg));
    assertFalse(RawMessageUtils.matches(
        BidRequest.newBuilder().buildPartial(),
        matchMsg));
  }

  @Test
  public void testMatchMessage() {
     BidRequest matchMsg = BidRequest.newBuilder()
         .setUser(User.newBuilder().setId("A").buildPartial()).buildPartial();

    assertTrue(RawMessageUtils.matches(
        BidRequest.newBuilder().setUser(
            User.newBuilder().setId("A").addKeywords("X").buildPartial()).buildPartial(),
        matchMsg));
    assertFalse(RawMessageUtils.matches(
        BidRequest.newBuilder().setUser(
            User.newBuilder().setId("B").addKeywords("X").buildPartial()).buildPartial(),
        matchMsg));
    assertFalse(RawMessageUtils.matches(
        BidRequest.newBuilder().setUser(
            User.newBuilder().addKeywords("X").buildPartial()).buildPartial(),
        matchMsg));
    assertFalse(RawMessageUtils.matches(
        BidRequest.newBuilder().buildPartial(),
        matchMsg));
  }

  @Test
  public void testMatchRepeatedMessage() {
    Imp i = Imp.newBuilder().setId("1").setInstl(true).buildPartial();
    Imp i1 = Imp.newBuilder().setId("1").setInstl(true).setTagid("X").buildPartial();
    Imp i2 = Imp.newBuilder().setId("1").setInstl(false).setTagid("X").buildPartial();
    Imp i3 = Imp.newBuilder().setId("1").setTagid("X").buildPartial();
    BidRequest matchMsg = BidRequest.newBuilder().addImp(i).buildPartial();

    assertTrue(RawMessageUtils.matches(
        BidRequest.newBuilder().addAllImp(asList(i1, i2)).buildPartial(),
        matchMsg));
    assertFalse(RawMessageUtils.matches(
        BidRequest.newBuilder().addAllImp(asList(i2, i3)).buildPartial(),
        matchMsg));
    assertFalse(RawMessageUtils.matches(
        BidRequest.newBuilder().buildPartial(),
        matchMsg));
  }
}
