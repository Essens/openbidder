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

package com.google.openbidder.exchange.doubleclick;

import com.google.openbidder.api.platform.Exchange;
import com.google.protos.adx.NetworkBid;

/**
 * {@link Exchange} implementation for DoubleClick Ad Exchange.
 */
final class DoubleClickExchange extends Exchange {
  static final DoubleClickExchange INSTANCE = new DoubleClickExchange();

  private DoubleClickExchange() {
    super("doubleclick");
  }

  @Override
  public Object newNativeResponse() {
    return NetworkBid.BidResponse.newBuilder();
  }
}
