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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.net.MediaType;
import com.google.openbidder.api.match.MatchController;
import com.google.openbidder.api.match.MatchRequest;
import com.google.openbidder.exchange.doubleclick.DoubleClickConstants;
import com.google.openbidder.exchange.doubleclick.config.DoubleClickMatchRedirectUrl;
import com.google.openbidder.exchange.doubleclick.config.DoubleClickNid;
import com.google.openbidder.http.HttpRequest;
import com.google.openbidder.http.HttpResponse;
import com.google.openbidder.http.util.PixelImage;
import com.google.openbidder.match.MatchRequestReceiver;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

import org.apache.http.HttpStatus;

import java.io.IOException;
import java.net.URI;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * {@link MatchRequestReceiver} for DoubleClick Ad Exchange.
 */
@Singleton
public class DoubleClickMatchRequestReceiver extends MatchRequestReceiver {

  private final URI matchDoubleClickRedirectUrl;
  private final String matchDoubleClickNid;
  private final Meter pushMeter;

  @Inject
  public DoubleClickMatchRequestReceiver(
      MetricRegistry metricRegistry,
      MatchController controller,
      @DoubleClickMatchRedirectUrl URI matchDoubleClickRedirectUrl,
      @DoubleClickNid String matchDoubleClickNid) {

    super(DoubleClickConstants.EXCHANGE, metricRegistry, controller);
    this.matchDoubleClickRedirectUrl = checkNotNull(matchDoubleClickRedirectUrl);
    this.matchDoubleClickNid = checkNotNull(matchDoubleClickNid);
    this.pushMeter = buildMeter("push");
  }

  @Override
  protected DoubleClickMatchRequest.Builder newRequest(HttpRequest httpRequest) {
    return DoubleClickMatchRequest.newBuilder().setHttpRequest(httpRequest);
  }

  @Override
  protected DoubleClickMatchResponse.Builder newResponse(
      MatchRequest request, HttpResponse.Builder httpResponse) {
    DoubleClickMatchRequest req = (DoubleClickMatchRequest) request;
    if (req.isPush()) {
      httpResponse.setStatusCode(HttpStatus.SC_MOVED_TEMPORARILY);
      httpResponse.setRedirectUri(matchDoubleClickRedirectUrl);
      httpResponse.setRedirectParameter(DoubleClickMatchTag.GOOGLE_NID, matchDoubleClickNid);
      httpResponse.setRedirectParameter(DoubleClickMatchTag.GOOGLE_PUSH, req.getPushData());
      pushMeter.mark();
    } else {
      httpResponse.setStatusOk();
      httpResponse.setMediaType(MediaType.GIF);
      try {
        PixelImage.write(httpResponse.content());
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    }
    return DoubleClickMatchResponse.newBuilder().setHttpResponse(httpResponse);
  }
}
