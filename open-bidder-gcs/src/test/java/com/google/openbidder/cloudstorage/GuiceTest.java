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

package com.google.openbidder.cloudstorage;

import static org.junit.Assert.assertNotNull;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.openbidder.api.bidding.BidInterceptor;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.interceptor.InterceptorChain;
import com.google.openbidder.api.testing.bidding.BiddingTestUtil;
import com.google.openbidder.cloudstorage.testing.FakeGoogleCloudStorage;
import com.google.openbidder.util.testing.FakeClock;

import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Tests for the guice package.
 */
public class GuiceTest {

  @Test
  public void testInterceptorWithStorage() {
    BiddingTestUtil.newBidController(new Module() {
      @Override public void configure(Binder binder) {
        binder.bind(HttpTransport.class).to(MockHttpTransport.class).in(Singleton.class);
        binder.bind(GoogleCloudStorage.class).toInstance(
            new FakeGoogleCloudStorage(new FakeClock()));
      }
    }, InterceptorWithInjectedStorage.class);
  }

  private static class InterceptorWithInjectedStorage implements BidInterceptor {
    @Inject
    public void setStorage(GoogleCloudStorage storage) {
      assertNotNull(storage);
    }

    @Override public void execute(InterceptorChain<BidRequest, BidResponse> chain) {
    }
  }
}
