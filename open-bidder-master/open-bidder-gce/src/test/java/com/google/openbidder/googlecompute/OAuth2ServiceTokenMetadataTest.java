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
import static org.junit.Assert.assertTrue;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.openbidder.util.testing.TestUtil;

import org.codehaus.jackson.JsonParseException;
import org.junit.Test;

import java.io.IOException;

/**
 * Unit tests for {@link OAuth2ServiceTokenMetadata}
 */
public class OAuth2ServiceTokenMetadataTest {
  private final JsonFactory JSON_FACTORY = new JacksonFactory();

  @Test(expected = JsonParseException.class)
  public void testInvalidJson() throws IOException {
    JSON_FACTORY.fromString("{", OAuth2ServiceTokenMetadata.class);
  }

  @Test
  public void testParsing() throws IOException {
    String json =
        "{\"accessToken\":\"ya29.AHES6ZRzh\",\"expiresAt\":1326931739,\"expiresIn\":3599}";
    OAuth2ServiceTokenMetadata tokens =
        JSON_FACTORY.fromString(json, OAuth2ServiceTokenMetadata.class);
    assertEquals("ya29.AHES6ZRzh", tokens.getAccessToken());
    assertNotNull(tokens.getExpiresAt());
    assertEquals(1326931739L, tokens.getExpiresAt().longValue());
    assertNotNull(tokens.getExpiresIn());
    assertEquals(3599L, tokens.getExpiresIn().longValue());
    assertEquals(JSON_FACTORY.fromString(json, OAuth2ServiceTokenMetadata.class), tokens);
    assertTrue(tokens.equals(JSON_FACTORY.fromString(json, OAuth2ServiceTokenMetadata.class)));
  }

  @Test
  public void testCommonMethods() throws IOException {
    OAuth2ServiceTokenMetadata tokens1 = JSON_FACTORY.fromString(
        "{\"accessToken\":\"ya29.AHES6ZRzh\",\"expiresAt\":1326931739,\"expiresIn\":3599}",
        OAuth2ServiceTokenMetadata.class);
    OAuth2ServiceTokenMetadata tokens2 = new OAuth2ServiceTokenMetadata();
    tokens2.setAccessToken("ya29.AHES6ZRzh");
    tokens2.setExpiresAt(1326931739L);
    tokens2.setExpiresIn(3599L);
    OAuth2ServiceTokenMetadata tokens3 = JSON_FACTORY.fromString(
        "{\"accessToken\":\"ya29.AHES6ZRzh\",\"expiresAt\":1326931739,\"expiresIn\":3577}",
        OAuth2ServiceTokenMetadata.class);

    TestUtil.testCommonMethods(tokens1,  tokens2, tokens3);
  }
}
