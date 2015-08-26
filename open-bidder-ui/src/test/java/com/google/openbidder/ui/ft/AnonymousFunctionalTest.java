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

package com.google.openbidder.ui.ft;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import com.google.api.client.util.escape.CharEscapers;
import com.google.openbidder.ui.resource.support.ResourcePath;
import com.google.openbidder.ui.resource.support.ResourceType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;

import java.util.Collection;

/**
 * Tests security for cases where there is no logged in user.
 */
@RunWith(Parameterized.class)
@ContextConfiguration(
    locations = {
        "file:src/main/webapp/WEB-INF/applicationContext.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-security.xml",
        "file:src/main/webapp/WEB-INF/ui-servlet.xml",
        "classpath:/bean-overrides.xml"
    })
@WebAppConfiguration
public class AnonymousFunctionalTest extends OpenBidderFunctionalTestCase {

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return asList(new Object[][] {
        {ResourceType.PROJECT.getResourceCollectionId()},
        {ResourceType.PROJECT.getResourceId("1")},
        {ResourceType.AD.getResourceCollectionId("1")},
        {ResourceType.AD.getResourceId("1", "abcd")},
        {ResourceType.AD_GROUP.getResourceCollectionId("1")},
        {ResourceType.AD_GROUP.getResourceId("1", "abcd")},
        {ResourceType.CAMPAIGN.getResourceCollectionId("1")},
        {ResourceType.CAMPAIGN.getResourceId("1", "abcd")},
        {ResourceType.FIREWALL.getResourceCollectionId("1")},
        {ResourceType.FIREWALL.getResourceId("1", "abcd")},
        {ResourceType.INSTANCE.getResourceCollectionId("1", "a")},
        {ResourceType.INSTANCE.getResourceId("1", "a", "abcd")},
        {ResourceType.MACHINE_TYPE.getResourceCollectionId("1", "a")},
        {ResourceType.MACHINE_TYPE.getResourceId("1", "a", "abcd")},
        {ResourceType.NETWORK.getResourceCollectionId("1")},
        {ResourceType.NETWORK.getResourceId("1", "abcd")},
        {ResourceType.QUOTA.getResourceCollectionId("1")},
        {ResourceType.QUOTA.getResourceId("1", "abcd")},
        {ResourceType.REPORT.getResourceCollectionId("1")},
        {ResourceType.REPORT.getResourceId("1", "abcd")},
        {ResourceType.USER.getResourceCollectionId("1")},
        {ResourceType.USER.getResourceId("1", "abcd")},
        {ResourceType.ZONE.getResourceCollectionId("1")},
        {ResourceType.ZONE.getResourceId("1", "abcd")}
    });
  }

  @Override
  protected void beforeInitMvc() throws Exception {
    TestContextManager testContextManager = new TestContextManager(getClass());
    testContextManager.prepareTestInstance(this);
  }

  private final ResourcePath resourcePath;

  public AnonymousFunctionalTest(ResourcePath resourcePath) {
    this.resourcePath = checkNotNull(resourcePath);
  }

  @Test
  public void get_notLoggedIn_forbidden() {
    expectLoginRedirect(get(resourcePath.getResourceUri()));
  }

  @Test
  public void postForm_notLoggedIn_forbidden() {
    expectLoginRedirect(postObjectUrlEncoded(resourcePath.getResourceUri(), emptyRequest()));
  }

  @Test
  public void postJson_notLoggedIn_forbidden() {
    expectLoginRedirect(postObjectJson(resourcePath.getResourceUri(), emptyRequest()));
  }

  @Test
  public void put_notLoggedIn_forbidden() {
    expectLoginRedirect(put(resourcePath.getResourceUri()));
  }

  @Test
  public void delete_notLoggedIn_forbidden() {
    expectLoginRedirect(delete(resourcePath.getResourceUri()));
  }

  private MvcResult expectLoginRedirect(RequestBuilder requestBuilder) {
    return expectMatcher(requestBuilder, redirectedUrl(
        String.format("/_ah/login?continue=%s",
            CharEscapers.escapeUri(resourcePath.getResourceUri()))));
  }
}
