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

package com.google.openbidder.ui.util;

import static com.googlecode.objectify.ObjectifyService.ofy;
import static java.util.Arrays.asList;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.common.collect.ImmutableMap;
import com.google.openbidder.ui.entity.AuthorizationToken;
import com.google.openbidder.ui.entity.Project;
import com.google.openbidder.ui.entity.Subscriber;
import com.google.openbidder.ui.entity.UserPreference;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cache.PendingFutures;
import com.googlecode.objectify.util.Closeable;

import org.junit.After;
import org.junit.Before;

/**
 * Objectify setup and teardown for necessary AppEngine stubs. Basically the same as
 * http://code.google.com/p/objectify-appengine/source/browse/trunk/src/com/googlecode/objectify/test/TestBase.java?spec=svn791&r=791.
 * <p>
 * Note: "mvn test" assumes the current working directory is the module's top-level directory.
 * For multi-module Maven projects, this is the submodule directory (in which "mvn test" is
 * currently being run). This behaviour is assumed and required to make unit testing with
 * non-default task queues to work (as the task queue config needs to read the queue.xml file).
 * IDEs may differ in their default configuration.
 * <p>
 * In IntelliJ, choose Run -> Edit Configurations -> Defaults -> JUnit. Set working directory to
 * $MODULE_DIR$ (there is a shortcut button on the right to do this). Update any existing test
 * configurations you have.
 * <p>
 * Background:
 * <ul>
 *   <li>http://youtrack.jetbrains.com/issue/IDEA-51283</li>
 *   <li>http://youtrack.jetbrains.com/issue/IDEA-25194</li>
 * </ul>
 */
public abstract class AppEngineTestCase {

  protected final LocalServiceTestHelper helper;
  protected String currentUser;
  private Closeable ofyService;

  public AppEngineTestCase() {
    helper = new LocalServiceTestHelper(
        new LocalDatastoreServiceTestConfig()
            .setApplyAllHighRepJobPolicy(),
        new LocalMemcacheServiceTestConfig(),
        new LocalTaskQueueTestConfig(),
        new LocalUserServiceTestConfig());
  }

  @Before
  public final void before() {
    helper.setUp();
    currentUser = null;

    TestObjectifyFactory factory = new TestObjectifyFactory();
    factory.setEntityClasses(asList(
        AuthorizationToken.class,
        Project.class,
        Subscriber.class,
        UserPreference.class));
    factory.postConstruct();
    ofyService = ObjectifyService.begin();
  }

  @After
  public final void after() {
    if (ofyService != null) {
      ofyService.close();
    }
    PendingFutures.completeAllPendingFutures();
    helper.tearDown();
  }

  protected void login(String email) {
    login(email, true);
  }

  protected void login(String email, boolean isAdmin) {
    currentUser = email;
    helper.setEnvIsLoggedIn(true);
    helper.setEnvEmail(email);
    helper.setEnvAuthDomain(email.split("@", 2)[1]);
    helper.setEnvIsAdmin(isAdmin);
    helper.setEnvAttributes(ImmutableMap.<String, Object>of(
        // It appears as if this will only set the user ID of {@link UserService#getCurrentUser}
        // if executed before the call to {@link helper#setUp}. That shouldn't matter because
        // we're not relying on it anyway. if it does become an issue, perhaps this should always
        // be set in the test setup instead, regardless of the user's identity.
        "com.google.appengine.api.users.UserService.user_id_key", "10"));
  }

  protected void logout() {
    helper.setEnvIsLoggedIn(false);
  }

  protected <T> T getEntity(Key<T> key) {
    return ofy().load().key(key).safe();
  }

  protected <T> T putAndGet(T entity) {
    Key<T> key = ofy().save().entity(entity).now();
    T ret = ofy().load().now(key);
    if (ret == null) {
      throw new RuntimeException(); //TODO(opinali) could we avoid this?
    }
    return ret;
  }
}
