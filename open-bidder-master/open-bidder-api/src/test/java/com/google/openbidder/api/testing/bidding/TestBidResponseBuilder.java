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

package com.google.openbidder.api.testing.bidding;

import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.platform.NoExchange;
import com.google.openbidder.http.response.StandardHttpResponse;

/**
 * Extends {@link com.google.openbidder.api.bidding.BidResponse.Builder}
 * with additional methods for unit testing on generic exchanges.
 */
public class TestBidResponseBuilder extends BidResponse.Builder {

  protected TestBidResponseBuilder() {
    setHttpResponse(StandardHttpResponse.newBuilder().setStatusCode(defaultStatusCode()));
    setExchange(NoExchange.INSTANCE);
  }

  public static TestBidResponseBuilder create() {
    return new TestBidResponseBuilder();
  }
}
