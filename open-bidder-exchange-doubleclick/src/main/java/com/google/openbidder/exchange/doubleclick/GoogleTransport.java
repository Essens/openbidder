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

package com.google.openbidder.exchange.doubleclick;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.doubleclick.util.DoubleClickMetadata;

import java.io.IOException;
import java.io.InputStream;

/**
 * Implements {@link com.google.doubleclick.util.DoubleClickMetadata.Transport}
 * with Google HTTP Client.
 */
public class GoogleTransport implements DoubleClickMetadata.Transport {
  private final HttpTransport httpTransport;

  public GoogleTransport() {
    this(new NetHttpTransport());
  }

  public GoogleTransport(HttpTransport httpTransport) {
    this.httpTransport = httpTransport;
  }

  @Override
  public InputStream open(String url) throws IOException {
    return httpTransport.createRequestFactory()
        .buildGetRequest(new GenericUrl(url))
        .execute().getContent();
  }
}
