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

package com.google.openbidder.api.testing.match;

import com.google.openbidder.api.match.MatchInterceptor;
import com.google.openbidder.api.match.MatchRequest;
import com.google.openbidder.api.match.MatchResponse;
import com.google.openbidder.api.testing.interceptor.CountingInterceptor;

/**
 * A match interceptor that monitors its own invocations.
 */
public class CountingMatchInterceptor
    extends CountingInterceptor<MatchRequest, MatchResponse>
    implements MatchInterceptor {
}
