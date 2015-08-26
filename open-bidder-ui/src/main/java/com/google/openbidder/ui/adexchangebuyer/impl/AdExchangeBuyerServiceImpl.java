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

package com.google.openbidder.ui.adexchangebuyer.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.adexchangebuyer.AdExchangeBuyer;
import com.google.openbidder.ui.adexchangebuyer.AdExchangeBuyerClient;
import com.google.openbidder.ui.adexchangebuyer.AdExchangeBuyerService;
import com.google.openbidder.ui.project.ProjectUser;
import com.google.openbidder.ui.user.AuthorizationService;

import javax.inject.Inject;

/**
 * Default implementation for {@link AdExchangeBuyerService}
 */
public class AdExchangeBuyerServiceImpl implements AdExchangeBuyerService {

  private final AuthorizationService authorizationService;
  private final HttpTransport httpTransport;
  private final JsonFactory jsonFactory;

  @Inject
  public AdExchangeBuyerServiceImpl(
      AuthorizationService authorizationService,
      HttpTransport httpTransport,
      JsonFactory jsonFactory) {
    this.authorizationService = checkNotNull(authorizationService);
    this.httpTransport = checkNotNull(httpTransport);
    this.jsonFactory = checkNotNull(jsonFactory);
  }

  @Override
  public AdExchangeBuyerClient connect(ProjectUser projectUser) {
    checkNotNull(projectUser);
    AdExchangeBuyer adexchangebuyer = new AdExchangeBuyer.Builder(
            httpTransport,
            jsonFactory,
            authorizationService.getCredentialsForProject(projectUser))
        .setApplicationName("Open Bidder")
        .build();
    return new AdExchangeBuyerClientImpl(adexchangebuyer);
  }
}
