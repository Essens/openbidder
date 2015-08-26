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

package com.google.openbidder.exchange.doubleclick;

import com.google.api.client.http.HttpTransport;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.io.BaseEncoding;
import com.google.doubleclick.crypto.DoubleClickCrypto;
import com.google.doubleclick.openrtb.DoubleClickLinkMapper;
import com.google.doubleclick.openrtb.DoubleClickOpenRtbMapper;
import com.google.doubleclick.openrtb.ExtMapper;
import com.google.doubleclick.openrtb.NullDoubleClickOpenRtbMapper;
import com.google.doubleclick.util.DoubleClickMetadata;
import com.google.doubleclick.util.DoubleClickValidator;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.util.Providers;
import com.google.openbidder.api.platform.Exchange;
import com.google.openbidder.config.http.Feature;
import com.google.openbidder.exchange.doubleclick.config.DoubleClick;
import com.google.openbidder.exchange.doubleclick.config.DoubleClickBidRequestPath;
import com.google.openbidder.exchange.doubleclick.server.DoubleClickBidRequestReceiver;
import com.google.openbidder.exchange.doubleclick.server.OpenBidderExtMapper;
import com.google.openbidder.http.route.AbstractHttpRouteProvider;
import com.google.openbidder.http.route.HttpRoute;
import com.google.openbidder.util.ResourceHttpTransport;
import com.google.openrtb.mapper.OpenRtbMapper;
import com.google.protobuf.MessageLite;
import com.google.protos.adx.NetworkBid;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidKeyException;
import java.util.List;

import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Module to enable functionality specific to the DoubleClick Ad Exchange.
 */
@Parameters(separators = "=")
public class DoubleClickModule extends AbstractModule {
  private static final Logger logger = LoggerFactory.getLogger(DoubleClickModule.class);

  @Parameter(names = "--doubleclick_bid_path", required = false,
      description = "Path spec for DoubleClick bid requests")
  private String path = DoubleClickBidRequestPath.DEFAULT;

  @Parameter(names = "--doubleclick_encryption_key", required = false,
      description = "Winning price encryption key (base 64)")
  private String encryptionKey;

  @Parameter(names = "--doubleclick_integrity_key", required = false,
      description = "Winning price integrity key (base 64)")
  private String integrityKey;

  @Parameter(names = "--doubleclick_local_resources",
      description = "Load DoubleClick dictionaries from local resources (test only)")
  private boolean localResources = false;

  @Parameter(names = "--doubleclick_openrtb",
      description = "Enable OpenRTB mapping")
  private boolean openRtb = true;

  @Parameter(names = "--doubleclick_validate", arity = 1,
      description = "Validate response messages")
  private boolean validate = false;

  @Override
  protected void configure() {
    // Supports single-exchange operation. If we support multiple exchanges in the same bidder in
    // the future, remove this and pass the correct exchange in Providers of objects that need it.
    bind(Exchange.class).toInstance(DoubleClickConstants.EXCHANGE);

    bind(MessageLite.class).annotatedWith(DoubleClick.class)
        .toInstance(NetworkBid.BidRequest.getDefaultInstance());
    boolean doubleClickBiddingEnabled = !Strings.isNullOrEmpty(path);
    if (doubleClickBiddingEnabled) {
      logger.info("Binding DoubleClick bid requests to: {}", path);
      bind(String.class).annotatedWith(DoubleClickBidRequestPath.class).toInstance(path);
      Multibinder.newSetBinder(binder(), HttpRoute.class).addBinding()
          .toProvider(HttpRouteProvider.class).in(Scopes.SINGLETON);
      bind(new TypeLiteral<List<ExtMapper>>() {}).toInstance(ImmutableList.<ExtMapper>of(
          DoubleClickLinkMapper.INSTANCE, OpenBidderExtMapper.INSTANCE));
      bind(new TypeLiteral<OpenRtbMapper<
              NetworkBid.BidRequest, NetworkBid.BidResponse,
              NetworkBid.BidRequest.Builder, NetworkBid.BidResponse.Builder>>() {})
          .to(openRtb ? DoubleClickOpenRtbMapper.class : NullDoubleClickOpenRtbMapper.class);
      if (!validate) {
        bind(DoubleClickValidator.class).toProvider(Providers.<DoubleClickValidator>of(null));
      }
    } else {
      logger.info("DoubleClick bid request handling not installed");
    }

    if (!Strings.isNullOrEmpty(encryptionKey) && !Strings.isNullOrEmpty(integrityKey)) {
      try {
        BaseEncoding base64 = BaseEncoding.base64();
        bind(DoubleClickCrypto.Keys.class).toInstance(new DoubleClickCrypto.Keys(
            new SecretKeySpec(base64.decode(encryptionKey), DoubleClickCrypto.KEY_ALGORITHM),
            new SecretKeySpec(base64.decode(integrityKey), DoubleClickCrypto.KEY_ALGORITHM)));
        logger.info("Setting Encryption and Integrity keys");
      } catch (InvalidKeyException e) {
        throw new IllegalStateException(e);
      }
    } else {
      logger.info("Encryption and Integrity keys not set");
      bind(DoubleClickCrypto.Keys.class).toProvider(Providers.<DoubleClickCrypto.Keys>of(null));
      bind(DoubleClickCrypto.AdId.class)
          .toProvider(Providers.<DoubleClickCrypto.AdId>of(null));
      bind(DoubleClickCrypto.Hyperlocal.class)
          .toProvider(Providers.<DoubleClickCrypto.Hyperlocal>of(null));
      bind(DoubleClickCrypto.Idfa.class)
          .toProvider(Providers.<DoubleClickCrypto.Idfa>of(null));
      bind(DoubleClickCrypto.Price.class)
          .toProvider(Providers.<DoubleClickCrypto.Price>of(null));
    }
  }

  @Provides
  @Singleton
  public DoubleClickMetadata provideDoubleClickMetadata(HttpTransport httpTransport) {
    return new DoubleClickMetadata(
        localResources && !(httpTransport instanceof ResourceHttpTransport)
            ? new DoubleClickMetadata.ResourceTransport()
            : new GoogleTransport(httpTransport));
  }

  public static class HttpRouteProvider extends AbstractHttpRouteProvider {
    @Inject
    private HttpRouteProvider(
        @DoubleClickBidRequestPath String path,
        DoubleClickBidRequestReceiver receiver) {
      super(HttpRoute.post("bid_doubleclick", path, receiver, Feature.BID));
    }
  }
}
