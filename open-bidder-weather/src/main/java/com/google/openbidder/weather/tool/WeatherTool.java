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

package com.google.openbidder.weather.tool;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.common.collect.Iterators;
import com.google.openbidder.cloudstorage.GoogleCloudStorageFactory;
import com.google.openbidder.oauth.generic.ConfiguredOAuth2CredentialFactory;
import com.google.openbidder.weather.WeatherDao;
import com.google.openbidder.weather.WeatherDaoCloudStorage;
import com.google.openbidder.weather.model.Weather.WeatherBiddingRule;
import com.google.openbidder.weather.model.Weather.WeatherRules;
import com.google.openbidder.weather.model.Weather.WeatherTarget;

import java.util.Iterator;

/**
 * Command-line tool for managing test data.
 *
 * TODO(opinali): Convert to JCommander
 */
public class WeatherTool {

  public static void main(String[] args) {
    if (args.length < 2 || !args[1].startsWith("gs://")) {
      help();
      System.exit(0);
    }

    switch (args[0]) {
      case "addrules":
        addRules(args);
        break;
      case "delrules":
        delRules(args);
        break;
      case "lsrules":
        lsRules(args);
        break;
      default:
        help();
        System.exit(1);
    }
  }

  private static WeatherDao createDao(String bucketName, boolean write) {
    HttpTransport httpTransport = new NetHttpTransport();
    Credential credential = new ConfiguredOAuth2CredentialFactory(
        new JacksonFactory(),
        httpTransport,
        getProperty("Google.OAuth2.P12FilePath"),
        getProperty("Google.OAuth2.ServiceAccountId"))
        .retrieveCredential(write
            ? "https://www.googleapis.com/auth/devstorage.full_control"
            : "https://www.googleapis.com/auth/devstorage.read_only");
    return new WeatherDaoCloudStorage(
        GoogleCloudStorageFactory.newFactory()
            .setHttpTransport(httpTransport)
            .setApiProjectNumber(Long.parseLong(getProperty("Google.Storage.ProjectId")))
            .setCredential(credential)
            .build(),
        bucketName);
  }

  private static String getProperty(String name) {
    String value = System.getProperty(name);

    if (value == null) {
      System.err.println("Missing system property " + name);
      System.exit(1);
    }

    return value;
  }

  private static void addRules(String[] args) {
    if ((args.length - 3) % 7 != 0) {
      System.err.println(args.length);
      helpAdd();
      System.exit(1);
    }

    Iterator<String> iter = Iterators.forArray(args);
    iter.next();
    WeatherDao dao = createDao(iter.next(), true);
    WeatherRules.Builder rules = WeatherRules.newBuilder().setOwnerId(iter.next());
    String value;

    while (iter.hasNext()) {
      WeatherTarget.Builder target = WeatherTarget.newBuilder();
      if (!"null".equals(value = iter.next())) {
        target.setMinTemp(Integer.parseInt(value));
      }
      if (!"null".equals(value = iter.next())) {
        target.setMaxTemp(Integer.parseInt(value));
      }
      if (!"null".equals(value = iter.next())) {
        target.setMinWind(Integer.parseInt(value));
      }
      if (!"null".equals(value = iter.next())) {
        target.setMaxWind(Integer.parseInt(value));
      }
      if (!"null".equals(value = iter.next())) {
        target.setMinHumidity(Double.parseDouble(value));
      }
      if (!"null".equals(value = iter.next())) {
        target.setMaxHumidity(Double.parseDouble(value));
      }
      rules.addRules(WeatherBiddingRule.newBuilder()
          .setTarget(target)
          .setMultiplier(Double.parseDouble(iter.next())));
    }

    dao.insert(rules.build());
  }

  private static void delRules(String[] args) {
    if (args.length != 3) {
      helpDel();
      System.exit(1);
    }

    createDao(args[1], true).deleteRules(args[2]);
  }

  private static void lsRules(String[] args) {
    if (args.length != 2) {
      helpLs();
      System.exit(1);
    }

    for (WeatherRules rules : createDao(args[1], false).listRules()) {
      System.out.println(rules);
    }
  }

  private static void help() {
    helpAdd();
    helpDel();
    helpLs();
  }

  private static void helpAdd() {
    System.out.println(
          "WeatherTool addrules <bucket> <cid> "
        + "(<tMin> <tMax> <wMin> <wMax> <hMin> <hMax> <multiplier>)*\n"
        + "            (use 'null' for unspecified limits)");
  }

  private static void helpDel() {
    System.out.println(
        "WeatherTool delrules <bucket> <cid>");
  }

  private static void helpLs() {
    System.out.println(
        "WeatherTool lsrules <bucket>");
  }
}
