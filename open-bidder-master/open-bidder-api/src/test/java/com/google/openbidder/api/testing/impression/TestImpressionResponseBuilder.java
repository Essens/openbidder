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

package com.google.openbidder.api.testing.impression;

import com.google.openbidder.api.impression.ImpressionResponse;
import com.google.openbidder.api.platform.NoExchange;
import com.google.openbidder.http.response.StandardHttpResponse;

/**
 * Extends {@link com.google.openbidder.api.impression.ImpressionResponse.Builder}
 * with additional methods for unit testing on generic exchanges.
 */
public class TestImpressionResponseBuilder extends ImpressionResponse.Builder {

  protected TestImpressionResponseBuilder() {
    setHttpResponse(StandardHttpResponse.newBuilder().setStatusCode(defaultStatusCode()));
    setExchange(NoExchange.INSTANCE);
  }

  public static TestImpressionResponseBuilder create() {
    return new TestImpressionResponseBuilder();
  }
}
