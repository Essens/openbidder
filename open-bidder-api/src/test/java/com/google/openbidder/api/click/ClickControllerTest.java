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

package com.google.openbidder.api.click;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.common.util.concurrent.Service.State;
import com.google.openbidder.api.interceptor.InterceptorChain;
import com.google.openbidder.api.testing.click.ClickTestUtil;
import com.google.openbidder.api.testing.click.CountingClickInterceptor;
import com.google.openbidder.api.testing.click.TestClickRequestBuilder;
import com.google.openbidder.api.testing.click.TestClickResponseBuilder;
import com.google.openbidder.http.util.HttpUtil;
import com.google.openbidder.util.testing.TestUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

/**
 * Tests for {@link ClickController}.
 */
public class ClickControllerTest {
  private static final URI REDIRECT_URI = HttpUtil.buildUri("http://ad.network.com");

  private ClickController controller;
  private CountingClickInterceptor countingInterceptor;

  @Before
  public void setUp() {
    countingInterceptor = new CountingClickInterceptor();
    controller = ClickTestUtil.newClickController(
        countingInterceptor,
        new ClickInterceptor() {
          @Override public void execute(InterceptorChain<ClickRequest, ClickResponse> chain) {
            chain.response().setRedirectLocation(REDIRECT_URI);
            chain.proceed();
          }});
  }

  @After
  public void tearDown() {
    controller.stopAsync().awaitTerminated();
    assertEquals(State.TERMINATED, controller.state());
    controller.stopAsync().awaitTerminated();
    assertEquals(State.TERMINATED, controller.state());
    assertEquals(1, countingInterceptor.preDestroyCount);
  }

  @Test
  public void testClick() {
    ClickRequest request = TestClickRequestBuilder.create().build();
    ClickResponse response = TestClickResponseBuilder.create().build();
    assertNull(response.getRedirectLocation());

    controller.onRequest(request, response);
    assertEquals(REDIRECT_URI, response.getRedirectLocation());
    assertEquals(1, countingInterceptor.postConstructCount);
    assertEquals(1, countingInterceptor.invokeCount);
  }

  @Test
  public void testClickResponse() {
    TestUtil.testCommonMethods(TestClickResponseBuilder.create().build());
  }
}
