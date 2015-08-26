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

import static java.util.Collections.emptyMap;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.common.collect.ObjectArrays;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

/**
 * Spring MVC integration testing.
 */
public class WebFunctionalTestCase extends AppEngineTestCase {

  protected static final Logger logger = LoggerFactory.getLogger(WebFunctionalTestCase.class);

  private static Set<String> IGNORE_PROPERTIES = ImmutableSet.of(
      "class"
  );

  @Inject
  protected WebApplicationContext appContext;

  @Inject
  protected ObjectMapper objectMapper;

  @Inject
  protected FilterChainProxy springSecurityFilterChain;

  protected MockMvc mvc;

  @Before
  public void initMvc() throws Exception {
    beforeInitMvc();
    mvc = MockMvcBuilders.webAppContextSetup(appContext)
        .addFilter(springSecurityFilterChain)
        .build();
  }

  /**
   * This hook is provided for subclasses that are parameterized such that they can
   * instantiate a test runner context directly before we try and instantiate the MVC
   * environment.
   */
  protected void beforeInitMvc() throws Exception {
  }

  protected MockHttpServletRequestBuilder postObjectUrlEncoded(String uri, Object object) {
    MockHttpServletRequestBuilder requestBuilder = post(uri);
    BeanWrapper beanWrapper = new BeanWrapperImpl(object);
    try {
      for (PropertyDescriptor propertyDescriptor : beanWrapper.getPropertyDescriptors()) {
        String propertyName = propertyDescriptor.getName();
        if (!IGNORE_PROPERTIES.contains(propertyName)
            && propertyDescriptor.getReadMethod() != null) {
          Object value = propertyDescriptor.getReadMethod().invoke(object);
          if (value instanceof Iterable<?>) {
            for (Object o : (Iterable<?>) value) {
              if (o != null) {
                requestBuilder.param(propertyName, getValue(o));
              }
            }
          } else if (value instanceof Map) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
              // Null key or value will NPE; we don't support them for tests
              requestBuilder.param(
                  propertyName + "." + entry.getKey(), entry.getValue().toString());
            }
          } else if (value != null) {
            requestBuilder.param(propertyName, getValue(value));
          }
        }
      }
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException("Error creating POST request", e);
    }
    return requestBuilder;
  }

  protected MvcResult expectMatcher(
      RequestBuilder requestBuilder,
      ResultMatcher statusMatcher,
      ResultMatcher... resultMatchers) {

    try {
      ResultActions resultActions = mvc.perform(requestBuilder)
          .andExpect(statusMatcher);
      for (ResultMatcher resultMatcher : resultMatchers) {
        resultActions = resultActions.andExpect(resultMatcher);
      }
      return resultActions.andReturn();
    } catch (Exception e) {
      throw new RuntimeException("Error performing request", e);
    }
  }

  protected MvcResult expectForbidden(RequestBuilder requestBuilder) {
    return expectMatcher(requestBuilder, status().isForbidden());
  }

  protected MvcResult expectConflict(RequestBuilder requestBuilder) {
    return expectMatcher(requestBuilder, status().isConflict());
  }

  protected MvcResult expectNotFound(RequestBuilder requestBuilder) {
    return expectMatcher(requestBuilder, status().isNotFound());
  }

  protected MvcResult expectUnauthorized(RequestBuilder requestBuilder) {
    return expectMatcher(requestBuilder, status().isUnauthorized());
  }

  protected MvcResult expectPreconditionFailed(RequestBuilder requestBuilder) {
    return expectMatcher(requestBuilder, status().isPreconditionFailed());
  }

  protected MvcResult expectMethodNotAllowed(RequestBuilder requestBuilder) {
    return expectMatcher(requestBuilder, status().isMethodNotAllowed());
  }

  protected MvcResult expectBadRequest(RequestBuilder requestBuilder) {
    return expectMatcher(requestBuilder, status().isBadRequest());
  }

  protected MvcResult expectOk(RequestBuilder requestBuilder, ResultMatcher... resultMatchers) {
    return expectMatcher(requestBuilder, status().isOk(), resultMatchers);
  }

  protected MvcResult expectJson(RequestBuilder requestBuilder, ResultMatcher... resultMatchers) {
    return expectOk(requestBuilder,
        ObjectArrays.concat(content().contentType(MediaType.APPLICATION_JSON), resultMatchers));
  }

  protected <T> T expectJson(
      RequestBuilder requestBuilder,
      Class<T> klass,
      ResultMatcher... resultMatchers) {

    MvcResult mvcResult = expectJson(requestBuilder, resultMatchers);
    String json;
    try {
      json = mvcResult.getResponse().getContentAsString();
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Error retrieving JSON from response", e);
    }
    try {
      return objectMapper.readValue(json, klass);
    } catch (IOException e) {
      throw new RuntimeException("Error parsing JSON (" + klass.getName() + "): " + json, e);
    }
  }

  protected <T> List<T> expectJson(
      RequestBuilder requestBuilder,
      TypeReference<List<T>> klass,
      ResultMatcher... resultMatchers) {

    MvcResult mvcResult = expectJson(requestBuilder, resultMatchers);
    String json;
    try {
      json = mvcResult.getResponse().getContentAsString();
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Error retrieving JSON from response", e);
    }
    try {
      return objectMapper.readValue(json, klass);
    } catch (IOException e) {
      throw new RuntimeException("Error parsing JSON list ("
          + com.google.openbidder.util.ReflectionUtils.getPrettyName(klass.getClass())
          + "): " + json, e);
    }
  }

protected MockHttpServletRequestBuilder postObjectJson(String uri, Object object) {
    return post(uri)
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(object));
  }

  protected MockHttpServletRequestBuilder putObjectJson(String uri, Object object) {
    return put(uri)
        .contentType(MediaType.APPLICATION_JSON)
        .content(toJson(object));
  }

  protected byte[] toJson(Object object) {
    try {
      logger.info("Serializing {}", object);
      String output = objectMapper.writer(new DefaultPrettyPrinter()).writeValueAsString(object);
      logger.info("Output: {}", output);
      return output.getBytes(Charsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalArgumentException("Error serializing to JSON: " + object, e);
    }
  }

  protected static Object emptyRequest() {
    return emptyMap();
  }

  private String getValue(Object value)
      throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    Set<Method> jsonValueMethods = ReflectionUtils.getAllMethods(value.getClass(),
        ReflectionUtils.withAnnotation(JsonValue.class));
    if (!jsonValueMethods.isEmpty()) {
      Method valueMethod = Iterators.getOnlyElement(jsonValueMethods.iterator());
      Object jsonValue = valueMethod.invoke(value);
      return jsonValue == null ? null : jsonValue.toString();
    }
    return value.toString();
  }
}
