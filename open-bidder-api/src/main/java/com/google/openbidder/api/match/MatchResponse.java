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

package com.google.openbidder.api.match;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.openbidder.api.interceptor.UserResponse;
import com.google.openbidder.api.platform.Exchange;
import com.google.openbidder.http.HttpResponse;

import org.apache.http.HttpStatus;

import java.net.URI;
import java.util.Collection;

/**
 * Pixel-matching response.
 */
public class MatchResponse extends UserResponse<MatchResponse> {

  protected MatchResponse(Exchange exchange, HttpResponse.Builder httpResponseBuilder) {
    super(exchange, httpResponseBuilder);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  @Override public Builder toBuilder() {
    return newBuilder()
        .setExchange(getExchange())
        .setHttpResponse(httpResponse());
  }

  public final URI getRedirectUri() {
    return checkNotNull(httpResponse().getRedirectUri(),
        "Error: match response should have a redirect URI");
  }

  public MatchResponse setRedirectUri(String uri) {
    httpResponse().setRedirectUri(uri);
    return self();
  }

  public MatchResponse setRedirectUri(URI uri) {
    httpResponse().setRedirectUri(uri);
    return self();
  }

  public final String getHostName() {
    return getRedirectUri().getHost();
  }

  public final String getPath() {
    return getRedirectUri().getPath();
  }

  /**
   * Returns a collection with all values for a given parameter name, or an empty collection
   * if no parameter with this name exists.
   */
  public final Collection<String> getRedirectParameters(String name) {
    return httpResponse().getRedirectParameters(name);
  }

  /**
   * Puts a parameter in the output parameter multimap.
   */
  public MatchResponse putRedirectParameter(String key, String value) {
    checkParameterKey(key);
    httpResponse().addRedirectParameter(key, value);
    return self();
  }

  /**
   * Removes a parameter in the output parameter multimap.
   */
  public MatchResponse removeRedirectParameter(String key, String value) {
    checkParameterKey(key);
    httpResponse().removeRedirectParameter(key, value);
    return self();
  }

  protected void checkParameterKey(String key) {
    checkArgument(!Strings.isNullOrEmpty(key));
  }

  /**
   * Builder for {@link MatchResponse}.
   */
  public static class Builder extends UserResponse.Builder<Builder> {
    protected Builder() {
    }

    @Override protected int defaultStatusCode() {
      return HttpStatus.SC_MOVED_PERMANENTLY;
    }

    @Override public MatchResponse build() {
      return new MatchResponse(
          MoreObjects.firstNonNull(getExchange(), defaultExchange()),
          getHttpResponse());
    }
  }
}
