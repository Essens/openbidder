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

package mobitrans.openbidder.sample.bidding;

import mobitrans.openbidder.bidding.RandomBidInterceptor;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import com.google.openbidder.api.bidding.BidController;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.testing.bidding.BiddingTestUtil;
import com.google.openbidder.api.testing.bidding.TestBidRequestBuilder;
import com.google.openbidder.api.testing.bidding.TestBidResponseBuilder;

import org.junit.Test;

/**
 * Unit tests for {@link RandomBidInterceptor}.
 */
public class RandomBidInterceptorTest {

  @Test
  public void testInterceptor() {
      /*
    BidRequest request = TestBidRequestBuilder.create().setRequest("1", 1, 1, 5.0).build();
    BidController controller = BiddingTestUtil.newBidController(new RandomBidInterceptor());
    BidResponse response = TestBidResponseBuilder.create().build();
    controller.onRequest(request, response);
    controller.stopAsync().awaitTerminated();
    assertTrue(Range.closedOpen(5.0, 10.0)
        .contains(Iterables.getOnlyElement(response.bids()).getPrice()));
              */
  }
}
