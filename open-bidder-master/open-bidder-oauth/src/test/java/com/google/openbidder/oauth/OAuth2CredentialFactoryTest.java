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

import static org.junit.Assert.assertNotNull;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.openbidder.googlecompute.InstanceMetadata;
import com.google.openbidder.oauth.generic.ConfiguredOAuth2CredentialFactory;
import com.google.openbidder.oauth.googlecompute.GoogleComputeOAuth2CredentialFactory;

import org.junit.Test;

/**
 * Tests for the {@link OAuth2CredentialFactory} hierarchy.
 */
public class OAuth2CredentialFactoryTest {
  private final JsonFactory JSON_FACTORY = new JacksonFactory();

  @Test
  public void testConfiguredOAuth2CredentialFactory() {
    ConfiguredOAuth2CredentialFactory factory = new ConfiguredOAuth2CredentialFactory(
        JSON_FACTORY, new MockHttpTransport(),
        "src/test/resources/fake-privatekey.p12", "svcAcct");
    assertNotNull(factory.retrieveCredential("scope"));
    assertNotNull(factory.toString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConfiguredOAuth2CredentialFactory_missingServiceAccount() {
    assertNotNull(new ConfiguredOAuth2CredentialFactory(
        JSON_FACTORY, new MockHttpTransport(), "src/test/resources/fake-privatekey.p12", null));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConfiguredOAuth2CredentialFactory_p12FilenameMissing() {
    assertNotNull(new ConfiguredOAuth2CredentialFactory(
        JSON_FACTORY, new MockHttpTransport(), null, "svcAcct"));
  }

  @Test(expected = IllegalStateException.class)
  public void testConfiguredOAuth2CredentialFactory_p12FileMissing() {
    ConfiguredOAuth2CredentialFactory factory = new ConfiguredOAuth2CredentialFactory(
        JSON_FACTORY, new MockHttpTransport(), "src/test/resources/filenotfound.p12", "svcAcct");
    factory.retrieveCredential("scope");
  }

  @Test(expected = IllegalStateException.class)
  public void testConfiguredOAuth2CredentialFactory_p12FileError() {
    ConfiguredOAuth2CredentialFactory factory = new ConfiguredOAuth2CredentialFactory(
        JSON_FACTORY, new MockHttpTransport(), "src/test/resources/logging.properties", "svcAcct");
    factory.retrieveCredential("scope");
  }

  @Test
  public void testGoogleComputeOAuth2CredentialFactory() {
    GoogleComputeOAuth2CredentialFactory factory = new GoogleComputeOAuth2CredentialFactory(
        new InstanceMetadata(JSON_FACTORY, new MockHttpTransport()), "svcAcct");
    assertNotNull(factory.retrieveCredential("scope"));
    assertNotNull(factory.toString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGoogleComputeOAuth2CredentialFactory_bad() {
    assertNotNull(new GoogleComputeOAuth2CredentialFactory(
        new InstanceMetadata(JSON_FACTORY, new MockHttpTransport()), null));
  }
}
