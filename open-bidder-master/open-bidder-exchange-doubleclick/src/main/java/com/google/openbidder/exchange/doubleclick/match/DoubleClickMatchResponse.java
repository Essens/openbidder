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

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.Iterables;
import com.google.common.io.BaseEncoding;
import com.google.openbidder.api.match.MatchResponse;
import com.google.openbidder.api.platform.Exchange;
import com.google.openbidder.exchange.doubleclick.DoubleClickConstants;
import com.google.openbidder.http.HttpResponse;
import com.google.protobuf.ByteString;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

/**
 * {@link MatchResponse} for the DoubleClick Ad Exchange.
 */
public class DoubleClickMatchResponse extends MatchResponse {

  protected DoubleClickMatchResponse(HttpResponse.Builder httpResponseBuilder) {
    super(DoubleClickConstants.EXCHANGE, httpResponseBuilder);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  @Override
  public Builder toBuilder() {
    return newBuilder()
        .setExchange(getExchange())
        .setHttpResponse(httpResponse().build().toBuilder());
  }

  public final String getCookieMatchNid() {
    return httpResponse().getRedirectParameter(DoubleClickMatchTag.GOOGLE_NID);
  }

  public final DoubleClickMatchResponse setCookieMatchNid(String cookieMatchNid) {
    httpResponse().setRedirectParameter(DoubleClickMatchTag.GOOGLE_NID, cookieMatchNid);
    return this;
  }

  public final ByteString getHostedMatch() {
    Collection<String> values = httpResponse().getRedirectParameters(
        DoubleClickMatchTag.GOOGLE_HOSTED_MATCH);
    if (values.size() == 0) {
      return ByteString.EMPTY;
    }
    return ByteString.copyFrom(BaseEncoding.base64Url().decode(Iterables.getFirst(values, null)));
  }

  public final DoubleClickMatchResponse setHostedMatch(ByteString hostedMatch) {
    httpResponse().setRedirectParameter(DoubleClickMatchTag.GOOGLE_HOSTED_MATCH,
        BaseEncoding.base64Url().encode(hostedMatch.toByteArray()));
    return this;
  }

  public final boolean isCookieMatch() {
    return httpResponse().containsRedirectParameter(DoubleClickMatchTag.GOOGLE_COOKIE_MATCH);
  }

  public final DoubleClickMatchResponse setCookieMatch(boolean cookieMatch) {
    if (cookieMatch) {
      httpResponse().addRedirectParameter(DoubleClickMatchTag.GOOGLE_COOKIE_MATCH, "");
    } else {
      httpResponse().removeRedirectParameter(DoubleClickMatchTag.GOOGLE_COOKIE_MATCH);
    }
    return this;
  }

  public final boolean isAddCookie() {
    return httpResponse().containsRedirectParameter(DoubleClickMatchTag.GOOGLE_SET_COOKIE);
  }

  public final DoubleClickMatchResponse setAddCookie(boolean addCookie) {
    if (addCookie) {
      httpResponse().addRedirectParameter(DoubleClickMatchTag.GOOGLE_SET_COOKIE, "");
    } else {
      httpResponse().removeRedirectParameter(DoubleClickMatchTag.GOOGLE_SET_COOKIE);
    }
    return this;
  }

  public final DoubleClickMatchResponse putUserList(long userList) {
    httpResponse().addRedirectParameter(
        DoubleClickMatchTag.GOOGLE_USER_LIST,
        Long.toString(userList));
    return this;
  }

  public final DoubleClickMatchResponse putUserList(long userList, long timestamp) {
    httpResponse().addRedirectParameter(
        DoubleClickMatchTag.GOOGLE_USER_LIST,
        String.format("%d,%d", userList, timestamp));
    return this;
  }

  public final DoubleClickMatchResponse removeUserList(long userList) {
    httpResponse().removeRedirectParameter(
        DoubleClickMatchTag.GOOGLE_USER_LIST,
        Long.toString(userList));
    String prefix = String.format("%d,", userList);
    for (String value : httpResponse().getRedirectParameters(DoubleClickMatchTag.GOOGLE_USER_LIST)) {
      if (value.startsWith(prefix)) {
        httpResponse().removeRedirectParameter(DoubleClickMatchTag.GOOGLE_USER_LIST, value);
      }
    }
    return this;
  }

  public final DoubleClickMatchResponse clearUserLists() {
    httpResponse().removeRedirectParameter(DoubleClickMatchTag.GOOGLE_USER_LIST);
    return this;
  }

  @Override
  protected void checkParameterKey(String key) {
    super.checkParameterKey(key);
    checkArgument(!key.startsWith(DoubleClickMatchTag.GOOGLE_RESERVED_TAG));
  }

  // Overrides for covariance
  @Override public DoubleClickMatchResponse setRedirectUri(String uri) {
    return (DoubleClickMatchResponse) super.setRedirectUri(uri);
  }
  @Override public DoubleClickMatchResponse setRedirectUri(URI uri) {
    return (DoubleClickMatchResponse) super.setRedirectUri(uri);
  }
  @Override public DoubleClickMatchResponse putRedirectParameter(String key, String value) {
    return (DoubleClickMatchResponse) super.putRedirectParameter(key, value);
  }
  @Override public DoubleClickMatchResponse removeRedirectParameter(String key, String value) {
    return (DoubleClickMatchResponse) super.removeRedirectParameter(key, value);
  }
  @Override public DoubleClickMatchResponse putMetadata(String key, Object value) {
    return (DoubleClickMatchResponse) super.putMetadata(key, value);
  }
  @Override public DoubleClickMatchResponse putAllMetadata(Map<String, Object> metadata) {
    return (DoubleClickMatchResponse) super.putAllMetadata(metadata);
  }

  /**
   * Builder for {@link DoubleClickMatchResponse}.
   */
  public static class Builder extends MatchResponse.Builder {
    protected Builder() {
    }

    @Override
    protected Exchange defaultExchange() {
      return DoubleClickConstants.EXCHANGE;
    }

    @Override
    public DoubleClickMatchResponse build() {
      return new DoubleClickMatchResponse(getHttpResponse());
    }

    // Overrides for covariance
    @Override public Builder setExchange(Exchange exchange) {
      return (Builder) super.setExchange(exchange);
    }
    @Override public Builder setHttpResponse(HttpResponse.Builder httpResponse) {
      return (Builder) super.setHttpResponse(httpResponse);
    }
  }
}
