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

package com.google.openbidder.util;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.common.net.MediaType;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Loads a local resource in lieu of HTTP.
 */
public class ResourceHttpTransport extends MockHttpTransport {
  private String resourceName;
  private MediaType contentType = MediaType.PLAIN_TEXT_UTF_8;
  private String contentEncoding = "UTF-8";
  private int statusCode = HttpStatusCodes.STATUS_CODE_OK;

  private ResourceHttpTransport() {
  }

  public static ResourceHttpTransport create() {
    return new ResourceHttpTransport();
  }

  public String getResourceName() {
    return resourceName;
  }

  public ResourceHttpTransport setResourceName(String resourceName) {
    this.resourceName = resourceName;
    return this;
  }

  public MediaType getContentType() {
    return contentType;
  }

  public ResourceHttpTransport setContentType(MediaType contentType) {
    this.contentType = contentType;
    return this;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public ResourceHttpTransport setStatusCode(int statusCode) {
    this.statusCode = statusCode;
    return this;
  }

  public String getContentEncoding() {
    return contentEncoding;
  }

  public ResourceHttpTransport setContentEncoding(String contentEncoding) {
    this.contentEncoding = contentEncoding;
    return this;
  }

  @Override
  public LowLevelHttpRequest buildRequest(String method, String url) throws MalformedURLException {
    final String resourceName = this.resourceName == null
        ? new URL(url).getPath()
        : this.resourceName;
    final MediaType contentType = this.contentType;
    final int statusCode = this.statusCode;
    final String contentEncoding = this.contentEncoding;
    return new MockLowLevelHttpRequest() {
      @Override public LowLevelHttpResponse execute() {
        MockLowLevelHttpResponse result = new MockLowLevelHttpResponse();
        result.setStatusCode(statusCode);
        if (contentType != null) {
          result.setContentType(contentType.toString());
        }
        if (contentEncoding != null) {
          result.setContentEncoding(contentEncoding);
        }
        InputStream inputStream = ResourceHttpTransport.class.getResourceAsStream(resourceName);
        result.setContent(checkNotNull(inputStream, "Resource %s not found", resourceName));
        return result;
    }};
  }
}
