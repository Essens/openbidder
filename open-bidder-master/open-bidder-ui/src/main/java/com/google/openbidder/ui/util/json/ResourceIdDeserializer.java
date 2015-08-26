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

package com.google.openbidder.ui.util.json;

import com.google.openbidder.ui.resource.support.ResourcePath;
import com.google.openbidder.ui.resource.support.ResourceType;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import java.io.IOException;

/**
 * Converts a URI into a {@link ResourcePath}.
 */
public class ResourceIdDeserializer extends JsonDeserializer<ResourcePath> {

  @Override
  public ResourcePath deserialize(
      JsonParser jp,
      DeserializationContext ctxt) throws IOException {

    return ResourceType.parseResourceUri(jp.getText());
  }
}
