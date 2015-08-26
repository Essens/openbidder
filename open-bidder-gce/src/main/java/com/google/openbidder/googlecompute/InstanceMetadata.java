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

package com.google.openbidder.googlecompute;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.io.CharStreams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A client for reading the metadata on a Google Compute instance.
 *
 * See the <a href="https://code.google.com/apis/compute/docs/instances.html#metadataserver">
 * Google Compute documentation</a>.
 */
public class InstanceMetadata {
  private static final Logger logger = LoggerFactory.getLogger(InstanceMetadata.class);

  private static final String API_HOST = "metadata";
  private static final String API_VERSION = "0.1";
  private static final String API_PREFIX = "meta-data";
  private static final String API_SERVICE_TOKEN_SCOPE_KEY = "scopes";
  private static final String API_SERVICE_TOKEN_PATH = "service-accounts/%s/acquire";
  private static final GenericUrl BASE_URL = new GenericUrl(
      String.format("http://%s/%s/%s/", API_HOST, API_VERSION, API_PREFIX));

  private final HttpRequestFactory requestFactory;
  private final JsonObjectParser jsonParser;

  public InstanceMetadata(JsonFactory jsonFactory, HttpTransport httpTransport) {
    this.jsonParser = new JsonObjectParser(jsonFactory);
    this.requestFactory = httpTransport.createRequestFactory();
  }

  /**
   * Retrieves a meta-data key from the server.
   *
   * @param keyName The meta data key
   * @throws MetadataNotFoundException If the key is not found
   * @throws IllegalStateException For other HTTP response errors
   * @return The value of the meta data key
   */
  public String metadata(String keyName) {
    GenericUrl metadataUrl = BASE_URL.clone();
    metadataUrl.appendRawPath(keyName);

    String value;
    try {
      HttpRequest request = requestFactory.buildGetRequest(metadataUrl);
      HttpResponse response = request.execute();
      value = inputStreamToString(response.getContent());
    } catch (HttpResponseException e) {
      if (e.getStatusCode() == HttpStatusCodes.STATUS_CODE_NOT_FOUND) {
        throw new MetadataNotFoundException(keyName);
      } else {
        throw new IllegalStateException(e);
      }
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }

    return value;
  }

  /**
   * Extracts the short form of a Compute Engine resource.  E.g.
   * {@code projects/123/zones/rtb-us-east2} will be mapped to {@code rtb-us-east2}.
   */
  public static String resourceShortName(String resource) {
    return Iterables.getLast(Splitter.on("/").split(resource));
  }

  /**
   * Retrieves a custom meta-data key that was specified at instance start up.  This is
   * equivalent to calling {#link metadata} with your key prefixed with {code attributes/}.
   *
   * @param keyName The meta data key
   * @throws MetadataNotFoundException If the key is not found
   * @return The value of the meta data key
   */
  public String customMetadata(String keyName) {
    return metadata("attributes/" + keyName);
  }

  /**
   * Retrieves the OAuth2 service access token and metadata.
   *
   * @param scope OAuth2 scope of the service
   * @throws MetadataOAuth2ScopeNotFoundException If the scope was not found in the meta-data
   *         server
   * @return {@link OAuth2ServiceTokenMetadata} for the given scope
   */
  public OAuth2ServiceTokenMetadata serviceToken(final String scope) {
    return serviceToken("default", scope);
  }

  /**
   * Retrieves the OAuth2 service access token and metadata.
   *
   * @param serviceAccount Compute service account of the instance
   * @param scope OAuth2 scope of the service
   * @throws MetadataOAuth2ScopeNotFoundException If the scope was not found in the meta-data
   *         server
   * @return {@link OAuth2ServiceTokenMetadata} for the given account and scope
   */
  public OAuth2ServiceTokenMetadata serviceToken(final String serviceAccount, final String scope) {
    GenericUrl serviceTokenUrl = BASE_URL.clone();
    serviceTokenUrl.appendRawPath(String.format(API_SERVICE_TOKEN_PATH, serviceAccount));
    serviceTokenUrl.put(API_SERVICE_TOKEN_SCOPE_KEY, scope);
    if (logger.isDebugEnabled()) {
      logger.debug("Retrieving service access token metadata from URL {}", serviceTokenUrl);
    }

    HttpResponse response;
    try {
      HttpRequest request = requestFactory.buildGetRequest(serviceTokenUrl);
      request.setParser(jsonParser);
      response = request.execute();
    } catch (HttpResponseException e) {
      if (e.getStatusCode() == HttpStatusCodes.STATUS_CODE_NOT_FOUND) {
        throw new MetadataOAuth2ScopeNotFoundException(scope);
      } else {
        throw new IllegalStateException(e);
      }
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }

    try {
      return response.parseAs(OAuth2ServiceTokenMetadata.class);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Retrieves the network meta-data.
   *
   * @throws IllegalStateException For HTTP response errors
   * @return {@link NetworkMetadata} describing the instance
   */
  public NetworkMetadata network() {
    GenericUrl networkUrl = BASE_URL.clone();
    networkUrl.appendRawPath("network");

    HttpResponse response;
    try {
      HttpRequest request = requestFactory.buildGetRequest(networkUrl);
      request.setParser(jsonParser);
      response = request.execute();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }

    try {
      return response.parseAs(NetworkMetadata.class);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  private String inputStreamToString(final InputStream inputStream) throws IOException {
    return CharStreams.toString(new InputStreamReader(inputStream, Charsets.UTF_8));
  }
}
