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

package com.google.openbidder.googlecompute;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.common.net.MediaType;

import org.junit.Test;

/**
 * Tests for {@link InstanceMetadata}.
 */
public class InstanceMetadataTest {
  private final JsonFactory JSON_FACTORY = new JacksonFactory();

  @Test
  public void testInstanceMetadata() {
    InstanceMetadata metadata = mockOkayRequest("there");
    assertEquals("there", metadata.metadata("hostname"));
    assertEquals("there", metadata.customMetadata("hostname"));
    assertNotNull(metadata.toString());
  }

  @Test(expected = MetadataNotFoundException.class)
  public void testMetadataNotFound() {
    InstanceMetadata metadata = mockRequest(HttpStatusCodes.STATUS_CODE_NOT_FOUND);
    metadata.metadata("a-key-that-does-not-exist");
  }

  @Test(expected = IllegalStateException.class)
  public void testMetadataError() {
    InstanceMetadata metadata = mockRequest(HttpStatusCodes.STATUS_CODE_SERVER_ERROR);
    metadata.metadata("a-key-causing-error");
  }

  @Test
  public void testOAuth2Scopes() {
    InstanceMetadata metadata = mockOkayRequest(
        "{\"accessToken\":\"abc\",\"expiresAt\":1346953416,\"expiresIn\":3412}");
    OAuth2ServiceTokenMetadata token = metadata.serviceToken("http://foo");
    assertEquals("abc", token.getAccessToken());
    assertEquals(Long.valueOf(1346953416L), token.getExpiresAt());
    assertEquals(Long.valueOf(3412L), token.getExpiresIn());
  }

  @Test(expected = MetadataOAuth2ScopeNotFoundException.class)
  public void testOAuth2MissingScope() {
    InstanceMetadata metadata = mockRequest(HttpStatusCodes.STATUS_CODE_NOT_FOUND);
    metadata.serviceToken("default", "http://foo");
  }

  @Test(expected = IllegalStateException.class)
  public void testOAuth2ScopeError() {
    InstanceMetadata metadata = mockRequest(HttpStatusCodes.STATUS_CODE_SERVER_ERROR);
    metadata.serviceToken("default", "http://foo");
  }

  @Test
  public void testNetworkMetadata() {
    InstanceMetadata metadata = mockOkayRequest(
        "{\"networkInterface\":[{\"accessConfiguration\":"
        + "[{\"externalIp\":\"8.34.218.168\",\"type\":\"ONE_TO_ONE_NAT\"}],"
        + "\"ip\":\"10.92.248.39\","
        + "\"network\":\"projects/1/networks/default\"}]}");
    NetworkMetadata network = metadata.network();
    assertEquals(1, network.getNetworkInterfaces().size());
    NetworkMetadata.NetworkInterface networkInterface = network.getNetworkInterfaces().get(0);
    assertEquals("10.92.248.39", networkInterface.getIp());
    assertEquals("projects/1/networks/default", networkInterface.getNetwork());
    assertEquals(1, networkInterface.getAccessConfiguration().size());
    NetworkMetadata.AccessConfiguration accessConfig =
        networkInterface.getAccessConfiguration().get(0);
    assertEquals("8.34.218.168", accessConfig.getExternalIp());
    assertEquals("ONE_TO_ONE_NAT", accessConfig.getType());
  }

  @Test
  public void testResourceShortName() {
    assertEquals("rtb-us-east2",
        InstanceMetadata.resourceShortName("projects/123/zones/rtb-us-east2"));
    assertEquals("foo", InstanceMetadata.resourceShortName("foo"));
  }

  private InstanceMetadata mockOkayRequest(final String content) {
    return new InstanceMetadata(JSON_FACTORY, new MockHttpTransport() {
      @Override public LowLevelHttpRequest buildRequest(String method, String url) {
        return new MockLowLevelHttpRequest() {
          @Override public LowLevelHttpResponse execute() {
            MockLowLevelHttpResponse result = new MockLowLevelHttpResponse();
            result.setContentType(MediaType.PLAIN_TEXT_UTF_8.toString());
            result.setContent(content);
            result.setStatusCode(HttpStatusCodes.STATUS_CODE_OK);
            return result;
          }};
      }});
  }

  private InstanceMetadata mockRequest(final int status) {
    return new InstanceMetadata(JSON_FACTORY, new MockHttpTransport() {
      @Override public LowLevelHttpRequest buildRequest(String method, String url) {
        return new MockLowLevelHttpRequest() {
          @Override public LowLevelHttpResponse execute() {
            MockLowLevelHttpResponse result = new MockLowLevelHttpResponse();
            result.setContentType(MediaType.PLAIN_TEXT_UTF_8.toString());
            result.setStatusCode(status);
            return result;
          }};
      }});
  }
}
