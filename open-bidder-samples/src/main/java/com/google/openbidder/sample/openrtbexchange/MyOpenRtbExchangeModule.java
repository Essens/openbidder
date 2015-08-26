/*
 * Copyright 2015 Google Inc. All Rights Reserved.
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

package com.google.openbidder.sample.openrtbexchange;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import com.google.openbidder.bidding.BidModule;
import com.google.openbidder.config.http.Feature;
import com.google.openbidder.http.route.AbstractHttpRouteProvider;
import com.google.openbidder.http.route.HttpRoute;
import com.google.openbidder.sample.openrtbexchange.model.MyExt;
import com.google.openrtb.OpenRtb.BidRequest;
import com.google.openrtb.json.OpenRtbJsonFactory;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.fasterxml.jackson.core.JsonFactory;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Inject;
import javax.inject.Singleton;

@Parameters(separators = "=")
public class MyOpenRtbExchangeModule extends AbstractModule {
  @Parameter(names = "--myopenrtb_bid_path",
      description = "Path spec for MyOpenRtbExchange's bid requests")
  private String path = BidRequestPath.DEFAULT;

  @Override protected void configure() {
    bind(String.class).annotatedWith(BidRequestPath.class).toInstance(path);
    Multibinder.newSetBinder(binder(), HttpRoute.class).addBinding()
        .toProvider(BidRouteProvider.class).in(Scopes.SINGLETON);
  }

  @Provides @Singleton
  public OpenRtbJsonFactory provideOpenRtbJsonFactory(JsonFactory jsonFactory) {
    return registerMyExt(BidModule.registerObExt(
        OpenRtbJsonFactory.create().setJsonFactory(jsonFactory)));
  }

  // Our exchange uses some OpenRTB extensions, so we also need
  // to register a pair of Reader/Writer for each extended object.
  public static OpenRtbJsonFactory registerMyExt(OpenRtbJsonFactory factory) {
    return factory
        .register(new MyOpenRtbExchangeExtBannerReader(),
            BidRequest.Imp.Banner.Builder.class)
        .register(new MyOpenRtbExchangeExtBannerWriter(),
            MyExt.Banner.class, BidRequest.Imp.Banner.class);
  }

  public static class BidRouteProvider extends AbstractHttpRouteProvider {
    @Inject private BidRouteProvider(
        @BidRequestPath String path, MyOpenRtbExchangeRequestReceiver receiver) {
      super(HttpRoute.post("bid_openrtb", path, receiver, Feature.BID));
    }
  }

  @BindingAnnotation @Target({ FIELD, PARAMETER, METHOD }) @Retention(RUNTIME)
  public static @interface BidRequestPath {
    String DEFAULT = "/bid_request/openrtb";
  }
}
