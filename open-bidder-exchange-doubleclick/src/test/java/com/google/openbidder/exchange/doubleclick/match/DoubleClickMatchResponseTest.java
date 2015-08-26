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

package com.google.openbidder.exchange.doubleclick.match;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.api.client.util.escape.CharEscapers;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.openbidder.exchange.doubleclick.testing.TestMatchResponseBuilder;
import com.google.openbidder.http.response.StandardHttpResponse;
import com.google.openbidder.util.testing.TestUtil;
import com.google.protobuf.ByteString;

import org.junit.Test;

/**
 * Tests for {@link DoubleClickMatchResponse}.
 */
public class DoubleClickMatchResponseTest {
  static final String DOUBLECLICK_REDIRECT_URI = "http://cm.g.doubleclick.net";
  static final String COOKIE_MATCH_NID = "rtbbuyer";
  static final String HOSTED_MATCH = "hostedmatch";

  @Test
  public void testCommonMethods() {
    DoubleClickMatchResponse resp = TestMatchResponseBuilder.create().build();
    TestUtil.testCommonMethods(resp);
  }

  @Test
  public void testBuilder() {
    DoubleClickMatchResponse.Builder resp = DoubleClickMatchResponse.newBuilder()
        .setHttpResponse(StandardHttpResponse.newBuilder());
    TestUtil.testCommonMethods(resp);
    assertNotNull(resp.build().toBuilder().build());
  }

  @Test
  public void constructor_push_nidAndPushSet() {
    DoubleClickMatchResponse matchResponse = newResponse();
    Multimap<String, String> expectedParams = HashMultimap.create();
    expectedParams.put(DoubleClickMatchTag.GOOGLE_NID, COOKIE_MATCH_NID);
    assertFalse(matchResponse.isAddCookie());
    assertFalse(matchResponse.isCookieMatch());
    assertEquals(DOUBLECLICK_REDIRECT_URI, matchResponse.getRedirectUri().toString());
    assertEquals(0, matchResponse.getHostedMatch().size());
  }

  public void constructor_nothingSet_emptyValues() {
    DoubleClickMatchResponse matchResponse = newResponse();
    assertNull(matchResponse.getCookieMatchNid());
    assertFalse(matchResponse.isAddCookie());
    assertFalse(matchResponse.isCookieMatch());
  }

  @Test
  public void cookieMatchNid_set_valueSet() {
    DoubleClickMatchResponse matchResponse = newResponse();
    assertNull(matchResponse.getCookieMatchNid());
    matchResponse.setCookieMatchNid(COOKIE_MATCH_NID);
    assertEquals(COOKIE_MATCH_NID, matchResponse.getCookieMatchNid());
  }

  @Test
  public void addCookie_set_valueSet() {
    DoubleClickMatchResponse matchResponse = newResponse();
    matchResponse.setAddCookie(false);
    assertFalse(matchResponse.isAddCookie());
    matchResponse.setAddCookie(true);
    assertTrue(matchResponse.isAddCookie());
  }

  @Test
  public void cookieMatch_set_valueSet() {
    DoubleClickMatchResponse matchResponse = newResponse();
    matchResponse.setCookieMatch(false);
    assertFalse(matchResponse.isCookieMatch());
    matchResponse.setCookieMatch(true);
    assertTrue(matchResponse.isCookieMatch());
  }

  @Test
  public void hostedMatch_set_valueSet() {
    DoubleClickMatchResponse matchResponse = newResponse();
    assertEquals(0, matchResponse.getHostedMatch().size());
    matchResponse.setHostedMatch(ByteString.copyFromUtf8("test"));
    assertEquals(ByteString.copyFromUtf8("test"), matchResponse.getHostedMatch());
    assertEquals(4, matchResponse.getHostedMatch().size());
  }

  @Test
  public void userList_set_valueSet() {
    DoubleClickMatchResponse matchResponse = newResponse();
    assertEquals(DOUBLECLICK_REDIRECT_URI, matchResponse.getRedirectUri().toString());
    matchResponse.putUserList(100);
    assertEquals(
        DOUBLECLICK_REDIRECT_URI + '?' + DoubleClickMatchTag.GOOGLE_USER_LIST + "=100",
        matchResponse.getRedirectUri().toString());
    matchResponse.clearUserLists();
    matchResponse.putUserList(100, 200);
    assertEquals(
        DOUBLECLICK_REDIRECT_URI + '?' + DoubleClickMatchTag.GOOGLE_USER_LIST
            + '=' + CharEscapers.escapeUri("100,200"),
        matchResponse.getRedirectUri().toString());
    matchResponse.removeUserList(100);
    assertEquals(DOUBLECLICK_REDIRECT_URI, matchResponse.getRedirectUri().toString());
  }

  private static DoubleClickMatchResponse newResponse() {
    return TestMatchResponseBuilder.create().build()
        .setRedirectUri(DOUBLECLICK_REDIRECT_URI);
  }
}
