/*
 * Copyright 2015 Google Inc. All Rights Reserved.
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

package mobitrans.openbidder.openrtbexchange;

import static com.google.openrtb.json.OpenRtbJsonUtils.getCurrentName;

import com.google.openbidder.sample.openrtbexchange.model.MyExt;
import com.google.openrtb.OpenRtb.BidRequest.Imp.Banner;
import com.google.openrtb.json.OpenRtbJsonExtReader;

import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;

/**
 * Reads our exchange's Banner extensions.
 * <p>
 * See {@link com.google.openrtb.json.OpenRtbJsonReader} for a much bigger sample of
 * how to writer readers for more complex objects, arrays, etc.
 */
public class MyOpenRtbExchangeExtBannerReader
extends OpenRtbJsonExtReader<Banner.Builder, MyExt.Banner.Builder> {

  public MyOpenRtbExchangeExtBannerReader() {
    super(MyExt.banner);
  }

  @Override protected void read(Banner.Builder msg, MyExt.Banner.Builder ext, JsonParser par)
      throws IOException {
    switch (getCurrentName(par)) {
      case "radius":
        ext.setRadius(par.nextIntValue(0));
        break;
    }
  }
}
