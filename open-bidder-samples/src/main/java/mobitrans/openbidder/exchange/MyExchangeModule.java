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

package mobitrans.openbidder.exchange;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.openbidder.config.http.Feature;
import com.google.openbidder.http.route.AbstractHttpRouteProvider;
import com.google.openbidder.http.route.HttpRoute;
import com.google.openrtb.mapper.OpenRtbMapper;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Inject;

@Parameters(separators = "=")
public class MyExchangeModule extends AbstractModule {
  @Parameter(names = "--my_bid_path",
      description = "Path spec for MyExchange's bid requests")
  private String path = "/bid_request/mobfox";

  @Override protected void configure() {
    bind(String.class).annotatedWith(BidRequestPath.class).toInstance(path);
    Multibinder.newSetBinder(binder(), HttpRoute.class).addBinding()
        .toProvider(BidRouteProvider.class).in(Scopes.SINGLETON);
    bind(new TypeLiteral<OpenRtbMapper<String, String, StringBuilder, StringBuilder>>() {})
        .to(MyExchangeOpenRtbMapper.class);
  }

  public static class BidRouteProvider extends AbstractHttpRouteProvider {
    @Inject private BidRouteProvider(
        @BidRequestPath String path, MyExchangeRequestReceiver receiver) {
      super(HttpRoute.post("bid", path, receiver, Feature.BID));
    }
  }

  @BindingAnnotation @Target({ FIELD, PARAMETER, METHOD }) @Retention(RUNTIME)
  public static @interface BidRequestPath {}
}
