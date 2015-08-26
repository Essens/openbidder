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

package com.google.openbidder.ui.user.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.util.Key;
import com.google.api.client.util.store.AbstractDataStoreFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.common.collect.ImmutableList;
import com.google.openbidder.ui.dao.UserPreferenceDao;
import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.entity.UserPreference;
import com.google.openbidder.ui.project.ProjectUser;
import com.google.openbidder.ui.user.AuthorizationService;
import com.google.openbidder.ui.user.UserIdService;
import com.google.openbidder.ui.user.UserInfo;
import com.google.openbidder.ui.user.exception.NoCredentialsForProjectException;
import com.google.openbidder.ui.util.db.Transactable;

import com.googlecode.objectify.Objectify;

import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Default implementation of {@link com.google.openbidder.ui.user.AuthorizationService}.
 * <p>
 * Assuming a user ID of "test@example.com" and a token of "1234", two memcache entries are
 * created:
 * <ol>
 *   <li>__xsrf__:test@example.com:1234 -> X</li>
 *   <li>__xsrf__:test@example.com -> 1234</li>
 * </ol>
 * The value for the first doesn't matter. Existence of any value indicates the token is valid.
 * <p>
 * The second is to return a valid token for the given user. When a token is created, both entries
 * are created. There is no guarantee on which token will be stored in (2) but it doesn't matter.
 * It simply has to be valid. It doesn't have to be the most recent.
 */
public class AuthorizationServiceImpl implements AuthorizationService {

  private final UserIdService userIdService;
  private final UserPreferenceDao userPreferenceDao;
  private final HttpTransport httpTransport;
  private final JsonFactory jsonFactory;
  private final ImmutableList<String> scopes;
  private final String userInfoUrl;

  @Inject
  public AuthorizationServiceImpl(
      UserIdService userIdService,
      UserPreferenceDao userPreferenceDao,
      HttpTransport httpTransport,
      JsonFactory jsonFactory,
      @Value("#{scopes}") Iterable<String> scopes,
      @Value("${URL.UserInfo}") String userInfoUrl) {

    this.userIdService = checkNotNull(userIdService);
    this.userPreferenceDao = checkNotNull(userPreferenceDao);
    this.httpTransport = checkNotNull(httpTransport);
    this.jsonFactory = checkNotNull(jsonFactory);
    this.scopes = ImmutableList.copyOf(scopes);
    this.userInfoUrl = checkNotNull(userInfoUrl);
  }

