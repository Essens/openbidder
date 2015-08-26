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

package com.google.openbidder.ui.dao.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.googlecode.objectify.ObjectifyService.ofy;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.openbidder.ui.dao.UserPreferenceDao;
import com.google.openbidder.ui.entity.AuthorizationToken;
import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.entity.UserPreference;
import com.google.openbidder.ui.user.exception.UserNotFoundException;
import com.google.openbidder.ui.util.db.Db;
import com.google.openbidder.ui.util.db.Transactable;
import com.google.openbidder.util.Clock;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;

import org.springframework.beans.factory.annotation.Value;

import java.util.UUID;

import javax.inject.Inject;

/**
 * Default implementation of {@link UserPreferenceDao}.
 */
public class UserPreferenceDaoImpl implements UserPreferenceDao {

  private final Clock clock;
  private final long tokenTimeoutMilliseconds;

  @Inject
  public UserPreferenceDaoImpl(
      Clock clock,
      @Value("${Xsrf.Token.TimeoutMs}") long tokenTimeoutMilliseconds) {

    this.clock = checkNotNull(clock);
    this.tokenTimeoutMilliseconds = tokenTimeoutMilliseconds;
  }

  @Override
  public UserPreference getUserPreferences(String userEmail) {
    try {
      return ofy().load().type(UserPreference.class).id(userEmail).safe();
    } catch (NotFoundException e) {
      throw new UserNotFoundException(userEmail);
    }
  }

  @Override
  public <T> T updateUserPreferences(final String userEmail,
      final Transactable<UserPreference, T> worker) {
    return Db.createOrUpdateInTransaction(UserPreference.class, userEmail, worker);
  }

  @Override
  public Iterable<UserPreference> getAllUsersForProject(final long projectId) {
    return Iterables.filter(ofy().load().type(UserPreference.class)
        .filter("userRoles.project", Project.key(projectId)),
        new Predicate<UserPreference>() {
          @Override public boolean apply(UserPreference userPreference) {
            return userPreference.getProjectRole(projectId).isRead();
          }});
  }

  @Override
  public void removeProjectFromAllUserPreferences(final long projectId) {
    Iterable<UserPreference> userPreferences = ofy().load().type(UserPreference.class)
        .filter("userRoles.project", Project.key(projectId));
    for (UserPreference userPreference : userPreferences) {
      userPreference.removeFromProject(projectId);
      ofy().save().entity(userPreference);
    }
  }

  @Override
  public String getAuthorizationToken(String userEamil) {
    // Tokens are valid for a period of time eg 1 day but we don't want to return tokens
    // that are about to expire. If a token is at least half its age then we generate a new
    // token instead.
    Iterable<Key<AuthorizationToken>> query = ofy().load().type(AuthorizationToken.class)
        .ancestor(UserPreference.key(userEamil))
        .filter("createdAt >=", clock.now().minus(tokenTimeoutMilliseconds / 2))
        .order("-createdAt")
        .limit(1)
        .keys();
    Key<AuthorizationToken> authTokenKey = Iterables.getFirst(query, null);

    // For a valid token, just return the key name, which is the authorization token.
    if (authTokenKey != null) {
      return authTokenKey.getName();
    }

    // Otherwise, generate and store a new authorization token.
    AuthorizationToken authToken = new AuthorizationToken(
        userEamil,
        UUID.randomUUID().toString(),
        clock.now());
    ofy().save().entity(authToken);
    return authToken.getToken();
  }

  @Override
  public boolean isValidAuthorizationToken(String userEmail, String authorizationToken) {
    return ofy().load().now(AuthorizationToken.key(userEmail, authorizationToken)) != null;
  }
}
