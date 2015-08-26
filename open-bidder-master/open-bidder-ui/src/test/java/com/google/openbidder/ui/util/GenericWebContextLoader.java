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

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.web.MockRequestDispatcher;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.support.AbstractContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

import javax.servlet.RequestDispatcher;

/**
 * From https://github.com/SpringSource/spring-test-mvc/blob/master/src/test/java/org/springframework/test/web/server/samples/context/GenericWebContextLoader.java
 * as a stopgap until Spring 3.2 (at least M2) is released.
 * <p>
 * This class is here temporarily until the TestContext framework provides
 * support for WebApplicationContext yet:
 * <p>
 * https://jira.springsource.org/browse/SPR-5243
 * <p>
 * After that this class will no longer be needed. It's provided here as an example
 * and to serve as a temporary solution.
 */
public class GenericWebContextLoader extends AbstractContextLoader {
  protected final MockServletContext servletContext;

  public GenericWebContextLoader(String warRootDir, boolean isClasspathRelative) {
    ResourceLoader resourceLoader = isClasspathRelative
        ? new DefaultResourceLoader()
        : new FileSystemResourceLoader();
    this.servletContext = initServletContext(warRootDir, resourceLoader);
  }

  private MockServletContext initServletContext(
      String warRootDir,
      ResourceLoader resourceLoader) {

    return new MockServletContext(warRootDir, resourceLoader) {
      // Required for DefaultServletHttpRequestHandler...
      @Override
      public RequestDispatcher getNamedDispatcher(String path) {
        return "default".equals(path)
            ? new MockRequestDispatcher(path)
            : super.getNamedDispatcher(path);
      }
    };
  }

  @Override
  public ApplicationContext loadContext(MergedContextConfiguration mergedConfig) throws Exception {
    GenericWebApplicationContext context = new GenericWebApplicationContext();
    context.getEnvironment().setActiveProfiles(mergedConfig.getActiveProfiles());
    prepareContext(context);
    loadBeanDefinitions(context, mergedConfig);
    return context;
  }

  @Override
  public ApplicationContext loadContext(String... locations) throws Exception {
    // should never be called
    throw new UnsupportedOperationException();
  }

  protected void prepareContext(GenericWebApplicationContext context) {
    this.servletContext.setAttribute(
        WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, context);
    context.setServletContext(this.servletContext);
  }

  protected void loadBeanDefinitions(GenericWebApplicationContext context, String[] locations) {
    new XmlBeanDefinitionReader(context).loadBeanDefinitions(locations);
    AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
    context.refresh();
    context.registerShutdownHook();
  }

  protected void loadBeanDefinitions(
      GenericWebApplicationContext context,
      MergedContextConfiguration mergedConfig) {

    new AnnotatedBeanDefinitionReader(context).register(mergedConfig.getClasses());
    loadBeanDefinitions(context, mergedConfig.getLocations());
  }

  @Override
  protected String getResourceSuffix() {
    return "-context.xml";
  }
}
