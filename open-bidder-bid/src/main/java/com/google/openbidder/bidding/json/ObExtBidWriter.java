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

package com.google.openbidder.bidding.json;

import com.google.openbidder.api.openrtb.ObExt.UrlParameter;
import com.google.openrtb.json.OpenRtbJsonExtWriter;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;

public final class ObExtBidWriter {

  protected ObExtBidWriter() {
  }

  public static class ClickThroughUrl extends OpenRtbJsonExtWriter<String> {
    public ClickThroughUrl() {
      super("click_through_url", false);
    }

    @Override protected void write(String ext, JsonGenerator gen) throws IOException {
      gen.writeString(ext);
    }
  }

  protected abstract static class Parameter extends OpenRtbJsonExtWriter<UrlParameter> {
    protected Parameter(String fieldName) {
      super(fieldName, true);
    }

    @Override protected void write(UrlParameter ext, JsonGenerator gen) throws IOException {
      gen.writeStringField("name", ext.getName());
      gen.writeStringField("value", ext.getValue());
    }
  }

  public static class ImpressionParameter extends Parameter {
    public ImpressionParameter() {
      super("impression_parameter");
    }
  }

  public static class ClickParameter extends Parameter {
    public ClickParameter() {
      super("click_parameter");
    }
  }
}
