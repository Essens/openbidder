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

/**
 * Extends {@link com.google.openbidder.api.click.ClickRequest.Builder}
 * with additional features and defaults for unit testing on DoubleClick Ad Exchange.
 */
public class TestClickRequestBuilder
extends com.google.openbidder.api.testing.click.TestClickRequestBuilder {

  protected TestClickRequestBuilder() {
  }

  public static TestClickRequestBuilder create() {
    return new TestClickRequestBuilder();
  }

  // Nothing to see here, equivalent to the superclass at this time. But you won't need
  // to change tests if later releases need a DoubleClick-specific ClickRequest.
}
