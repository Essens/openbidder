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

package com.google.openbidder.oauth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.common.net.MediaType;
import com.google.openbidder.googlecompute.InstanceMetadata;
import com.google.openbidder.oauth.googlecompute.GoogleComputeCredential;

import org.junit.Test;

import java.io.IOException;

/**
 * Tests for {@link com.google.openbidder.oauth.googlecompute.GoogleComputeCredential}.
 */
public class GoogleComputeCredentialTest {
  private final JsonFactory JSON_FACTORY = new JacksonFactory();

  @Test
  public void testCredential() throws IOException {
    Credential credential = new GoogleComputeCredential(
        new InstanceMetadata(JSON_FACTORY, createCredentialTransport()),
        "service-account", "scope");
    credential.refreshToken();
    assertNotNull(credential.toString());
    assertEquals("1/8xbJqaOZXSUZbHLl5EOtu1pxz3fmmetKx9W8CV4t79M", credential.getAccessToken());
  }

  private static HttpTransport createCredentialTransport() {
    return new MockHttpTransport() {
        @Override public LowLevelHttpRequest buildRequest(String method, String url) {
          return new MockLowLevelHttpRequest() {
            @Override public LowLevelHttpResponse execute() {
              MockLowLevelHttpResponse result = new MockLowLevelHttpResponse();
              result.setContentType(MediaType.JSON_UTF_8.toString());
              result.setContent(
                    "{"
                  + "\"accessToken\" : \"1/8xbJqaOZXSUZbHLl5EOtu1pxz3fmmetKx9W8CV4t79M\","
                  + "\"expiresIn\" : 3600"
                  + "}");
              return result;
            }};
        }};
  }
}
