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

package com.google.openbidder.binary;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Arrays.asList;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.inject.Module;
import com.google.openbidder.bidding.BidInterceptorsModule;
import com.google.openbidder.bidding.BidModule;
import com.google.openbidder.click.ClickModule;
import com.google.openbidder.googleapi.GoogleApiModule;
import com.google.openbidder.googlecompute.GoogleComputeModule;
import com.google.openbidder.http.HttpModule;
import com.google.openbidder.http.template.TemplateModule;
import com.google.openbidder.http.template.mustache.MustacheModule;
import com.google.openbidder.impression.ImpressionModule;
import com.google.openbidder.jetty.JettyModule;
import com.google.openbidder.metrics.MetricsModule;
import com.google.openbidder.netty.server.NettyServerModule;
import com.google.openbidder.oauth.OAuth2Module;
import com.google.openbidder.server.LoadBalancerModule;
import com.google.openbidder.server.ServerModule;
import com.google.openbidder.server.ServiceWrapper;
import com.google.openbidder.system.SystemModule;
import com.google.openbidder.util.FasterxmlJsonModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * Base implementation for a bidder's main class: master configurator, bootstrapper and controller.
 */
public abstract class BidderServer extends ServiceWrapper {
  private static final Logger logger = LoggerFactory.getLogger(BidderServer.class);
  private static final String JETTY_HOME = "jetty.home";
  private static boolean useNetty = true;

  protected BidderServer(String[] args) {
    super(procesBootstrapArgs(args));
  }

  private static String[] procesBootstrapArgs(String[] args) {
    Iterable<String> retArgs = Iterables.filter(asList(args), new Predicate<String>() {
      @Override public boolean apply(String arg) {
        if ("--jetty".equals(arg)) {
          useNetty = false;
          return false;
        } else if ("--netty".equals(arg)) {
          useNetty = true;
          return false;
        } else {
          return true;
        }
      }});
    return Iterables.toArray(retArgs, String.class);
  }

  /**
   * Configures the server with all primary modules to load.
   * You can override this to add or remove modules.
   *
   * @see #getJava8Modules()
   */
  @Override
  protected ImmutableList<Module> getModules() {
    ImmutableList.Builder<Module> modules = ImmutableList.<Module>builder().add(
        new SystemModule(),
        new FasterxmlJsonModule(),
        new ServerModule(),
        new HttpModule(),
        new TemplateModule(),
        new MustacheModule(),
        new LoadBalancerModule(),
        new GoogleApiModule(),
        new GoogleComputeModule(),
        new MetricsModule(),
        new OAuth2Module(),
        new BidModule(),
        new BidInterceptorsModule(),
        new ImpressionModule(),
        new ClickModule());

    if (useNetty) {
      modules.add(new NettyServerModule());
      logger.info("Selecting Netty server stack.");
    } else {
      modules.add(new JettyModule());

      checkState(!Strings.isNullOrEmpty(System.getProperty(JETTY_HOME)),
          "Must set system property jetty.home to start");

      logger.info("Selecting Jetty server stack.");
      logger.info("Jetty home set to: {}", System.getProperty(JETTY_HOME));
    }

    if (new BigDecimal("1.8").compareTo(getJavaVersion()) <= 0) {
      modules.addAll(getJava8Modules());
    }

    return modules.build();
  }

  /**
   * Returns modules that require Java 8 or better runtime.
   */
  protected ImmutableList<Module> getJava8Modules() {
    return ImmutableList.<Module>of();
  }

  protected static BigDecimal getJavaVersion () {
    String version = System.getProperty("java.version");
    int pos = version.indexOf('.');
    pos = version.indexOf('.', pos+1);
    return new BigDecimal(version.substring (0, pos));
  }
}
