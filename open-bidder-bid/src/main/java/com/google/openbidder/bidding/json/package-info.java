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

/**
 * JSON serialization support for the Open Bidder-specific OpenRTB extensions.
 * These extensions are used only internally by the Open Bidder framework,
 * they're not designed to be sent to any exchange; but the full JSON serialization
 * including all extensions may be useful for logging or other purposes.
 *
 * See {@link com.google.openrtb.json.OpenRtbJsonReader} for a bigger sample of
 * how to writer readers for more complex objects, arrays, etc.
 */
@javax.annotation.ParametersAreNonnullByDefault
package com.google.openbidder.bidding.json;
