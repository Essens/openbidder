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

package com.google.openbidder.remarketing.interceptor;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Iterables;
import com.google.openbidder.api.interceptor.InterceptorChain;
import com.google.openbidder.api.match.MatchInterceptor;
import com.google.openbidder.api.match.MatchRequest;
import com.google.openbidder.api.match.MatchResponse;
import com.google.openbidder.http.Cookie;
import com.google.openbidder.remarketing.model.Remarketing.TargetedUser;
import com.google.openbidder.remarketing.services.RemarketingService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Remarketing Match Interceptor.
 */
public class RemarketingMatchInterceptor implements MatchInterceptor {
  private static final Logger logger = LoggerFactory.getLogger(RemarketingMatchInterceptor.class);
  private static final String ADD_ACTION = "add_action";
  private static final String DELETE_ACTION = "delete_action";
  private static final String PUB_ID = "pub_id";

  private final RemarketingService remarketingService;

  @Inject
  public RemarketingMatchInterceptor(RemarketingService remarketingService) {
    this.remarketingService = checkNotNull(remarketingService);
  }

  @Override
  public void execute(InterceptorChain<MatchRequest, MatchResponse> chain) {
    chain.proceed();

    String googleId = chain.request().getUserId();
    String pubUserId = getPubUserId(chain.request().httpRequest().getCookies(PUB_ID));
    Collection<String> addActions = chain.request().httpRequest().getParameters(ADD_ACTION);
    Collection<String> deleteActions = chain.request().httpRequest().getParameters(DELETE_ACTION);

    // A publisher user id is required to add or delete actions.
    if (pubUserId != null) {
      // Build a targeted user if they have actions to add.
      if (!addActions.isEmpty()) {
        TargetedUser.Builder user = TargetedUser.newBuilder()
            .setPubUserId(pubUserId)
            .addAllActionId(addActions);
        if (googleId != null) {
          user.addGoogleGid(googleId);
        }
        remarketingService.addTargetedUser(user.build());

        if (logger.isDebugEnabled()) {
          logger.debug("Adding targeted user:\n{}", user);
        }
      }

      // Delete any actions.
      for (String action : deleteActions) {
        remarketingService.deleteActionForUser(pubUserId, action);
      }

      if (logger.isDebugEnabled() && !deleteActions.isEmpty()) {
        logger.debug("Deleting actions for user {}: {}", pubUserId, deleteActions);
      }
    }
  }

  @Nullable
  private static String getPubUserId(Iterable<Cookie> cookies) {
    Cookie cookie = Iterables.getFirst(cookies, null);
    return cookie == null ? null : cookie.getValue();
  }
}