  @Override
  public AuthorizationCodeFlow initializeFlow(Project project) {
    checkNotNull(project);
    try {
      return new GoogleAuthorizationCodeFlow.Builder(
              httpTransport,
              jsonFactory,
              project.getOauth2ClientId(),
              project.getOauth2ClientSecret(),
              scopes)
          .setApprovalPrompt("force")
          .setAccessType("offline")
          .setDataStoreFactory(new CredentialDataStoreFactory(project.getId()))
          .build();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public boolean isAuthorized(ProjectUser projectUser) {
    AuthorizationCodeFlow flow = initializeFlow(projectUser.getProject());
    return isAuthorized(flow);
  }

  @Override
  public boolean isAuthorized(AuthorizationCodeFlow flow) {
    try {
      Credential credential = flow.loadCredential(userIdService.getUserId());
      return credential != null
          && credential.getAccessToken() != null
          && credential.getRefreshToken() != null;
    } catch (IOException e) {
      return false;
    }
  }

  @Override
  public Credential getCredentialsForProject(ProjectUser projectUser) {
    checkNotNull(projectUser);
    GoogleCredential credential = new GoogleCredential.Builder()
        .setTransport(httpTransport)
        .setJsonFactory(jsonFactory)
        .setClientSecrets(projectUser.getProject().getOauth2ClientId(),
            projectUser.getProject().getOauth2ClientSecret())
        .build();
    if (!projectUser.getUserRole().loadCredentials(credential)) {
      throw new NoCredentialsForProjectException(
          projectUser.getProject().getId(), projectUser.getEmail());
    }
    return credential;
  }

  @Override
  public ProjectUser revokeAuthorization(ProjectUser projectUser) {
    UserPreference userPreference = revokeTokens(projectUser);
    return userIdService.buildProjectUser(projectUser.getProject(), userPreference);
  }

  private UserPreference revokeTokens(final ProjectUser projectUser) {
    return userPreferenceDao.updateUserPreferences(projectUser.getEmail(),
        new Transactable<UserPreference, UserPreference>() {
          @Override public UserPreference work(UserPreference item, Objectify ofy) {
            item.getUserRole(projectUser.getProject().getId()).deleteCredentials();
            ofy.save().entity(item);
            return item;
          }});
  }

  @Override
  public UserInfo getUserInfo(ProjectUser projectUser) {
    final Credential credential = getCredentialsForProject(projectUser);
    HttpRequestFactory requestFactory = httpTransport.createRequestFactory(
        new HttpRequestInitializer() {
          @Override public void initialize(HttpRequest request) throws IOException {
            credential.initialize(request);
          }});

    GenericUrl url = new GenericUrl(userInfoUrl);
    HttpRequest request;
    try {
      request = requestFactory.buildGetRequest(url);
    } catch (IOException e) {
      throw new IllegalStateException("IO error on creating user info request", e);
    }
    request.setParser(new JsonObjectParser(jsonFactory));

    HttpResponse response;
    try {
      response = request.execute();
    } catch (HttpResponseException e) {
      // This should only happen if the OAuth access token doesn't have scope for userInfo.
      if (e.getStatusCode() == 401) {
        throw new NoCredentialsForProjectException(projectUser.getProject().getId(),
            projectUser.getEmail());
      }
      throw new IllegalStateException(e);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }

    try {
      UserInfoResponse userInfoResponse = response.parseAs(UserInfoResponse.class);
      return new UserInfo(userInfoResponse.getEmail(), userInfoResponse.isVerifiedEmail());
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public String getAuthorizationToken() {
    String userId = userIdService.getUserId();
    return userPreferenceDao.getAuthorizationToken(userId);
  }

  @Override
  public boolean isValidAuthorizationToken(String token) {
    String userId = userIdService.getUserId();
    return userPreferenceDao.isValidAuthorizationToken(userId, token);
  }

  class CredentialDataStoreFactory extends AbstractDataStoreFactory {
    private final long projectId;
    CredentialDataStoreFactory(long projectId) {
      this.projectId = projectId;
    }
    @Override protected DataStore<StoredCredential> createDataStore(String id) throws IOException {
      return new UserPreferenceCredentialStore(this, id, projectId);
    }
  }

  class UserPreferenceCredentialStore extends AbstractMemoryDataStore<StoredCredential> {
    private final long projectId;

    UserPreferenceCredentialStore(DataStoreFactory dataStoreFactory, String id, long projectId) {
      super(dataStoreFactory, id);
      this.projectId = projectId;
    }

    @Override public DataStore<StoredCredential> set(String key, final StoredCredential credential)
        throws IOException {
      super.set(key, credential);
      userPreferenceDao.updateUserPreferences(key, new Transactable<UserPreference, Void>() {
        @Override public @Nullable Void work(UserPreference item, Objectify ofy) {
          item.getUserRole(projectId).storeCredentials(credential);
          ofy.save().entity(item);
          return null;
        }});
      return this;
    }

    @Override public DataStore<StoredCredential> delete(String key) throws IOException {
      super.delete(key);
      userPreferenceDao.updateUserPreferences(key, new Transactable<UserPreference, Void>() {
        @Override public @Nullable Void work(UserPreference item, Objectify ofy) {
          item.getUserRole(projectId).deleteCredentials();
          ofy.save().entity(item);
          return null;
        }});
      return this;
    }
  }

  public static class UserInfoResponse {

    @Key("email")
    private String email;

    @Key("verified_email")
    private boolean verifiedEmail;

    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    public boolean isVerifiedEmail() {
      return verifiedEmail;
    }

    public void setVerifiedEmail(boolean verifiedEmail) {
      this.verifiedEmail = verifiedEmail;
    }
  }
}
