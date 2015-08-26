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

package com.google.openbidder.api.platform;

import static org.junit.Assert.assertSame;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.openbidder.util.testing.TestUtil;

import org.junit.Test;

/**
 * Tests for {@link Exchange}, {@link NoExchange} and {@link OpenRtbExchange}.
 */
public class ExchangeTest {

  @Test
  public void test() {
    Exchange e1 = new Exchange("e1") {
      @Override public Object newNativeResponse() {
        return 1;
      }};
    Exchange e2 = new Exchange("e2") {
      @Override public Object newNativeResponse() {
        return 2;
      }};
    OpenRtbExchange o1 = new OpenRtbExchange("o1") {};
    OpenRtbExchange o2 = new OpenRtbExchange("o2") {};

    TestUtil.testCommonMethods(e1, e2);
    TestUtil.testCommonMethods(o1, o2);
    TestUtil.testCommonMethods(e1, o1);
    TestUtil.testCommonMethods(e1, NoExchange.INSTANCE);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testNoExchange_nativeResponse() {
    NoExchange.INSTANCE.newNativeResponse();
  }

  @Test
  public void testModule() {
    Injector injector = Guice.createInjector(Stage.DEVELOPMENT, new NoExchange.Module());
    assertSame(NoExchange.INSTANCE, injector.getInstance(Exchange.class));
  }
}
